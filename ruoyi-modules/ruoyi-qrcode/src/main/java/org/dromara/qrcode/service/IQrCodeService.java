package org.dromara.qrcode.service;

import org.dromara.qrcode.domain.QrCodeResult;
import org.dromara.qrcode.domain.bo.FaceQrCodeBo;
import org.dromara.qrcode.domain.bo.QrRecordWriteBo;
import org.dromara.qrcode.domain.bo.QrVerifyBo;
import org.dromara.qrcode.domain.vo.QrVerifyVo;

/**
 * 二维码通行核心服务 (通用能力)
 *
 * <p>对应设计文档《二维码通行设计》服务端二维码服务:
 * generate / verify / revoke / record</p>
 *
 * @author qrcode
 */
public interface IQrCodeService {

    /**
     * 生成二维码 (加密+入库)
     *
     * @param bo 二维码业务对象
     * @return 生成结果 (含二维码内容, 可直接渲染图片)
     */
    QrCodeResult generate(FaceQrCodeBo bo);

    /**
     * 二维码在线比对 (设备调用: /api/v1/qrcode_recognition)
     *
     * @param bo 设备请求
     * @return 比对结果 (Result/Msg/Content 设备协议)
     */
    QrVerifyVo verify(QrVerifyBo bo);

    /**
     * 作废二维码
     *
     * @param qrId 二维码ID
     */
    void revoke(Long qrId);

    /**
     * 离线识别记录入库 (设备调用: /client_write_record)
     *
     * @param bo 设备请求
     */
    void record(QrRecordWriteBo bo);

}
