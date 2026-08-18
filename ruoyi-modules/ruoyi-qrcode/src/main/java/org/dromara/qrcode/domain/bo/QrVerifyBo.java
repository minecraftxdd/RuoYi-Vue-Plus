package org.dromara.qrcode.domain.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备二维码在线比对请求 (设备协议: 17.2.二维码-在线比对)
 *
 * @author qrcode
 */
@Data
public class QrVerifyBo {

    /**
     * 二维码内容
     */
    @JsonProperty("qrcode_content")
    @NotBlank(message = "二维码内容不能为空")
    private String qrcodeContent;

    /**
     * 设备号
     */
    @NotBlank(message = "设备号不能为空")
    private String sn;

    /**
     * 体温 (启用测温的设备才有值)
     */
    private Double temperature;

    /**
     * 用户id (识别设置中验证模式选择【人脸加二维码】模式下才会有值)
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 识别时间 (yyyy-MM-dd HH:mm:ss)
     */
    @JsonProperty("recog_time")
    private String recogTime;

}
