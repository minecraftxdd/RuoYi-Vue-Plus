package org.dromara.qrcode.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 设备二维码在线比对响应 (设备协议: 17.2.二维码-在线比对)
 * 注意: 字段名必须严格匹配设备协议(Result/Msg/Content), Content 必须为 JSON 对象, 不能为字符串
 *
 * @author qrcode
 */
@Data
public class QrVerifyVo {

    /**
     * 识别结果: 0成功 1内容无效 2已过期 3超过有效次数 4未绑定设备 5项目不一致 6其他错误(必带voice) 7需1:1人脸比对
     */
    @JsonProperty("Result")
    private Integer result;

    /**
     * 对应 Result 的状态, 可为空
     */
    @JsonProperty("Msg")
    private String msg;

    /**
     * 识别后返回的详细信息 (JSON 对象, 必须为对象不能为字符串, 可为 {})
     */
    @JsonProperty("Content")
    private QrVerifyContentVo content = new QrVerifyContentVo();

}
