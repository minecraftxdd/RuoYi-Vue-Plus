package org.dromara.qrcode.utils;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.qrcode.domain.ParsedQrCode;

/**
 * 二维码内容编解码工具 (设备协议: 17.3.二维码-离线识别生成规则)
 *
 * <p>字段拼接: 用户类型&amp;校验数&amp;开始时间戳&amp;结束时间戳&amp;有效次数&amp;二维码ID&amp;项目ID&amp;设备序列号&amp;用户id</p>
 * <ul>
 *   <li>校验数 = (开始时间戳 + 结束时间戳 + 有效次数 + 二维码ID + 项目ID) + 6 再 XOR 9</li>
 *   <li>所有十进制数(含校验数)转为36进制</li>
 * </ul>
 *
 * @author qrcode
 */
public final class QrCodeCryptoUtils {

    /**
     * 通配所有设备的设备序列号
     */
    public static final String SN_WILDCARD = "null";

    private QrCodeCryptoUtils() {
    }

    /**
     * 生成二维码内容
     *
     * @param qrType     用户类型: A#业主/用户 B#访客
     * @param validStart 开始时间戳 (秒)
     * @param validEnd   结束时间戳 (秒)
     * @param totalTimes 有效次数
     * @param qrId       二维码ID
     * @param projectId  项目ID
     * @param sn         设备序列号, 为空时使用 "null" 通配
     * @param userId     用户id (可为空)
     * @return 二维码内容
     */
    public static String generateContent(String qrType, long validStart, long validEnd,
                                         int totalTimes, long qrId, long projectId,
                                         String sn, String userId) {
        long checksum = (validStart + validEnd + totalTimes + qrId + projectId + 6) ^ 9;
        String snValue = StringUtils.isBlank(sn) ? SN_WILDCARD : sn;
        String userIdValue = StringUtils.isBlank(userId) ? "" : userId;
        String qrTypeValue = qrType.endsWith("#") ? qrType : qrType + "#";
        return qrTypeValue + "&"
            + Long.toString(checksum, 36) + "&"
            + Long.toString(validStart, 36) + "&"
            + Long.toString(validEnd, 36) + "&"
            + Integer.toString(totalTimes, 36) + "&"
            + Long.toString(qrId, 36) + "&"
            + Long.toString(projectId, 36) + "&"
            + snValue + "&"
            + userIdValue;
    }

    /**
     * 解析二维码内容
     *
     * @param content 二维码内容
     * @return 解析结果
     */
    public static ParsedQrCode parse(String content) {
        if (StringUtils.isBlank(content)) {
            throw new ServiceException("二维码内容为空");
        }
        String[] parts = content.split("&");
        if (parts.length < 8) {
            throw new ServiceException("二维码内容格式错误");
        }
        try {
            ParsedQrCode parsed = new ParsedQrCode();
            parsed.setQrType(parts[0]);
            parsed.setChecksum(Long.parseLong(parts[1], 36));
            parsed.setValidStart(Long.parseLong(parts[2], 36));
            parsed.setValidEnd(Long.parseLong(parts[3], 36));
            parsed.setTotalTimes(Integer.parseInt(parts[4], 36));
            parsed.setQrId(Long.parseLong(parts[5], 36));
            parsed.setProjectId(Long.parseLong(parts[6], 36));
            parsed.setSn(parts[7]);
            if (parts.length > 8) {
                parsed.setUserId(parts[8]);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new ServiceException("二维码内容格式错误");
        }
    }

    /**
     * 校验二维码内容完整性
     *
     * @param parsed 解析结果
     * @return true 校验通过
     */
    public static boolean verifyChecksum(ParsedQrCode parsed) {
        long expected = (parsed.getValidStart() + parsed.getValidEnd() + parsed.getTotalTimes()
            + parsed.getQrId() + parsed.getProjectId() + 6) ^ 9;
        return expected == parsed.getChecksum();
    }

}
