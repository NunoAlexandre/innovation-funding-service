package org.innovateuk.ifs.assessment.viewmodel.profile;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDate;

/**
 * Holder of model attributes for the Declaration of Interests Edit view.
 */
public class AssessorProfileEditDeclarationViewModel {

    private LocalDate declarationDate;

    public AssessorProfileEditDeclarationViewModel(LocalDate declarationDate) {
        this.declarationDate = declarationDate;
    }

    public LocalDate getDeclarationDate() {
        return declarationDate;
    }

    public void setDeclarationDate(LocalDate declarationDate) {
        this.declarationDate = declarationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AssessorProfileEditDeclarationViewModel that = (AssessorProfileEditDeclarationViewModel) o;

        return new EqualsBuilder()
                .append(declarationDate, that.declarationDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(declarationDate)
                .toHashCode();
    }
}
