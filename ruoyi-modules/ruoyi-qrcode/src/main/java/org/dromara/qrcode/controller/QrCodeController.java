package org.dromara.qrcode.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.qrcode.domain.QrCodeResult;
import org.dromara.qrcode.domain.bo.FaceQrCodeBo;
import org.dromara.qrcode.domain.bo.FaceQrRecordBo;
import org.dromara.qrcode.domain.vo.FaceQrCodeVo;
import org.dromara.qrcode.domain.vo.FaceQrRecordVo;
import org.dromara.qrcode.service.IFaceQrCodeService;
import org.dromara.qrcode.service.IFaceQrRecordService;
import org.dromara.qrcode.service.IQrCodeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 二维码通行管理 (通用能力)
 *
 * <p>管理端接口: 二维码的生成/查询/修改/删除/作废 及 通行记录查询</p>
 *
 * @author qrcode
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/qrcode")
public class QrCodeController extends BaseController {

    private final IFaceQrCodeService qrCodeService;
    private final IFaceQrRecordService qrRecordService;
    private final IQrCodeService coreQrCodeService;

    /**
     * 获取二维码分页列表
     */
    @SaCheckPermission("qrcode:list")
    @GetMapping("/list")
    public TableDataInfo<FaceQrCodeVo> list(FaceQrCodeBo bo, PageQuery pageQuery) {
        return qrCodeService.selectPageQrCodeList(bo, pageQuery);
    }

    /**
     * 获取二维码详细信息
     *
     * @param qrId 二维码ID
     */
    @SaCheckPermission("qrcode:query")
    @GetMapping("/{qrId}")
    public R<FaceQrCodeVo> getInfo(@PathVariable Long qrId) {
        return R.ok(qrCodeService.selectQrCodeById(qrId));
    }

    /**
     * 生成二维码 (加密+入库)
     */
    @SaCheckPermission("qrcode:add")
    @Log(title = "二维码通行", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<QrCodeResult> add(@Validated @RequestBody FaceQrCodeBo bo) {
        return R.ok(qrCodeService.insertQrCode(bo));
    }

    /**
     * 修改二维码 (基础字段)
     */
    @SaCheckPermission("qrcode:edit")
    @Log(title = "二维码通行", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody FaceQrCodeBo bo) {
        return toAjax(qrCodeService.updateQrCode(bo));
    }

    /**
     * 作废二维码
     *
     * @param qrId 二维码ID
     */
    @SaCheckPermission("qrcode:revoke")
    @Log(title = "二维码通行", businessType = BusinessType.UPDATE)
    @PutMapping("/revoke/{qrId}")
    public R<Void> revoke(@PathVariable Long qrId) {
        coreQrCodeService.revoke(qrId);
        return R.ok();
    }

    /**
     * 删除二维码
     *
     * @param qrIds 二维码ID串
     */
    @SaCheckPermission("qrcode:remove")
    @Log(title = "二维码通行", businessType = BusinessType.DELETE)
    @DeleteMapping("/{qrIds}")
    public R<Void> remove(@PathVariable Long[] qrIds) {
        return toAjax(qrCodeService.deleteQrCodeByIds(qrIds));
    }

    /**
     * 获取通行记录分页列表
     */
    @SaCheckPermission("qrcode:record:list")
    @GetMapping("/records/list")
    public TableDataInfo<FaceQrRecordVo> recordList(FaceQrRecordBo bo, PageQuery pageQuery) {
        return qrRecordService.selectPageQrRecordList(bo, pageQuery);
    }

    /**
     * 获取通行记录详细信息
     *
     * @param recordId 记录ID
     */
    @SaCheckPermission("qrcode:record:query")
    @GetMapping("/records/{recordId}")
    public R<FaceQrRecordVo> recordInfo(@PathVariable Long recordId) {
        return R.ok(qrRecordService.selectQrRecordById(recordId));
    }

}
