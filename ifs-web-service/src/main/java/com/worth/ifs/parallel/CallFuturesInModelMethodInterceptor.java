package com.worth.ifs.parallel;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import static com.worth.ifs.application.service.Futures.callAllFutures;

@Component
public class CallFuturesInModelMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CallFuturesInModelMethodInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object proceed = invocation.proceed();
        for (Object argument : invocation.getArguments()) {
            if (argument instanceof Model) {
                callAllFutures((Model) argument);
            }
        }
        return proceed;
    }
}