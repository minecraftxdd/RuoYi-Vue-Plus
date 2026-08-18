package org.dromara.qrcode.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.qrcode.domain.QrCodeResult;
import org.dromara.qrcode.domain.bo.FaceQrCodeBo;
import org.dromara.qrcode.domain.vo.FaceQrCodeVo;

import java.util.List;

/**
 * 二维码通行 服务层
 *
 * @author qrcode
 */
public interface IFaceQrCodeService {

    /**
     * 分页查询二维码列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 二维码分页列表
     */
    TableDataInfo<FaceQrCodeVo> selectPageQrCodeList(FaceQrCodeBo bo, PageQuery pageQuery);

    /**
     * 查询二维码详情
     *
     * @param qrId 二维码ID
     * @return 二维码信息
     */
    FaceQrCodeVo selectQrCodeById(Long qrId);

    /**
     * 查询二维码列表
     *
     * @param bo 二维码信息
     * @return 二维码集合
     */
    List<FaceQrCodeVo> selectQrCodeList(FaceQrCodeBo bo);

    /**
     * 新增二维码 (内部调用核心服务 generate)
     *
     * @param bo 二维码信息
     * @return 生成结果 (含二维码ID与内容)
     */
    QrCodeResult insertQrCode(FaceQrCodeBo bo);

    /**
     * 修改二维码
     *
     * @param bo 二维码信息
     * @return 结果
     */
    int updateQrCode(FaceQrCodeBo bo);

    /**
     * 删除二维码
     *
     * @param qrId 二维码ID
     * @return 结果
     */
    int deleteQrCodeById(Long qrId);

    /**
     * 批量删除二维码
     *
     * @param qrIds 需要删除的二维码ID
     * @return 结果
     */
    int deleteQrCodeByIds(Long[] qrIds);

}
