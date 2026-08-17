package org.dromara.qrcode.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.qrcode.domain.bo.QrVerifyBo;
import org.dromara.qrcode.domain.vo.QrVerifyVo;
import org.dromara.qrcode.service.IQrCodeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备端接口 - 二维码在线比对 (设备协议: 17.2.二维码-在线比对)
 *
 * <p>注意: 该接口由人脸识别设备匿名调用, 已在 security.excludes 中放行,
 * 响应字段必须严格匹配设备协议 Result/Msg/Content</p>
 *
 * @author qrcode
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class DeviceQrCodeController {

    private final IQrCodeService qrCodeService;

    /**
     * 二维码在线比对
     * <p>设备解析到二维码数据后调用, 根据返回值确定是否通行</p>
     *
     * @param bo 设备请求: {qrcode_content, sn, temperature, user_id, recog_time}
     * @return {Result, Msg, Content}
     */
    @PostMapping("/qrcode_recognition")
    public QrVerifyVo qrcodeRecognition(@RequestBody QrVerifyBo bo) {
        return qrCodeService.verify(bo);
    }

}
