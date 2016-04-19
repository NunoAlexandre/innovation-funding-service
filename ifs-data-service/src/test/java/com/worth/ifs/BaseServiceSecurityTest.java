package com.worth.ifs;

import com.worth.ifs.security.PermissionRule;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Arrays.asList;

/**
 * A base class for testing services with Spring Security integrated into them.  PermissionRules-annotated beans are
 * made available as mocks so that we can test the effects of calling service methods against the PermissionRule methods
 * that are available.
 * <p>
 * Subclasses of this base class are therefore able to test the security annotations of their various methods by verifying
 * that individual PermissionRule methods are being called (on their owning mocks)
 */
public abstract class BaseServiceSecurityTest<T> extends BaseMockSecurityTest {

    protected T service;

    /**
     * @return the service class under test.  Note that in order for Spring Security to be able to read parameter-name
     * information from expressions like @PreAuthorize("hasPermission(#feedback, 'UPDATE')"), we cannot provide a
     * Mockito mock of type T, as Spring Security is unable to infer which parameter is called "feedback", in this example.
     * <p>
     * Therefore we need to just create a very simple implementation of T.
     */
    protected abstract Class<? extends T> getServiceClass();

    private List<String> recordedRuleInteractions = new ArrayList<>();

    /**
     * Register a temporary bean definition for the class under test (as provided by getServiceClass()), and replace
     * all PermissionRules with mocks that can be looked up with getMockPermissionRulesBean().
     */
    @Before
    public void setup() {

        applicationContext.registerBeanDefinition("beanUndergoingSecurityTesting", new RootBeanDefinition(getServiceClass()));
        T serviceBeanWithSpringSecurity = (T) applicationContext.getBean("beanUndergoingSecurityTesting");
        service = createRecordingProxy(serviceBeanWithSpringSecurity, getServiceClass(),
                method -> hasOneAnnotation(method, PreAuthorize.class, PostAuthorize.class, PreFilter.class, PostFilter.class),
                methodCalled -> recordedRuleInteractions.add("SERVICE CALL: " + getServiceClass().getSimpleName() + "." + methodCalled.getName()));

        super.setup();
    }

    @After
    public void teardown() {
        applicationContext.removeBeanDefinition("beanUndergoingSecurityTesting");
        recordedRuleInteractions.forEach(interaction -> System.out.println(interaction));
        recordedRuleInteractions.clear();
        super.teardown();
    }

    @Override
    protected Object createPermissionRuleMock(Object mock, Class<?> mockClass) {
        return createRecordingProxy(mock, mockClass,
                method -> hasOneAnnotation(method, PermissionRule.class),
                methodCalled -> recordedRuleInteractions.add("  RULE CALL: " + mockClass.getSimpleName() + "." + methodCalled.getName()));
    }

    /**
     * Create a pass-through proxy that is able to record interactions with mock objects in a similar way that Mockito does
     * (Mockito does not expose its recordings and so it is necessary to do this manually if you want a list of interactions available)
     *
     * @param instance
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T createRecordingProxy(Object instance, Class<T> clazz, Predicate<Method> proxyMethodFilter, Consumer<Method> methodCallHandler) {

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);
        factory.setFilter(proxyMethodFilter::test);

        MethodHandler handler = (self, thisMethod, proceed, args) -> {
            Method originalMethod = instance.getClass().getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
            methodCallHandler.accept(originalMethod);
            try {
                return originalMethod.invoke(instance, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };

        try {
            return (T) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasOneAnnotation(Method method, Class<? extends Annotation>... annotations) {
        return asList(annotations).stream().anyMatch(annotation -> AnnotationUtils.findAnnotation(method, annotation) != null);
    }
}
