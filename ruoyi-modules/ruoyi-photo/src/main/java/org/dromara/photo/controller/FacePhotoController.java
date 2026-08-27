package org.dromara.photo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.photo.domain.bo.FacePhotoBo;
import org.dromara.photo.domain.vo.FacePhotoEncodeLogVo;
import org.dromara.photo.domain.vo.FacePhotoVo;
import org.dromara.photo.service.IFacePhotoService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 人脸照片管理 (通用能力)
 *
 * @author photo
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/photo")
public class FacePhotoController extends BaseController {

    private final IFacePhotoService photoService;

    /**
     * 查询照片列表
     */
    @SaCheckPermission("photo:list")
    @GetMapping("/list")
    public TableDataInfo<FacePhotoVo> list(FacePhotoBo bo, PageQuery pageQuery) {
        return photoService.selectPagePhotoList(bo, pageQuery);
    }

    /**
     * 查询照片详情
     */
    @SaCheckPermission("photo:query")
    @GetMapping("/{id}")
    public R<FacePhotoVo> getInfo(@PathVariable Long id) {
        return R.ok(photoService.selectPhotoById(id));
    }

    /**
     * 上传照片
     */
    @SaCheckPermission("photo:upload")
    @Log(title = "人脸照片", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FacePhotoVo> upload(@RequestPart("file") MultipartFile file) {
        FacePhotoVo vo = photoService.upload(file);
        return R.ok(vo);
    }

    /**
     * 触发照片编码
     */
    @SaCheckPermission("photo:encode")
    @Log(title = "人脸照片编码", businessType = BusinessType.INSERT)
    @PostMapping("/encode/{id}")
    public R<Void> encode(@PathVariable Long id) {
        photoService.encodeFace(id);
        return R.ok();
    }

    /**
     * 查询照片编码状态
     */
    @SaCheckPermission("photo:query")
    @GetMapping("/encoding/{id}")
    public R<FacePhotoVo> getEncodingStatus(@PathVariable Long id) {
        return R.ok(photoService.getEncodingStatus(id));
    }

    /**
     * 删除照片
     */
    @SaCheckPermission("photo:remove")
    @Log(title = "人脸照片", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(photoService.deletePhotoByIds(ids));
    }

    /**
     * 查询编码日志列表
     */
    @SaCheckPermission("photo:log:list")
    @GetMapping("/log")
    public R<List<FacePhotoEncodeLogVo>> getLogList(@RequestParam(required = false) Long photoId) {
        return R.ok(photoService.selectEncodeLogList(photoId));
    }

    /**
     * 删除编码日志
     */
    @SaCheckPermission("photo:log:remove")
    @Log(title = "人脸照片编码日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/log/{ids}")
    public R<Void> removeEncodeLogs(@PathVariable Long[] ids) {
        return toAjax(photoService.deleteEncodeLogByIds(ids));
    }

    /**
     * 获取照片编码数据
     */
    @SaCheckPermission("photo:query")
    @GetMapping("/encoding-data/{id}")
    public R<String> getEncodingData(@PathVariable Long id) {
        return R.ok(photoService.getEncodingData(id));
    }

}
