package org.dromara.photo.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.photo.domain.FacePhoto;
import org.dromara.photo.domain.bo.FacePhotoBo;
import org.dromara.photo.domain.vo.FacePhotoEncodeLogVo;
import org.dromara.photo.domain.vo.FacePhotoVo;
import org.dromara.photo.mapper.FacePhotoEncodeLogMapper;
import org.dromara.photo.mapper.FacePhotoMapper;
import org.dromara.photo.task.FaceEncodeTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 人脸照片 服务层实现
 * 使用本地磁盘存储，不依赖OSS
 *
 * @author photo
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FacePhotoServiceImpl implements org.dromara.photo.service.IFacePhotoService {

    private final FacePhotoMapper baseMapper;
    private final FacePhotoEncodeLogMapper encodeLogMapper;
    private final FaceEncodeTask faceEncodeTask;

    /**
     * 最大文件大小：1MB
     */
    @Value("${photo.upload.max-size:1048576}")
    private Long maxSize;

    /**
     * 文件存储根路径
     */
    @Value("${photo.upload.path:E:/uploads/face_photos}")
    private String uploadPath;

    /**
     * 静态资源访问前缀
     */
    @Value("${photo.upload.url-prefix:/photo/file}")
    private String urlPrefix;

    /**
     * 编码状态常量
     */
    private static final String STATUS_PENDING = "0";
    private static final String STATUS_ENCODING = "1";
    private static final String STATUS_SUCCESS = "2";
    private static final String STATUS_FAILED = "3";

    @Override
    public TableDataInfo<FacePhotoVo> selectPagePhotoList(FacePhotoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FacePhoto> lqw = buildQueryWrapper(bo);
        Page<FacePhotoVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 填充状态标签和URL
        page.getRecords().forEach(this::fillPhotoDetail);
        return TableDataInfo.build(page);
    }

    @Override
    public FacePhotoVo selectPhotoById(Long id) {
        FacePhoto photo = baseMapper.selectById(id);
        if (photo == null) {
            return null;
        }
        FacePhotoVo vo = MapstructUtils.convert(photo, FacePhotoVo.class);
        fillPhotoDetail(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FacePhotoVo upload(MultipartFile file) {
        // 1. 先读取字节到内存，避免 Undertow 临时文件被提前清理
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败: " + e.getMessage());
        }

        // 2. 校验文件大小
        if (fileBytes.length > maxSize) {
            throw new ServiceException("文件大小不能超过1MB");
        }

        // 3. 校验文件类型（仅允许 JPG/JPEG/PNG）
        String originalFileName = file.getOriginalFilename();
        String suffix = StrUtil.emptyIfNull(originalFileName);
        int dotIndex = suffix.lastIndexOf(".");
        if (dotIndex < 0) {
            throw new ServiceException("文件名缺少后缀");
        }
        suffix = suffix.substring(dotIndex);
        if (!StringUtils.equalsAnyIgnoreCase(suffix, ".jpg", ".jpeg", ".png")) {
            throw new ServiceException("仅支持JPG/JPEG/PNG格式图片");
        }
        String fileSuffix = suffix.replace(".", "");

        // 4. 生成本地存储路径: yyyy/MM/dd/uuid.suffix
        String datePath = DateUtils.datePath();
        String uuid = IdUtil.fastSimpleUUID();
        String relativePath = datePath + "/" + uuid + "." + fileSuffix;

        // 5. 写入本地磁盘
        try {
            Path dirPath = Paths.get(uploadPath, datePath);
            Files.createDirectories(dirPath);
            Path filePath = Paths.get(uploadPath, relativePath);
            Files.write(filePath, fileBytes);
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        }

        // 6. 创建照片记录
        FacePhoto photo = new FacePhoto();
        photo.setFilePath(relativePath);
        photo.setOriginalName(originalFileName);
        photo.setFileSuffix(fileSuffix);
        photo.setFileSize((long) fileBytes.length);
        photo.setEncodingStatus(STATUS_PENDING);
        photo.setFaceCount(0);

        baseMapper.insert(photo);

        return selectPhotoById(photo.getId());
    }

    @Override
    public void encodeFace(Long id) {
        FacePhoto photo = baseMapper.selectById(id);
        if (photo == null) {
            throw new RuntimeException("照片不存在");
        }
        if (STATUS_SUCCESS.equals(photo.getEncodingStatus())) {
            throw new RuntimeException("该照片已编码完成");
        }

        // 更新状态为编码中
        photo.setEncodingStatus(STATUS_ENCODING);
        baseMapper.updateById(photo);

        // 异步入队处理
        faceEncodeTask.execute(photo.getId());
    }

    @Override
    public FacePhotoVo getEncodingStatus(Long id) {
        return selectPhotoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePhotoByIds(Long[] ids) {
        // 1. 删除本地文件
        for (Long id : ids) {
            FacePhoto photo = baseMapper.selectById(id);
            if (photo != null && StringUtils.isNotEmpty(photo.getFilePath())) {
                try {
                    Path filePath = Paths.get(uploadPath, photo.getFilePath());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    log.warn("删除本地文件失败: {}", e.getMessage());
                }
            }
        }
        // 2. 删除数据库记录
        return baseMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public List<FacePhotoEncodeLogVo> selectEncodeLogList(Long photoId) {
        return baseMapper.selectEncodeLogList(photoId);
    }

    @Override
    public int deleteEncodeLogByIds(Long[] ids) {
        // 仅删除日志记录，不影响照片本体及其编码数据
        return encodeLogMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public String getEncodingData(Long photoId) {
        // 首先从FacePhoto表获取编码数据
        FacePhoto photo = baseMapper.selectById(photoId);
        if (photo != null && StringUtils.isNotEmpty(photo.getFaceEncoding())) {
            log.info("从FacePhoto表获取编码数据，photoId: {}, 数据前50字符: {}", 
                    photoId, photo.getFaceEncoding().substring(0, Math.min(50, photo.getFaceEncoding().length())));
            return photo.getFaceEncoding();
        }
        
        // 如果FacePhoto表没有，从编码日志表获取
        List<FacePhotoEncodeLogVo> logs = baseMapper.selectEncodeLogList(photoId);
        for (FacePhotoEncodeLogVo encodeLog : logs) {
            if ("0".equals(encodeLog.getStatus()) && StringUtils.isNotEmpty(encodeLog.getEncodingData())) {
                log.info("从编码日志表获取编码数据，photoId: {}, 数据前50字符: {}",
                        photoId, encodeLog.getEncodingData().substring(0, Math.min(50, encodeLog.getEncodingData().length())));
                return encodeLog.getEncodingData();
            }
        }
        return null;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<FacePhoto> buildQueryWrapper(FacePhotoBo bo) {
        LambdaQueryWrapper<FacePhoto> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(bo.getOriginalName()), FacePhoto::getOriginalName, bo.getOriginalName())
           .eq(StringUtils.isNotEmpty(bo.getEncodingStatus()), FacePhoto::getEncodingStatus, bo.getEncodingStatus())
           .orderByDesc(FacePhoto::getCreateTime);
        return lqw;
    }

    /**
     * 填充照片详情（状态标签 + URL）
     */
    private void fillPhotoDetail(FacePhotoVo vo) {
        if (vo == null) {
            return;
        }
        // 填充状态标签
        switch (vo.getEncodingStatus()) {
            case STATUS_PENDING:
                vo.setEncodingStatusLabel("待编码");
                break;
            case STATUS_ENCODING:
                vo.setEncodingStatusLabel("编码中");
                break;
            case STATUS_SUCCESS:
                vo.setEncodingStatusLabel("已成功");
                break;
            case STATUS_FAILED:
                vo.setEncodingStatusLabel("失败");
                break;
            default:
                vo.setEncodingStatusLabel("未知");
        }
        // 构建访问URL
        if (StringUtils.isNotEmpty(vo.getFilePath())) {
            String url = urlPrefix + "/" + vo.getFilePath();
            vo.setUrl(url);
            // 没有缩略图时用原图作为预览
            if (StringUtils.isEmpty(vo.getThumbnailUrl())) {
                vo.setThumbnailUrl(url);
            }
        }
    }

}
