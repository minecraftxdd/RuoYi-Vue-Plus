package org.dromara.qrcode.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.qrcode.domain.FaceQrCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 二维码通行视图对象 face_qr_code
 *
 * @author qrcode
 */
@Data
@AutoMapper(target = FaceQrCode.class)
public class FaceQrCodeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 二维码ID
     */
    private Long id;

    /**
     * 二维码类型: A业主/用户 B访客
     */
    private String qrType;

    /**
     * 绑定人员ID
     */
    private Long personId;

    /**
     * 访客单ID
     */
    private Long orderId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 指定设备序列号
     */
    private String sn;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 有效开始时间 (秒级时间戳)
     */
    private Long validStart;

    /**
     * 有效结束时间 (秒级时间戳)
     */
    private Long validEnd;

    /**
     * 有效开始时间 (日期)
     */
    private Date validStartTime;

    /**
     * 有效结束时间 (日期)
     */
    private Date validEndTime;

    /**
     * 有效次数
     */
    private Integer totalTimes;

    /**
     * 已使用次数
     */
    private Integer usedTimes;

    /**
     * 生成的二维码内容
     */
    private String content;

    /**
     * 状态: 0有效 1作废 2用完 3过期
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

}
