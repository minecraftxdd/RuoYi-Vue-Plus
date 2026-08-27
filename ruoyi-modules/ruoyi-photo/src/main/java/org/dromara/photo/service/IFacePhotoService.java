package org.dromara.photo.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.photo.domain.bo.FacePhotoBo;
import org.dromara.photo.domain.vo.FacePhotoEncodeLogVo;
import org.dromara.photo.domain.vo.FacePhotoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 人脸照片 服务层
 *
 * @author photo
 */
public interface IFacePhotoService {

    /**
     * 分页查询照片列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 照片分页列表
     */
    TableDataInfo<FacePhotoVo> selectPagePhotoList(FacePhotoBo bo, PageQuery pageQuery);

    /**
     * 查询照片详情
     *
     * @param id 照片ID
     * @return 照片信息
     */
    FacePhotoVo selectPhotoById(Long id);

    /**
     * 上传照片
     *
     * @param file 图片文件
     * @return 上传结果
     */
    FacePhotoVo upload(MultipartFile file);

    /**
     * 触发照片编码（入队异步处理）
     *
     * @param id 照片ID
     */
    void encodeFace(Long id);

    /**
     * 查询照片编码状态
     *
     * @param id 照片ID
     * @return 编码状态
     */
    FacePhotoVo getEncodingStatus(Long id);

    /**
     * 删除照片
     *
     * @param ids 照片ID串
     * @return 结果
     */
    int deletePhotoByIds(Long[] ids);

    /**
     * 查询编码日志列表
     *
     * @param photoId 照片ID
     * @return 编码日志列表
     */
    List<FacePhotoEncodeLogVo> selectEncodeLogList(Long photoId);

    /**
     * 删除编码日志
     *
     * @param ids 日志ID串
     * @return 结果
     */
    int deleteEncodeLogByIds(Long[] ids);

    /**
     * 获取照片编码数据
     *
     * @param photoId 照片ID
     * @return 编码数据（URL编码后的Base64字符串）
     */
    String getEncodingData(Long photoId);

}
