package org.dromara.qrcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.qrcode.domain.FaceQrCode;
import org.dromara.qrcode.domain.QrCodeResult;
import org.dromara.qrcode.domain.bo.FaceQrCodeBo;
import org.dromara.qrcode.domain.vo.FaceQrCodeVo;
import org.dromara.qrcode.mapper.FaceQrCodeMapper;
import org.dromara.qrcode.service.IFaceQrCodeService;
import org.dromara.qrcode.service.IQrCodeService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 二维码通行 服务层实现
 *
 * @author qrcode
 */
@RequiredArgsConstructor
@Service
public class FaceQrCodeServiceImpl implements IFaceQrCodeService {

    private final FaceQrCodeMapper baseMapper;
    private final IQrCodeService qrCodeService;

    /**
     * 分页查询二维码列表
     */
    @Override
    public TableDataInfo<FaceQrCodeVo> selectPageQrCodeList(FaceQrCodeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FaceQrCode> lqw = buildQueryWrapper(bo);
        Page<FaceQrCodeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        page.getRecords().forEach(this::fillTimeField);
        return TableDataInfo.build(page);
    }

    /**
     * 查询二维码详情
     */
    @Override
    public FaceQrCodeVo selectQrCodeById(Long qrId) {
        FaceQrCodeVo vo = baseMapper.selectVoById(qrId);
        fillTimeField(vo);
        return vo;
    }

    /**
     * 查询二维码列表
     */
    @Override
    public List<FaceQrCodeVo> selectQrCodeList(FaceQrCodeBo bo) {
        LambdaQueryWrapper<FaceQrCode> lqw = buildQueryWrapper(bo);
        List<FaceQrCodeVo> list = baseMapper.selectVoList(lqw);
        list.forEach(this::fillTimeField);
        return list;
    }

    /**
     * 新增二维码 (内部调用核心服务 generate)
     */
    @Override
    public QrCodeResult insertQrCode(FaceQrCodeBo bo) {
        return qrCodeService.generate(bo);
    }

    /**
     * 修改二维码 (基础字段, 不重新生成内容)
     */
    @Override
    public int updateQrCode(FaceQrCodeBo bo) {
        if (bo.getId() == null) {
            return 0;
        }
        FaceQrCode qr = MapstructUtils.convert(bo, FaceQrCode.class);
        // 内容与次数由服务端管控, 禁止直接修改
        qr.setContent(null);
        qr.setUsedTimes(null);
        qr.setStatus(null);
        return baseMapper.updateById(qr);
    }

    /**
     * 删除二维码
     */
    @Override
    public int deleteQrCodeById(Long qrId) {
        return baseMapper.deleteById(qrId);
    }

    /**
     * 批量删除二维码
     */
    @Override
    public int deleteQrCodeByIds(Long[] qrIds) {
        return baseMapper.deleteByIds(Arrays.asList(qrIds));
    }

    private LambdaQueryWrapper<FaceQrCode> buildQueryWrapper(FaceQrCodeBo bo) {
        LambdaQueryWrapper<FaceQrCode> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getQrType()), FaceQrCode::getQrType, bo.getQrType());
        lqw.eq(bo.getProjectId() != null, FaceQrCode::getProjectId, bo.getProjectId());
        lqw.eq(bo.getPersonId() != null, FaceQrCode::getPersonId, bo.getPersonId());
        lqw.eq(StringUtils.isNotBlank(bo.getSn()), FaceQrCode::getSn, bo.getSn());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), FaceQrCode::getStatus, bo.getStatus());
        lqw.orderByDesc(FaceQrCode::getCreateTime);
        return lqw;
    }

    private void fillTimeField(FaceQrCodeVo vo) {
        if (vo == null) {
            return;
        }
        if (vo.getValidStart() != null) {
            vo.setValidStartTime(new Date(vo.getValidStart() * 1000));
        }
        if (vo.getValidEnd() != null) {
            vo.setValidEndTime(new Date(vo.getValidEnd() * 1000));
        }
    }

}
