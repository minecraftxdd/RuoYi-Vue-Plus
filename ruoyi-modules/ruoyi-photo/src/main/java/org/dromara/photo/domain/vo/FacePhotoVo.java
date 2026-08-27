package org.dromara.photo.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.photo.domain.FacePhoto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 人脸照片视图对象 face_photo
 *
 * @author photo
 */
@Data
@AutoMapper(target = FacePhoto.class)
public class FacePhotoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 原图URL
     */
    private String url;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 人脸特征编码(Base64)
     */
    private String faceEncoding;

    /**
     * 编码状态: 0待编码 1编码中 2成功 3失败
     */
    private String encodingStatus;

    /**
     * 编码状态标签
     */
    private String encodingStatusLabel;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者
     */
    private String createByName;

}
