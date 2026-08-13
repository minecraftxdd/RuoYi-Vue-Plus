package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.encrypt.annotation.ApiEncrypt;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.SysTenantBo;
import org.dromara.system.domain.vo.SysTenantVo;
import org.dromara.system.service.ISysTenantService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理
 *
 * @author Michelle.Chung
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/tenant")
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class SysTenantController extends BaseController {

    private final ISysTenantService tenantService;

    /**
     * 查询租户列表
     * <p>
     * 平台超管查看全部租户，租户管理员仅查看自己及下级租户（多级代理）
     */
    @SaCheckPermission(value = {"system:tenant:list", "system:tenantChild:list"}, mode = SaMode.OR)
    @GetMapping("/list")
    public TableDataInfo<SysTenantVo> list(SysTenantBo bo, PageQuery pageQuery) {
        return tenantService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询当前账号可见的租户列表（自己 + 全部下级租户，供前端下拉/树使用）
     */
    @SaCheckPermission(value = {"system:tenant:list", "system:tenantChild:list"}, mode = SaMode.OR)
    @GetMapping("/scope")
    public R<List<SysTenantVo>> scope() {
        return R.ok(tenantService.queryTenantScope());
    }

    /**
     * 导出租户列表
     */
    @SaCheckPermission(value = {"system:tenant:export", "system:tenantChild:list"}, mode = SaMode.OR)
    @Log(title = "租户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SysTenantBo bo, HttpServletResponse response) {
        List<SysTenantVo> list = tenantService.queryList(bo);
        ExcelUtil.exportExcel(list, "租户", SysTenantVo.class, response);
    }

    /**
     * 获取租户详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission(value = {"system:tenant:query", "system:tenantChild:list"}, mode = SaMode.OR)
    @GetMapping("/{id}")
    public R<SysTenantVo> getInfo(@NotNull(message = "主键不能为空")
                                  @PathVariable Long id) {
        return R.ok(tenantService.queryById(id));
    }

    /**
     * 新增租户
     * <p>
     * 平台超管新增顶层租户；租户管理员新增自己的下级租户（多级代理）
     */
    @ApiEncrypt
    @SaCheckPermission(value = {"system:tenant:add", "system:tenantChild:add"}, mode = SaMode.OR)
    @Log(title = "租户管理", businessType = BusinessType.INSERT)
    @Lock4j
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysTenantBo bo) {
        if (!tenantService.checkCompanyNameUnique(bo)) {
            return R.fail("新增租户'" + bo.getCompanyName() + "'失败，企业名称已存在");
        }
        return toAjax(TenantHelper.ignore(() -> tenantService.insertByBo(bo)));
    }

    /**
     * 修改租户
     */
    @SaCheckPermission(value = {"system:tenant:edit", "system:tenantChild:edit"}, mode = SaMode.OR)
    @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysTenantBo bo) {
        tenantService.checkTenantAllowed(bo.getTenantId());
        if (!tenantService.checkCompanyNameUnique(bo)) {
            return R.fail("修改租户'" + bo.getCompanyName() + "'失败，公司名称已存在");
        }
        return toAjax(tenantService.updateByBo(bo));
    }

    /**
     * 状态修改
     */
    @SaCheckPermission(value = {"system:tenant:edit", "system:tenantChild:edit"}, mode = SaMode.OR)
    @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysTenantBo bo) {
        tenantService.checkTenantAllowed(bo.getTenantId());
        return toAjax(tenantService.updateTenantStatus(bo));
    }

    /**
     * 删除租户
     *
     * @param ids 主键串
     */
    @SaCheckPermission(value = {"system:tenant:remove", "system:tenantChild:remove"}, mode = SaMode.OR)
    @Log(title = "租户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(tenantService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 动态切换租户
     *
     * @param tenantId 租户ID
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @GetMapping("/dynamic/{tenantId}")
    public R<Void> dynamicTenant(@NotBlank(message = "租户ID不能为空") @PathVariable String tenantId) {
        TenantHelper.setDynamic(tenantId, true);
        return R.ok();
    }

    /**
     * 清除动态租户
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @GetMapping("/dynamic/clear")
    public R<Void> dynamicClear() {
        TenantHelper.clearDynamic();
        return R.ok();
    }


    /**
     * 同步租户套餐
     *
     * @param tenantId  租户id
     * @param packageId 套餐id
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:tenant:edit")
    @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    @Lock4j
    @GetMapping("/syncTenantPackage")
    public R<Void> syncTenantPackage(@NotBlank(message = "租户ID不能为空") String tenantId,
                                     @NotNull(message = "套餐ID不能为空") Long packageId) {
        return toAjax(TenantHelper.ignore(() -> tenantService.syncTenantPackage(tenantId, packageId)));
    }

    /**
     * 同步租户字典
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @Log(title = "租户管理", businessType = BusinessType.INSERT)
    @Lock4j
    @GetMapping("/syncTenantDict")
    public R<Void> syncTenantDict() {
        if (!TenantHelper.isEnable()) {
            return R.fail("当前未开启租户模式");
        }
        tenantService.syncTenantDict();
        return R.ok("同步租户字典成功");
    }

    /**
     * 同步租户参数配置
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @Log(title = "租户管理", businessType = BusinessType.INSERT)
    @Lock4j
    @GetMapping("/syncTenantConfig")
    public R<Void> syncTenantConfig() {
        if (!TenantHelper.isEnable()) {
            return R.fail("当前未开启租户模式");
        }
        tenantService.syncTenantConfig();
        return R.ok("同步租户参数配置成功");
    }

}
