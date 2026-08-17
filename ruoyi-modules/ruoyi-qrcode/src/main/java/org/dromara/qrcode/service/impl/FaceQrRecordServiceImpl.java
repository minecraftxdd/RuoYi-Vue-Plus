package org.dromara.qrcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.qrcode.domain.FaceQrRecord;
import org.dromara.qrcode.domain.bo.FaceQrRecordBo;
import org.dromara.qrcode.domain.vo.FaceQrRecordVo;
import org.dromara.qrcode.mapper.FaceQrRecordMapper;
import org.dromara.qrcode.service.IFaceQrRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 二维码通行记录 服务层实现
 *
 * @author qrcode
 */
@RequiredArgsConstructor
@Service
public class FaceQrRecordServiceImpl implements IFaceQrRecordService {

    private final FaceQrRecordMapper baseMapper;

    /**
     * 分页查询通行记录列表
     */
    @Override
    public TableDataInfo<FaceQrRecordVo> selectPageQrRecordList(FaceQrRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FaceQrRecord> lqw = buildQueryWrapper(bo);
        Page<FaceQrRecordVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 查询通行记录详情
     */
    @Override
    public FaceQrRecordVo selectQrRecordById(Long recordId) {
        return baseMapper.selectVoById(recordId);
    }

    /**
     * 查询通行记录列表
     */
    @Override
    public List<FaceQrRecordVo> selectQrRecordList(FaceQrRecordBo bo) {
        LambdaQueryWrapper<FaceQrRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<FaceQrRecord> buildQueryWrapper(FaceQrRecordBo bo) {
        LambdaQueryWrapper<FaceQrRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getQrId() != null, FaceQrRecord::getQrId, bo.getQrId());
        lqw.eq(bo.getPersonId() != null, FaceQrRecord::getPersonId, bo.getPersonId());
        lqw.eq(StringUtils.isNotBlank(bo.getSn()), FaceQrRecord::getSn, bo.getSn());
        lqw.eq(bo.getPassStatus() != null, FaceQrRecord::getPassStatus, bo.getPassStatus());
        lqw.eq(bo.getSource() != null, FaceQrRecord::getSource, bo.getSource());
        lqw.eq(StringUtils.isNotBlank(bo.getQrType()), FaceQrRecord::getQrType, bo.getQrType());
        lqw.orderByDesc(FaceQrRecord::getCreateTime);
        return lqw;
    }

}
