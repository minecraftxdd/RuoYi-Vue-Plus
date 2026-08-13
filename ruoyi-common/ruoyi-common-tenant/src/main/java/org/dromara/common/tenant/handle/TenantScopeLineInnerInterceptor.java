package org.dromara.common.tenant.handle;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.dromara.common.tenant.helper.TenantHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 多级代理租户拦截器
 * <p>
 * 查询/更新/删除时按"可见租户范围"（当前租户 + 全部下级租户）过滤数据，
 * 使上级租户可以查看并管理自己及所有下级租户的数据；
 * 插入时仍由 {@link TenantLineHandler#getTenantId()} 提供当前租户单值，防止跨租户写入。
 *
 * @author Lion Li
 */
public class TenantScopeLineInnerInterceptor extends TenantLineInnerInterceptor {

    public TenantScopeLineInnerInterceptor(TenantLineHandler tenantLineHandler) {
        super(tenantLineHandler);
    }

    /**
     * 构建单张表的租户过滤条件
     * <p>
     * 默认实现为 {@code tenant_id = getTenantId()}，这里扩展为：
     * 范围仅一个租户时保持 {@code tenant_id = 'xxx'}；
     * 范围含多个租户时生成 {@code tenant_id IN ('xxx', 'yyy', ...)}。
     */
    @Override
    public Expression buildTableExpression(Table table, Expression where, String whereString) {
        TenantLineHandler handler = getTenantLineHandler();
        if (handler.ignoreTable(table.getName())) {
            return null;
        }
        Column column = getAliasColumn(table);
        List<String> scope = TenantHelper.getTenantScope();
        if (scope.isEmpty()) {
            // 无可见租户（异常兜底），构建恒不成立条件，避免越权查出数据
            return new EqualsTo(column, new NullValue());
        }
        if (scope.size() == 1) {
            return new EqualsTo(column, new StringValue(scope.get(0)));
        }
        List<Expression> items = scope.stream().map(StringValue::new).collect(Collectors.toList());
        return new InExpression(column, new ExpressionList(items));
    }

}
