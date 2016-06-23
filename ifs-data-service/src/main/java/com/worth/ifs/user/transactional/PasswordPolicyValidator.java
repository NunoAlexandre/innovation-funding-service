package com.worth.ifs.user.transactional;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.CollectionFunctions.*;
import static com.worth.ifs.util.MapFunctions.asMap;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.summingInt;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * A component that is able to enforce certain additional rules for the Innovate UK password policy
 * that are not handled in the Identity Provider (Shibboleth REST API)
 */
@Component
public class PasswordPolicyValidator {

    static final String PASSWORD_MUST_NOT_CONTAIN_FIRST_NAME = "PASSWORD_MUST_NOT_CONTAIN_FIRST_NAME";
    static final String PASSWORD_MUST_NOT_CONTAIN_LAST_NAME = "PASSWORD_MUST_NOT_CONTAIN_LAST_NAME";
    static final String PASSWORD_MUST_NOT_CONTAIN_FULL_NAME = "PASSWORD_MUST_NOT_CONTAIN_FULL_NAME";
    static final String PASSWORD_MUST_NOT_CONTAIN_FIRST_OR_LAST_NAME = "PASSWORD_MUST_NOT_CONTAIN_FIRST_OR_LAST_NAME";
    static final String PASSWORD_MUST_NOT_CONTAIN_ORGANISATION_NAME = "PASSWORD_MUST_NOT_CONTAIN_ORGANISATION_NAME";

    @Autowired
    private OrganisationRepository organisationRepository;

    private List<ExclusionRule> exclusionRules;
    private List<ExclusionRulePatternGenerator> exclusionRulePatternGenerators;

    /**
     * A class representing a facet of a User that we wish to exclude from their password e.g. first name, last name,
     * full name, organisation name(s)
     */
    class ExclusionRule {

        private String errorKey;
        private Function<UserResource, List<String>> exclusionSupplier;
        private int lengthThresholdForRuleToApply;
        private boolean mustContainAllExcludedWords;

        public ExclusionRule(String errorKey, int lengthThresholdForRuleToApply, boolean mustContainAllExcludedWords, Function<UserResource, List<String>> exclusionSupplier) {
            this.errorKey = errorKey;
            this.exclusionSupplier = exclusionSupplier;
            this.lengthThresholdForRuleToApply = lengthThresholdForRuleToApply;
            this.mustContainAllExcludedWords = mustContainAllExcludedWords;
        }
    }

    /**
     * A marker interface representing a component that, given a regular expression Pattern, can produce alternative
     * Patterns based on some set of rules (e.g. disallowing common numerical replacements of letters that would otherwise
     * produce an excluded word)
     */
    interface ExclusionRulePatternGenerator extends Function<String, List<Pattern>> {

    }

    /**
     * A component that, given a regular expression that defines an exclusion rule (e.g. for checking for the presence of
     * a first name in a string "*.first.*name.*"), is able to produce variations on this regex for additional checking of
     * common numerical replacements for letters.  e.g. given a pattern checking for ".*hello.*there.*", this generator
     * would allow numberical replacements to be checked for as well like ".*h[e3][l1][l1][o0].*th[e3]r[e3].*"
     */
    private ExclusionRulePatternGenerator lettersForNumbersGenerator = new ExclusionRulePatternGenerator() {

        private Map<String, String> interchangeableLettersAndNumbers = asMap(
                "a", "4",
                "b", "8",
                "e", "3",
                "i", "1",
                "l", "1",
                "o", "0",
                "s", "5",
                "z", "2");

        @Override
        public List<Pattern> apply(String currentExcludedRegexPattern) {

            String currentExcludedWordWithNumericalReplacements = currentExcludedRegexPattern.toLowerCase();

            for (Map.Entry<String, String> replacement : interchangeableLettersAndNumbers.entrySet()) {
                String searchString = format("([%s])", replacement.getKey());
                String replacementString = format("[$1%s]", replacement.getValue());
                currentExcludedWordWithNumericalReplacements =
                        currentExcludedWordWithNumericalReplacements.replaceAll(searchString, replacementString);
            }

            Pattern currentExcludedWordWithNumericalReplacementsPattern =
                    Pattern.compile(currentExcludedWordWithNumericalReplacements, CASE_INSENSITIVE);

            return singletonList(currentExcludedWordWithNumericalReplacementsPattern);
        }
    };

    @PostConstruct
    void postConstruct() {

        ExclusionRule containsFirstName = new ExclusionRule(PASSWORD_MUST_NOT_CONTAIN_FIRST_OR_LAST_NAME, 4, true, user -> singletonList(user.getFirstName()));
        ExclusionRule containsLastName = new ExclusionRule(PASSWORD_MUST_NOT_CONTAIN_FIRST_OR_LAST_NAME, 4, true, user -> singletonList(user.getLastName()));
        ExclusionRule containsFullName = new ExclusionRule(PASSWORD_MUST_NOT_CONTAIN_FIRST_OR_LAST_NAME, 5, true, user -> asList(user.getFirstName(), user.getLastName()));
        ExclusionRule containsOrganisationName = new ExclusionRule(PASSWORD_MUST_NOT_CONTAIN_ORGANISATION_NAME, 4, false, user -> {
            List<Long> organisationIds = user.getOrganisations();
            List<Organisation> organisations = simpleMap(organisationIds, organisationRepository::findOne);
            return simpleMap(organisations, Organisation::getName);
        });

        exclusionRules = asList(containsFirstName, containsLastName, containsFullName, containsOrganisationName);
        exclusionRulePatternGenerators = asList(lettersForNumbersGenerator);
    }

    /**
     * Validate the password, returning a list of errors if one or more exclusions were found
     *
     * @param password
     * @param userResource
     * @return
     */
    public ServiceResult<Void> validatePassword(String password, UserResource userResource) {

        List<ServiceResult<Void>> exclusionResults = flattenLists(simpleMap(exclusionRules, rule ->
                simpleMap(exclusionRulePatternGenerators, patternGenerator -> {

                    List<String> excludedWords = rule.exclusionSupplier.apply(userResource);

                    if (rule.mustContainAllExcludedWords) {
                        return checkForExclusionWordsWithinPassword(password, rule, patternGenerator, excludedWords);
                    } else {
                        List<ServiceResult<Void>> results = simpleMap(excludedWords, excludedWord -> checkForExclusionWordsWithinPassword(password, rule, patternGenerator, singletonList(excludedWord)));
                        return returnSuccessOrCollateFailures(results);
                    }
                })
        ));

        return returnSuccessOrCollateFailures(exclusionResults);
    }

    private ServiceResult<Void> returnSuccessOrCollateFailures(List<ServiceResult<Void>> exclusionResults) {
        List<ServiceResult<Void>> failures = simpleFilter(exclusionResults, ServiceResult::isFailure);

        if (!failures.isEmpty()) {
            List<Error> allErrors = flattenLists(simpleMap(failures, failure -> failure.getFailure().getErrors()));
            List<Error> uniqueErrors = removeDuplicates(allErrors);
            return serviceFailure(uniqueErrors);
        } else {
            return serviceSuccess();
        }
    }

    /**
     * This method, given a user, their chosen password, an exclusion rule (e.g. no full names) and a pattern generator, will
     * check for the presence of excluded tokens (in different orders as well if, for instance, checking for more than one word
     * for a single rule, like in the case of full name (that uses "first name" and "last name" in combination)).
     *
     * @param password
     * @param rule
     * @param patternGenerator
     * @param excludedWords
     * @return
     */
    private ServiceResult<Void> checkForExclusionWordsWithinPassword(String password, ExclusionRule rule, ExclusionRulePatternGenerator patternGenerator, List<String> excludedWords) {

        int lengthOfAllWords = excludedWords.stream().collect(summingInt(String::length));

        if (lengthOfAllWords < rule.lengthThresholdForRuleToApply) {
            return serviceSuccess();
        }

        List<List<String>> permutations = findPermutations(excludedWords);
        List<String> permutationsAsRegexes = simpleMap(permutations, permutation -> ".*" + simpleJoiner(permutation, ".*") + ".*");
        String permutationsAsSingleRegex = simpleJoiner(permutationsAsRegexes, "|");

        List<Pattern> exclusionPatterns = patternGenerator.apply(permutationsAsSingleRegex);
        boolean excluded = exclusionPatterns.stream().anyMatch(pattern -> pattern.matcher(password).matches());
        return excluded ? serviceFailure(new Error(rule.errorKey, BAD_REQUEST)) : serviceSuccess();
    }
}
