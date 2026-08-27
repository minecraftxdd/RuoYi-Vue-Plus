package org.dromara.photo.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.photo.domain.FacePhoto;
import org.dromara.photo.domain.FacePhotoEncodeLog;
import org.dromara.photo.mapper.FacePhotoEncodeLogMapper;
import org.dromara.photo.mapper.FacePhotoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;

/**
 * 人脸编码异步任务
 *
 * @author photo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FaceEncodeTask {

    private final FacePhotoMapper photoMapper;
    private final FacePhotoEncodeLogMapper encodeLogMapper;

    @Value("${photo.upload.path:E:/uploads/face_photos}")
    private String uploadPath;

    /**
     * 异步执行编码任务：读取图片文件 → 转Base64 → 存入日志
     *
     * @param photoId 照片ID
     */
    @Async("faceEncodeExecutor")
    public void execute(Long photoId) {
        log.info("开始异步编码照片，photoId: {}", photoId);

        FacePhoto photo = photoMapper.selectById(photoId);
        if (photo == null) {
            log.warn("照片不存在，photoId: {}", photoId);
            return;
        }

        try {
            // 1. 读取本地图片文件
            Path filePath = Paths.get(uploadPath, photo.getFilePath());
            if (!Files.exists(filePath)) {
                throw new IOException("图片文件不存在: " + filePath);
            }
            byte[] imageBytes = Files.readAllBytes(filePath);

            // 2. 转换为 Base64 字符串
            String base64Data = Base64.getEncoder().encodeToString(imageBytes);
            
            // 3. 添加Base64前缀（符合通信规范要求：JPEG与PNG的前缀MIME类型必须严格区分）
            String mimeType = resolveMimeType(photo.getFileSuffix());
            String base64WithPrefix = "data:" + mimeType + ";base64," + base64Data;
            
            // 4. URL编码（符合通信规范要求）
            String urlEncodedData = URLEncoder.encode(base64WithPrefix, StandardCharsets.UTF_8.name());
            
            int faceCount = 1; // TODO: 实际项目替换为人脸检测结果

            // 5. 更新照片编码状态
            updateEncodeStatus(photoId, "2", urlEncodedData, null, faceCount);

            // 6. 记录编码日志（存储URL编码后的数据）
            FacePhotoEncodeLog encodeLog = new FacePhotoEncodeLog();
            encodeLog.setPhotoId(photoId);
            encodeLog.setEncodeType("image-base64");
            encodeLog.setEncodingData(urlEncodedData);
            encodeLog.setFaceIndex(0);
            encodeLog.setStatus("0");
            encodeLog.setCreateTime(new Date());
            encodeLogMapper.insert(encodeLog);

            // 调试日志：查看存储的数据格式（仅打印前50字符，避免大Base64刷爆日志）
            log.info("照片编码成功，photoId: {}", photoId);
            log.info("Base64数据前50字符: {}", base64Data.substring(0, Math.min(50, base64Data.length())));
            log.info("添加前缀后前50字符: {}", base64WithPrefix.substring(0, Math.min(50, base64WithPrefix.length())));
            log.info("URL编码后前50字符: {}", urlEncodedData.substring(0, Math.min(50, urlEncodedData.length())));

        } catch (Exception e) {
            log.error("照片编码失败，photoId: {}", photoId, e);
            updateEncodeStatus(photoId, "3", null, e.getMessage(), 0);

            // 记录失败日志（截断错误信息，防止超出列宽导致回写再次失败）
            FacePhotoEncodeLog encodeLog = new FacePhotoEncodeLog();
            encodeLog.setPhotoId(photoId);
            encodeLog.setEncodeType("image-base64");
            encodeLog.setStatus("1");
            encodeLog.setErrorMsg(truncate(e.getMessage(), 1000));
            encodeLog.setCreateTime(new Date());
            encodeLogMapper.insert(encodeLog);
        }
    }

    /**
     * 根据文件后缀解析 Data URI 的 MIME 类型
     * 通信规范：JPEG 与 PNG 的前缀必须严格区分，禁止一律使用 image/jpeg
     *
     * @param fileSuffix 文件后缀（不含点，如 jpg/jpeg/png）
     * @return MIME 类型
     */
    private String resolveMimeType(String fileSuffix) {
        if ("png".equalsIgnoreCase(fileSuffix)) {
            return "image/png";
        }
        // 上传白名单仅允许 jpg/jpeg/png，其余后缀按 jpeg 兜底
        return "image/jpeg";
    }

    /**
     * 截断字符串，防止超出数据库列宽导致回写失败（null 安全）
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    /**
     * 更新编码状态
     */
    private void updateEncodeStatus(Long photoId, String status, String encoding, String error, int faceCount) {
        FacePhoto photo = new FacePhoto();
        photo.setId(photoId);
        photo.setEncodingStatus(status);
        if (StringUtils.isNotEmpty(encoding)) {
            photo.setFaceEncoding(encoding);
            log.info("设置faceEncoding字段，photoId: {}, encoding前50字符: {}", photoId, encoding.substring(0, Math.min(50, encoding.length())));
        }
        if (StringUtils.isNotEmpty(error)) {
            photo.setEncodingError(truncate(error, 1000));
        }
        photo.setFaceCount(faceCount);
        photo.setUpdateTime(new Date());
        photoMapper.updateById(photo);
    }

}
