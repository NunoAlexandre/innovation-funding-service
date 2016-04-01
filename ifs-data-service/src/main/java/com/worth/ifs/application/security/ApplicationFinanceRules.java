package com.worth.ifs.application.security;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.repository.ApplicationRepository;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.security.SecurityRuleUtil;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *  ApplicationFinanceRules are applying rules for seeing / updating the application
 */

@Component
@PermissionRules
public class ApplicationFinanceRules {
    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ProcessRoleRepository processRoleRepository;

    @PermissionRule(value = "READ", description = "An applicant can only see the totals of their finance ")
    public boolean applicantCanSeeTheOrganisationFinanceTotals(Application application, User user) {
        List<ProcessRole> processRole = processRoleRepository.findByUserAndApplication(user, application);
        return SecurityRuleUtil.isCompAdmin(user) || !processRole.isEmpty();
    }
}
