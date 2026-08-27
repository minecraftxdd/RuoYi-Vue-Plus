package org.dromara.photo.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.photo.domain.FacePhoto;

/**
 * 人脸照片业务对象 face_photo
 *
 * @author photo
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FacePhoto.class, reverseConvertGenerate = false)
public class FacePhotoBo extends BaseEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文件存储相对路径
     */
    private String filePath;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件后缀名
     */
    private String fileSuffix;

    /**
     * 编码状态: 0待编码 1编码中 2成功 3失败
     */
    private String encodingStatus;

    /**
     * 备注
     */
    private String remark;

}
