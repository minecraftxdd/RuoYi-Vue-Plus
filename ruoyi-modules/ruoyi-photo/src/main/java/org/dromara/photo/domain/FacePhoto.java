package org.dromara.photo.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 人脸照片对象 face_photo
 *
 * @author photo
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("face_photo")
public class FacePhoto extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id")
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
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 人脸特征编码(Base64)
     */
    private String faceEncoding;

    /**
     * 编码状态: 0待编码 1编码中 2成功 3失败
     */
    private String encodingStatus;

    /**
     * 编码失败原因
     */
    private String encodingError;

    /**
     * 检测到的人脸数量
     */
    private Integer faceCount;

    /**
     * 备注
     */
    private String remark;

}
