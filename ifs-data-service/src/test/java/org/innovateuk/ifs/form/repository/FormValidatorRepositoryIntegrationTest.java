package org.innovateuk.ifs.form.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.form.domain.FormValidator;
import org.innovateuk.ifs.validator.EmailValidator;
import org.innovateuk.ifs.validator.NotEmptyValidator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Repository Integration tests for Form Inputs.
 */
public class FormValidatorRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<FormValidatorRepository> {

    @Autowired
    private FormValidatorRepository repository;

    @Override
    @Autowired
    protected void setRepository(FormValidatorRepository repository) {
        this.repository = repository;
    }

    @Test
    public void test_findByIdEmailValidator() {
        Long id= 1L;
        FormValidator validator = repository.findById(id);
        assertEquals(id, validator.getId());
        assertEquals(EmailValidator.class.getName(), validator.getClazzName());
        try {
            assertEquals(EmailValidator.class, validator.getClazz());
        } catch (ClassNotFoundException e) {
            assertFalse("ClassNotFoundException " + validator.getClazzName(), true);
        }
    }

    @Test
    public void test_findByIdNotEmptyValidator() {
        Long id= 2L;
        FormValidator validator = repository.findById(id);
        assertEquals(id, validator.getId());
        assertEquals(NotEmptyValidator.class.getName(), validator.getClazzName());
        try {
            assertEquals(NotEmptyValidator.class, validator.getClazz());
        } catch (ClassNotFoundException e) {
            assertFalse("ClassNotFoundException " + validator.getClazzName(), true);
        }
    }


    @Test
    public void test_findById_nonExistentInput() {
        assertEquals(null, repository.findById(Long.MAX_VALUE));
    }


}
