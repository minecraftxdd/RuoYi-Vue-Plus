package org.dromara.qrcode.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.qrcode.domain.FaceQrCode;

import java.util.Date;

/**
 * 二维码通行业务对象 face_qr_code
 *
 * @author qrcode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FaceQrCode.class, reverseConvertGenerate = false)
public class FaceQrCodeBo extends BaseEntity {

    /**
     * 二维码ID (新增时为空, 由服务端生成)
     */
    private Long id;

    /**
     * 二维码类型: A业主/用户 B访客
     */
    @NotBlank(message = "二维码类型不能为空")
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
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 指定设备序列号, 不填/空字符串表示通配所有设备
     */
    private String sn;

    /**
     * 用户id (人脸+二维码模式 A#必填)
     */
    private String userId;

    /**
     * 有效开始时间 (秒级时间戳)
     */
    @NotNull(message = "有效开始时间不能为空")
    private Long validStart;

    /**
     * 有效结束时间 (秒级时间戳)
     */
    @NotNull(message = "有效结束时间不能为空")
    private Long validEnd;

    /**
     * 有效次数
     */
    @NotNull(message = "有效次数不能为空")
    private Integer totalTimes;

    /**
     * 已使用次数
     */
    private Integer usedTimes;

    /**
     * 生成的二维码内容 (由服务端生成, 不可手工指定)
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
     * 有效开始时间 (日期格式, 前端表单使用, 后端将转换为秒级时间戳)
     */
    private Date validStartTime;

    /**
     * 有效结束时间 (日期格式, 前端表单使用, 后端将转换为秒级时间戳)
     */
    private Date validEndTime;

}
