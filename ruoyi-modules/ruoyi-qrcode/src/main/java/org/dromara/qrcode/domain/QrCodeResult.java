package org.dromara.qrcode.domain;

import lombok.Data;

/**
 * 二维码生成结果
 *
 * @author qrcode
 */
@Data
public class QrCodeResult {

    /**
     * 二维码ID
     */
    private Long qrId;

    /**
     * 二维码类型
     */
    private String qrType;

    /**
     * 生成的二维码内容 (可直接渲染为二维码图片)
     */
    private String content;

}
