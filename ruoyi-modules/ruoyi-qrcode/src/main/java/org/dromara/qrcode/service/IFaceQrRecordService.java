package org.dromara.qrcode.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.qrcode.domain.bo.FaceQrRecordBo;
import org.dromara.qrcode.domain.vo.FaceQrRecordVo;

import java.util.List;

/**
 * 二维码通行记录 服务层
 *
 * @author qrcode
 */
public interface IFaceQrRecordService {

    /**
     * 分页查询通行记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 记录分页列表
     */
    TableDataInfo<FaceQrRecordVo> selectPageQrRecordList(FaceQrRecordBo bo, PageQuery pageQuery);

    /**
     * 查询通行记录详情
     *
     * @param recordId 记录ID
     * @return 记录信息
     */
    FaceQrRecordVo selectQrRecordById(Long recordId);

    /**
     * 查询通行记录列表
     *
     * @param bo 查询条件
     * @return 记录集合
     */
    List<FaceQrRecordVo> selectQrRecordList(FaceQrRecordBo bo);

}
