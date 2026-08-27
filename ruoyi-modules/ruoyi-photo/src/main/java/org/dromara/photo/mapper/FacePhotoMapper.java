package org.dromara.photo.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.photo.domain.FacePhoto;
import org.dromara.photo.domain.vo.FacePhotoEncodeLogVo;
import org.dromara.photo.domain.vo.FacePhotoVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人脸照片 Mapper 接口
 *
 * @author photo
 */
public interface FacePhotoMapper extends BaseMapperPlus<FacePhoto, FacePhotoVo> {

    /**
     * 查询编码日志列表
     *
     * @param photoId 照片ID
     * @return 编码日志列表
     */
    List<FacePhotoEncodeLogVo> selectEncodeLogList(@Param("photoId") Long photoId);

}
