package org.dromara.qrcode.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.qrcode.domain.bo.QrRecordWriteBo;
import org.dromara.qrcode.domain.vo.QrVerifyContentVo;
import org.dromara.qrcode.domain.vo.QrVerifyVo;
import org.dromara.qrcode.service.IQrCodeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备端接口 - 二维码离线识别记录写入 (设备协议: 17.4.二维码离线识别记录)
 *
 * <p>注意: 该接口由人脸识别设备匿名调用, 已在 security.excludes 中放行,
 * 只有二维码离线识别产生的记录才会调用此接口</p>
 *
 * @author qrcode
 */
@RequiredArgsConstructor
@RestController
public class DeviceRecordController {

    private final IQrCodeService qrCodeService;

    /**
     * 离线识别记录写入 (支持单条/多条)
     *
     * @param bo 设备请求: {sn, body_temperature, photo, qrcode_id[], create_time[], ...}
     * @return {Result, Msg}
     */
    @PostMapping("/client_write_record")
    public QrVerifyVo clientWriteRecord(@RequestBody QrRecordWriteBo bo) {
        qrCodeService.record(bo);
        QrVerifyVo resp = new QrVerifyVo();
        resp.setResult(0);
        resp.setMsg("success");
        resp.setContent(new QrVerifyContentVo());
        return resp;
    }

}
