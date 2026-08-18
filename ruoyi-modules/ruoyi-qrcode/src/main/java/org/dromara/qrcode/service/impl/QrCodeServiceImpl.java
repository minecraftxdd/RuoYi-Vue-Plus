package org.dromara.qrcode.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.qrcode.domain.FaceQrCode;
import org.dromara.qrcode.domain.FaceQrRecord;
import org.dromara.qrcode.domain.ParsedQrCode;
import org.dromara.qrcode.domain.QrCodeResult;
import org.dromara.qrcode.domain.bo.FaceQrCodeBo;
import org.dromara.qrcode.domain.bo.QrRecordWriteBo;
import org.dromara.qrcode.domain.bo.QrVerifyBo;
import org.dromara.qrcode.domain.vo.QrVerifyContentVo;
import org.dromara.qrcode.domain.vo.QrVerifyVo;
import org.dromara.qrcode.mapper.FaceQrCodeMapper;
import org.dromara.qrcode.mapper.FaceQrRecordMapper;
import org.dromara.qrcode.service.IQrCodeService;
import org.dromara.qrcode.utils.QrCodeCryptoUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 二维码通行核心服务实现
 *
 * @author qrcode
 */
@RequiredArgsConstructor
@Service
public class QrCodeServiceImpl implements IQrCodeService {

    private final FaceQrCodeMapper baseMapper;
    private final FaceQrRecordMapper qrRecordMapper;

    /**
     * 生成二维码 (加密+入库)
     */
    @Override
    public QrCodeResult generate(FaceQrCodeBo bo) {
        if (bo.getValidEnd() <= bo.getValidStart()) {
            throw new ServiceException("结束时间必须晚于开始时间");
        }
        if (bo.getTotalTimes() == null || bo.getTotalTimes() <= 0) {
            throw new ServiceException("有效次数必须大于0");
        }
        FaceQrCode qr = MapstructUtils.convert(bo, FaceQrCode.class);
        qr.setId(IdUtil.getSnowflakeNextId());
        qr.setUsedTimes(0);
        qr.setStatus("0");
        String sn = StringUtils.isBlank(bo.getSn()) ? QrCodeCryptoUtils.SN_WILDCARD : bo.getSn();
        String content = QrCodeCryptoUtils.generateContent(
            qr.getQrType(), qr.getValidStart(), qr.getValidEnd(), qr.getTotalTimes(),
            qr.getId(), qr.getProjectId(), sn, qr.getUserId());
        qr.setSn(sn);
        qr.setContent(content);
        baseMapper.insert(qr);
        QrCodeResult result = new QrCodeResult();
        result.setQrId(qr.getId());
        result.setQrType(qr.getQrType());
        result.setContent(content);
        return result;
    }

    /**
     * 二维码在线比对 (设备协议: 17.2.二维码-在线比对)
     */
    @Override
    public QrVerifyVo verify(QrVerifyBo bo) {
        QrVerifyVo resp = new QrVerifyVo();
        resp.setContent(new QrVerifyContentVo());
        // 1. 解析内容 + 校验数完整性
        ParsedQrCode parsed;
        try {
            parsed = QrCodeCryptoUtils.parse(bo.getQrcodeContent());
        } catch (ServiceException e) {
            return fail(resp, 1, "二维码内容无效");
        }
        if (!QrCodeCryptoUtils.verifyChecksum(parsed)) {
            return fail(resp, 1, "二维码内容无效");
        }
        // 2. 查库
        FaceQrCode qr = baseMapper.selectById(parsed.getQrId());
        if (qr == null) {
            return fail(resp, 1, "二维码内容无效");
        }
        long now = System.currentTimeMillis() / 1000;
        // 3. 作废/无效
        if ("1".equals(qr.getStatus())) {
            saveRecord(qr, bo, 1, "二维码已作废", 1);
            return fail(resp, 1, "二维码已作废");
        }
        // 4. 有效期
        if ("3".equals(qr.getStatus()) || now > qr.getValidEnd()) {
            saveRecord(qr, bo, 2, "二维码已过期", 1);
            return fail(resp, 2, "二维码已过期");
        }
        if (now < qr.getValidStart()) {
            saveRecord(qr, bo, 2, "二维码未到生效时间", 1);
            return fail(resp, 2, "二维码未到生效时间");
        }
        // 5. 次数
        if ("2".equals(qr.getStatus()) || qr.getUsedTimes() >= qr.getTotalTimes()) {
            saveRecord(qr, bo, 3, "超过有效次数", 1);
            return fail(resp, 3, "超过有效次数");
        }
        // 6. 设备绑定
        String qrSn = qr.getSn();
        if (StringUtils.isNotBlank(qrSn)
            && !QrCodeCryptoUtils.SN_WILDCARD.equalsIgnoreCase(qrSn)
            && !qrSn.equals(bo.getSn())) {
            saveRecord(qr, bo, 4, "二维码未绑定设备", 1);
            return fail(resp, 4, "二维码未绑定设备");
        }
        // 7. 项目一致
        if (!qr.getProjectId().equals(parsed.getProjectId())) {
            saveRecord(qr, bo, 5, "项目不一致", 1);
            return fail(resp, 5, "项目不一致");
        }
        // 8. 通过: 核销次数
        qr.setUsedTimes(qr.getUsedTimes() + 1);
        if (qr.getUsedTimes() >= qr.getTotalTimes()) {
            qr.setStatus("2");
        }
        baseMapper.updateById(qr);
        saveRecord(qr, bo, 0, null, 1);
        // 9. 返回
        QrVerifyContentVo content = resp.getContent();
        resp.setMsg("识别通过");
        if (StringUtils.isNotBlank(bo.getUserId())) {
            // 人脸+二维码模式: 返回 user_id, 设备做 1:1 人脸比对后放行
            resp.setResult(7);
            content.setUserId(StringUtils.isNotBlank(qr.getUserId()) ? qr.getUserId() : bo.getUserId());
            content.setVoiceCode(-2);
            content.setVoiceText("请面向屏幕进行人脸核验");
        } else {
            resp.setResult(0);
            content.setVoiceCode(-2);
            content.setVoiceText("识别通过");
        }
        return resp;
    }

    /**
     * 作废二维码
     */
    @Override
    public void revoke(Long qrId) {
        FaceQrCode qr = baseMapper.selectById(qrId);
        if (qr == null) {
            throw new ServiceException("二维码不存在");
        }
        if ("1".equals(qr.getStatus())) {
            throw new ServiceException("二维码已作废");
        }
        qr.setStatus("1");
        baseMapper.updateById(qr);
    }

    /**
     * 离线识别记录入库 (设备协议: 17.4.二维码离线识别记录)
     */
    @Override
    public void record(QrRecordWriteBo bo) {
        List<Long> qrIds = bo.getQrcodeId();
        for (int i = 0; i < qrIds.size(); i++) {
            Long qrId = qrIds.get(i);
            FaceQrCode qr = baseMapper.selectById(qrId);
            FaceQrRecord record = new FaceQrRecord();
            record.setQrId(qrId);
            if (qr != null) {
                record.setQrType(qr.getQrType());
                record.setPersonId(qr.getPersonId());
                record.setProjectId(qr.getProjectId());
                record.setQrCode(qr.getContent());
                // 同步核销次数
                qr.setUsedTimes(qr.getUsedTimes() + 1);
                if (qr.getUsedTimes() >= qr.getTotalTimes()) {
                    qr.setStatus("2");
                }
                baseMapper.updateById(qr);
            }
            record.setSn(bo.getSn());
            if (bo.getBodyTemperature() != null) {
                record.setBodyTemperature(BigDecimal.valueOf(bo.getBodyTemperature()));
            }
            record.setPhoto(bo.getPhoto());
            if (CollUtil.isNotEmpty(bo.getCreateTime()) && bo.getCreateTime().size() > i && bo.getCreateTime().get(i) != null) {
                record.setRecogTime(new Date(bo.getCreateTime().get(i) * 1000));
            }
            if (CollUtil.isNotEmpty(bo.getPanoramicPicture()) && bo.getPanoramicPicture().size() > i) {
                record.setPanoramicPicture(bo.getPanoramicPicture().get(i));
            }
            if (CollUtil.isNotEmpty(bo.getHealthCodeColor()) && bo.getHealthCodeColor().size() > i) {
                record.setHealthCodeColor(bo.getHealthCodeColor().get(i));
            }
            if (CollUtil.isNotEmpty(bo.getHealthCodePicture()) && bo.getHealthCodePicture().size() > i) {
                record.setHealthCodePicture(bo.getHealthCodePicture().get(i));
            }
            record.setPassStatus(0);
            record.setResultCode(0);
            record.setSource(2);
            qrRecordMapper.insert(record);
        }
    }

    /**
     * 拦截响应
     */
    private QrVerifyVo fail(QrVerifyVo resp, int code, String msg) {
        QrVerifyContentVo content = resp.getContent();
        if (content == null) {
            content = new QrVerifyContentVo();
            resp.setContent(content);
        }
        // Result 6 时 voice_code/voice_text 必须不为空
        content.setVoiceCode(-2);
        content.setVoiceText(msg);
        resp.setResult(code);
        resp.setMsg(msg);
        return resp;
    }

    /**
     * 保存通行/拦截记录
     */
    private void saveRecord(FaceQrCode qr, QrVerifyBo bo, int resultCode, String failReason, int source) {
        FaceQrRecord record = new FaceQrRecord();
        record.setQrId(qr.getId());
        record.setQrType(qr.getQrType());
        record.setPersonId(qr.getPersonId());
        record.setProjectId(qr.getProjectId());
        record.setSn(bo.getSn());
        record.setPassStatus(resultCode == 0 ? 0 : 1);
        record.setResultCode(resultCode);
        record.setFailReason(failReason);
        if (bo.getTemperature() != null) {
            record.setBodyTemperature(BigDecimal.valueOf(bo.getTemperature()));
        }
        record.setQrCode(qr.getContent());
        if (StringUtils.isNotBlank(bo.getRecogTime())) {
            try {
                record.setRecogTime(DateUtil.parse(bo.getRecogTime()));
            } catch (Exception ignored) {
                record.setRecogTime(new Date());
            }
        } else {
            record.setRecogTime(new Date());
        }
        record.setSource(source);
        qrRecordMapper.insert(record);
    }

}
