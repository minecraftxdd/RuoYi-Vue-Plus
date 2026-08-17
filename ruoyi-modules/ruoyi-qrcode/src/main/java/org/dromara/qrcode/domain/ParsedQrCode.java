package org.dromara.qrcode.domain;

import lombok.Data;

/**
 * 二维码内容解析结果 (设备协议: 17.3.二维码-离线识别生成规则)
 *
 * @author qrcode
 */
@Data
public class ParsedQrCode {

    /**
     * 用户类型: A#业主/用户 B#访客
     */
    private String qrType;

    /**
     * 校验数 (36进制)
     */
    private long checksum;

    /**
     * 开始时间戳 (秒)
     */
    private long validStart;

    /**
     * 结束时间戳 (秒)
     */
    private long validEnd;

    /**
     * 有效次数
     */
    private int totalTimes;

    /**
     * 二维码ID
     */
    private long qrId;

    /**
     * 项目ID
     */
    private long projectId;

    /**
     * 设备序列号 (字符串"null"表示通配)
     */
    private String sn;

    /**
     * 用户id (人脸+二维码模式 A#必填)
     */
    private String userId;

}
