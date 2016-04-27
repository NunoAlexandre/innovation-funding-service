package com.worth.ifs.config;

import com.worth.ifs.util.ThymeleafUtil;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;

import java.util.HashMap;
import java.util.Map;

/**
 * Thymeleaf Dialect that registers new objects that can be used in OGNL or SpringEL expression evaluations like <tt>${#ifsUtil.doThis(obj)}</tt>
 */
public class IfSThymeleafDialect extends AbstractDialect implements IExpressionEnhancingDialect {

    @Override
    public String getPrefix() {
        // currently no attribute or tag processors, so this value doesn't matter yet.
        return "ifs";
    }

    @Override
    public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
        final Map<String, Object> expressionObjects = new HashMap<>();
        expressionObjects.put("ifsUtil", new ThymeleafUtil());
        return expressionObjects;
    }
}