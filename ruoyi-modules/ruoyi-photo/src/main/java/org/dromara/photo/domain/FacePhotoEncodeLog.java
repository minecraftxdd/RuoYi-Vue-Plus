package org.dromara.photo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 人脸照片编码日志对象 face_photo_encode_log
 *
 * @author photo
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("face_photo_encode_log")
public class FacePhotoEncodeLog extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 照片ID
     */
    private Long photoId;

    /**
     * 编码类型: facenet128/facenet512/arcface512
     */
    private String encodeType;

    /**
     * 编码后数据(Base64)
     */
    private String encodingData;

    /**
     * 人脸索引(多脸时)
     */
    private Integer faceIndex;

    /**
     * 状态: 0成功 1失败
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

}