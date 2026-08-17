package org.dromara.qrcode.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 二维码通行记录对象 face_qr_record
 *
 * @author qrcode
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("face_qr_record")
public class FaceQrRecord extends BaseEntity {

    /**
     * 记录ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 二维码ID
     */
    private Long qrId;

    /**
     * 二维码类型: A业主/用户 B访客
     */
    private String qrType;

    /**
     * 绑定人员ID
     */
    private Long personId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 设备号
     */
    private String sn;

    /**
     * 通行状态: 0放行 1拦截
     */
    private Integer passStatus;

    /**
     * 在线比对结果码: 0成功 1无效 2过期 3超次数 4未绑定设备 5项目不一致 6其他错误 7需1:1人脸比对
     */
    private Integer resultCode;

    /**
     * 拦截原因
     */
    private String failReason;

    /**
     * 体温
     */
    private BigDecimal bodyTemperature;

    /**
     * 二维码内容
     */
    private String qrCode;

    /**
     * 抓拍照片 (离线识别)
     */
    private String photo;

    /**
     * 全景抓拍 (离线识别)
     */
    private String panoramicPicture;

    /**
     * 健康颜色: 1000绿 2000黄 3000红 -1未知
     */
    private String healthCodeColor;

    /**
     * 健康码抓拍图 (离线识别)
     */
    private String healthCodePicture;

    /**
     * 识别时间
     */
    private Date recogTime;

    /**
     * 记录来源: 1在线比对 2离线识别
     */
    private Integer source;

}
