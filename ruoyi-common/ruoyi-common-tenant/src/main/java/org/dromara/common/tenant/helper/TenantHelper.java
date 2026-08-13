package org.dromara.common.tenant.helper;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.reflect.ReflectUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;

import java.time.Duration;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * 租户助手
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantHelper {

    private static final String DYNAMIC_TENANT_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "dynamicTenant";

    /**
     * 可见租户范围缓存key前缀（值为租户id列表：当前租户 + 所有下级租户）
     */
    private static final String TENANT_SCOPE_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "tenantScope:";

    private static final ThreadLocal<String> TEMP_DYNAMIC_TENANT = new ThreadLocal<>();

    private static final ThreadLocal<Stack<Integer>> REENTRANT_IGNORE = ThreadLocal.withInitial(Stack::new);

    /**
     * 租户功能是否启用
     */
    public static boolean isEnable() {
        return Convert.toBool(SpringUtils.getProperty("tenant.enable"), false);
    }

    private static IgnoreStrategy getIgnoreStrategy() {
        Object ignoreStrategyLocal = ReflectUtils.getStaticFieldValue(ReflectUtils.getField(InterceptorIgnoreHelper.class, "IGNORE_STRATEGY_LOCAL"));
        if (ignoreStrategyLocal instanceof ThreadLocal<?> IGNORE_STRATEGY_LOCAL) {
            if (IGNORE_STRATEGY_LOCAL.get() instanceof IgnoreStrategy ignoreStrategy) {
                return ignoreStrategy;
            }
        }
        return null;
    }

    /**
     * 开启忽略租户(开启后需手动调用 {@link #disableIgnore()} 关闭)
     */
    private static void enableIgnore() {
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        if (ObjectUtil.isNull(ignoreStrategy)) {
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().tenantLine(true).build());
        } else {
            ignoreStrategy.setTenantLine(true);
        }
        Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
        reentrantStack.push(reentrantStack.size() + 1);
    }

    /**
     * 关闭忽略租户
     */
    private static void disableIgnore() {
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        if (ObjectUtil.isNotNull(ignoreStrategy)) {
            boolean noOtherIgnoreStrategy = !Boolean.TRUE.equals(ignoreStrategy.getDynamicTableName())
                && !Boolean.TRUE.equals(ignoreStrategy.getBlockAttack())
                && !Boolean.TRUE.equals(ignoreStrategy.getIllegalSql())
                && !Boolean.TRUE.equals(ignoreStrategy.getDataPermission())
                && CollectionUtil.isEmpty(ignoreStrategy.getOthers());
            Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
            boolean empty = reentrantStack.isEmpty() || reentrantStack.pop() == 1;
            if (noOtherIgnoreStrategy && empty) {
                InterceptorIgnoreHelper.clearIgnoreStrategy();
            } else if (empty) {
                ignoreStrategy.setTenantLine(false);
            }
        }
    }

    /**
     * 在忽略租户中执行
     *
     * @param handle 处理执行方法
     */
    public static void ignore(Runnable handle) {
        enableIgnore();
        try {
            handle.run();
        } finally {
            disableIgnore();
        }
    }

    /**
     * 在忽略租户中执行
     *
     * @param handle 处理执行方法
     */
    public static <T> T ignore(Supplier<T> handle) {
        enableIgnore();
        try {
            return handle.get();
        } finally {
            disableIgnore();
        }
    }

    public static void setDynamic(String tenantId) {
        setDynamic(tenantId, false);
    }

    /**
     * 设置动态租户(一直有效 需要手动清理)
     * <p>
     * 如果为未登录状态下 那么只在当前线程内生效
     *
     * @param tenantId 租户id
     * @param global   是否全局生效
     */
    public static void setDynamic(String tenantId, boolean global) {
        if (!isEnable()) {
            return;
        }
        if (!LoginHelper.isLogin() || !global) {
            TEMP_DYNAMIC_TENANT.set(tenantId);
            return;
        }
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId();
        RedisUtils.setCacheObject(cacheKey, tenantId);
        SaHolder.getStorage().set(cacheKey, tenantId);
    }

    /**
     * 获取动态租户(一直有效 需要手动清理)
     * <p>
     * 如果为未登录状态下 那么只在当前线程内生效
     */
    public static String getDynamic() {
        if (!isEnable()) {
            return null;
        }
        if (!LoginHelper.isLogin()) {
            return TEMP_DYNAMIC_TENANT.get();
        }
        // 如果线程内有值 优先返回
        String tenantId = TEMP_DYNAMIC_TENANT.get();
        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId;
        }
        SaStorage storage = SaHolder.getStorage();
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId();
        tenantId = storage.getString(cacheKey);
        // 如果为 -1 说明已经查过redis并且不存在值 则直接返回null
        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId.equals("-1") ? null : tenantId;
        }
        tenantId = RedisUtils.getCacheObject(cacheKey);
        storage.set(cacheKey, StringUtils.isBlank(tenantId) ? "-1" : tenantId);
        return tenantId;
    }

    /**
     * 清除动态租户
     */
    public static void clearDynamic() {
        if (!isEnable()) {
            return;
        }
        if (!LoginHelper.isLogin()) {
            TEMP_DYNAMIC_TENANT.remove();
            return;
        }
        TEMP_DYNAMIC_TENANT.remove();
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId();
        RedisUtils.deleteObject(cacheKey);
        SaHolder.getStorage().delete(cacheKey);
    }

    /**
     * 在动态租户中执行
     *
     * @param handle 处理执行方法
     */
    public static void dynamic(String tenantId, Runnable handle) {
        setDynamic(tenantId);
        try {
            handle.run();
        } finally {
            clearDynamic();
        }
    }

    /**
     * 在动态租户中执行
     *
     * @param handle 处理执行方法
     */
    public static <T> T dynamic(String tenantId, Supplier<T> handle) {
        setDynamic(tenantId);
        try {
            return handle.get();
        } finally {
            clearDynamic();
        }
    }

    /**
     * 获取当前租户id(动态租户优先)
     */
    public static String getTenantId() {
        if (!isEnable()) {
            return null;
        }
        String tenantId = TenantHelper.getDynamic();
        if (StringUtils.isBlank(tenantId)) {
            tenantId = LoginHelper.getTenantId();
        }
        return tenantId;
    }

    /**
     * 获取当前可见租户id列表（当前租户 + 所有下级租户）
     * <p>
     * 用于多级代理租户：上级租户账号可查询/管理自己及全部下级租户的数据，
     * 数据范围由缓存中的租户树快照决定（登录或创建租户时刷新）。
     * 未开启租户、未登录或无缓存时，仅返回当前租户自身，避免越权。
     */
    public static List<String> getTenantScope() {
        if (!isEnable()) {
            return List.of();
        }
        String tenantId = getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            return List.of();
        }
        List<String> scope = RedisUtils.getCacheList(TENANT_SCOPE_KEY + tenantId);
        if (CollectionUtil.isEmpty(scope)) {
            // 兜底：无缓存时仅当前租户可见
            return List.of(tenantId);
        }
        return scope;
    }

    /**
     * 写入租户可见范围缓存（登录或租户树变更后调用）
     *
     * @param tenantId 租户id
     * @param scope    可见租户id列表（须包含自身）
     */
    public static void setTenantScope(String tenantId, List<String> scope) {
        if (!isEnable() || StringUtils.isBlank(tenantId) || CollectionUtil.isEmpty(scope)) {
            return;
        }
        String key = TENANT_SCOPE_KEY + tenantId;
        RedisUtils.deleteObject(key);
        RedisUtils.setCacheList(key, scope);
        RedisUtils.expire(key, Duration.ofDays(1));
    }

    /**
     * 清除指定租户的可见范围缓存（租户树变更后调用）
     */
    public static void clearTenantScope(String tenantId) {
        if (StringUtils.isNotBlank(tenantId)) {
            RedisUtils.deleteObject(TENANT_SCOPE_KEY + tenantId);
        }
    }

    /**
     * 清除所有租户的可见范围缓存
     */
    public static void clearAllTenantScope() {
        RedisUtils.deleteKeys(TENANT_SCOPE_KEY + "*");
    }

}
