package org.dromara.photo.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 人脸照片编码日志视图对象
 *
 * @author photo
 */
@Data
public class FacePhotoEncodeLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 照片ID
     */
    private Long photoId;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 编码类型
     */
    private String encodeType;

    /**
     * 编码后数据(Base64)
     */
    private String encodingData;

    /**
     * 编码后数据长度
     */
    private Integer encodingDataLength;

    /**
     * 人脸索引
     */
    private Integer faceIndex;

    /**
     * 状态: 0成功 1失败
     */
    private String status;

    /**
     * 状态标签
     */
    private String statusLabel;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    private Date createTime;

}
