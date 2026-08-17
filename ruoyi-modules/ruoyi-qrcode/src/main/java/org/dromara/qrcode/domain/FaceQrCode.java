package org.dromara.qrcode.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 二维码通行对象 face_qr_code
 *
 * @author qrcode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("face_qr_code")
public class FaceQrCode extends BaseEntity {

    /**
     * 二维码ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 二维码类型: A业主/用户 B访客
     */
    private String qrType;

    /**
     * 绑定人员ID (A#必填)
     */
    private Long personId;

    /**
     * 访客单ID (可选)
     */
    private Long orderId;

    /**
     * 项目ID (需与设备识别设置的二维码项目ID一致)
     */
    private Long projectId;

    /**
     * 指定设备序列号, 字符串"null"表示通配所有设备
     */
    private String sn;

    /**
     * 用户id (人脸+二维码模式 A#必填)
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

}
