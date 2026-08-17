package org.dromara.qrcode.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 设备在线比对响应的 Content 对象 (JSON 对象, 可为空对象 {})
 *
 * @author qrcode
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrVerifyContentVo {

    /**
     * 提示的语音代码, -2 为自定义语音(使用 voice_text 朗读)
     */
    @JsonProperty("voice_code")
    private Integer voiceCode;

    /**
     * 语音文字, 仅在 voice_code 为 -2 时解析
     */
    @JsonProperty("voice_text")
    private String voiceText;

    /**
     * 用户id (Result 为 7 时必填, 设备据此做 1:1 人脸比对)
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * Result 为 8 时需要 1:1 核验的照片 (http链接或base64图片)
     */
    @JsonProperty("user_photo")
    private String userPhoto;

}
