package com.worth.ifs.commons.pojo;


import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.*;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import com.worth.ifs.address.domain.Address;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import com.worth.ifs.application.domain.AssessorFeedback;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.authentication.resource.CreateUserResource;
import com.worth.ifs.authentication.resource.UpdateUserResource;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.CostField;
import com.worth.ifs.finance.domain.CostValue;
import com.worth.ifs.form.domain.FormInputType;
import com.worth.ifs.form.domain.FormValidator;
import com.worth.ifs.invite.domain.Invite;
import com.worth.ifs.invite.domain.InviteOrganisation;
import com.worth.ifs.organisation.domain.OrganisationAddress;
import com.worth.ifs.token.domain.Token;
import com.worth.ifs.token.resource.TokenResource;
import com.worth.ifs.user.domain.OrganisationType;
import com.worth.ifs.workflow.resource.ProcessOutcomeResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class POJOTest {
    // Configured for expectation, so we know when a class gets added or removed.
    private static final int EXPECTED_RESOURCES = 28;

    // The package to test
    private static final String POJO_PACKAGE = "com.worth.ifs";

    private List<PojoClass> classes;
    private Validator validator;
    private List<Class<?>> classesToTest = Arrays.asList(
        Invite.class,
        Address.class,
        OrganisationType.class,
        Application.class,
        ApplicationStatus.class,
        FileEntry.class,
        CostField.class,
        FormValidator.class,
        FormInputType.class,
        OrganisationAddress.class,
        CostValue.class,
        Token.class,
        AssessorFeedback.class,
        InviteOrganisation.class,
        Section.class,
        ApplicationFinance.class,
        Competition.class,
        CreateUserResource.class,
        UpdateUserResource.class
    );

    @Before
    public void setup() {
        classes = PojoClassFactory.getPojoClassesRecursively(POJO_PACKAGE, new FilterPackages(classesToTest));

        validator = ValidatorBuilder.create()
            .with(
                new GetterMustExistRule(),
                new SetterMustExistRule(),
                new NoNestedClassRule(),
                new NoStaticExceptFinalRule(),
                new SerializableMustHaveSerialVersionUIDRule(),
                new NoFieldShadowingRule(),
                new NoPublicFieldsExceptStaticFinalRule(),
                new TestClassMustBeProperlyNamedRule()
            )
            .with(
                new SetterTester(),
                new GetterTester()
            )
            .build();


    }

    @Test
    public void ensureExpectedPojoCount() {
        Assert.assertEquals(String.format("Classes added / removed? %s => %s ", classesToTest.size()+EXPECTED_RESOURCES, classes.size()), classesToTest.size()+EXPECTED_RESOURCES, classes.size());
    }

    @Test
    public void testPojoStructureAndBehavior() {
        validator.validateRecursively(POJO_PACKAGE, new FilterPackages(classesToTest));
    }

    private static class FilterPackages implements  PojoClassFilter {
        private final List<Class<?>> classes;

        FilterPackages(List<Class<?>> classes){
            this.classes = classes;
        }

        @Override
        public boolean include(PojoClass pojoClass) {
            return !pojoClass.getClazz().equals(ProcessOutcomeResource.class)
                    && !pojoClass.getClazz().equals(Token.class)
                    && !pojoClass.getClazz().equals(TokenResource.class)
                    && (classes.stream().anyMatch(pojoClass.getClazz()::equals)|| pojoClass.getClazz().getName().endsWith("Resource"));
        }
    }
}
