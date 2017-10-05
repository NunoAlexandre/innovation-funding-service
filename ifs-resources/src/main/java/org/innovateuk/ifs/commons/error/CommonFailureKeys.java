package org.innovateuk.ifs.commons.error;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

/**
 * A set of failure cases for Service code, including general catch-all errors and more specific use-case errors that potentially
 * span different services
 */
public enum CommonFailureKeys implements ErrorTemplate {

    /**
     * General
     */
    GENERAL_UNEXPECTED_ERROR(INTERNAL_SERVER_ERROR),
    GENERAL_NOT_FOUND(NOT_FOUND),
    GENERAL_INCORRECT_TYPE(BAD_REQUEST),
    GENERAL_FORBIDDEN(FORBIDDEN),
    GENERAL_REST_RESULT_UNABLE_TO_PROCESS_REST_ERROR_RESPONSE(INTERNAL_SERVER_ERROR),
    GENERAL_REST_RESULT_UNEXPECTED_STATUS_CODE(INTERNAL_SERVER_ERROR),
    GENERAL_SERVICE_RESULT_NULL_RESULT_RETURNED(INTERNAL_SERVER_ERROR),
    GENERAL_SERVICE_RESULT_EXCEPTION_THROWN_DURING_PROCESSING(INTERNAL_SERVER_ERROR),
    GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION(FORBIDDEN),
    GENERAL_SPRING_SECURITY_OTHER_EXCEPTION_THROWN(INTERNAL_SERVER_ERROR),
    GENERAL_SINGLE_ENTRY_EXPECTED(INTERNAL_SERVER_ERROR),
    GENERAL_OPTIONAL_ENTRY_EXPECTED(INTERNAL_SERVER_ERROR),
    GENERAL_UNABLE_TO_PARSE_LONG(INTERNAL_SERVER_ERROR),

    /**
     * Files
     */
    FILES_UNABLE_TO_FIND_FILE_ENTRY_ID_FROM_FILE(INTERNAL_SERVER_ERROR),
    FILES_UNABLE_TO_CREATE_FILE(INTERNAL_SERVER_ERROR),
    FILES_NO_SUCH_FILE(INTERNAL_SERVER_ERROR),
    FILES_UNABLE_TO_MOVE_FILE(INTERNAL_SERVER_ERROR),
    FILES_FILE_ALREADY_LINKED_TO_FORM_INPUT_RESPONSE(CONFLICT),
    FILES_UNABLE_TO_UPDATE_FILE(INTERNAL_SERVER_ERROR),
    FILES_UNABLE_TO_DELETE_FILE(INTERNAL_SERVER_ERROR),
    FILES_UNABLE_TO_CREATE_FOLDERS(INTERNAL_SERVER_ERROR),
    FILES_DUPLICATE_FILE_CREATED(CONFLICT),
    FILES_DUPLICATE_FILE_MOVED(CONFLICT),
    FILES_MOVE_DESTINATION_EXIST_SOURCE_DOES_NOT(CONFLICT),
    FILES_DUPLICATE_FILE_(CONFLICT),
    FILES_INCORRECTLY_REPORTED_MEDIA_TYPE(UNSUPPORTED_MEDIA_TYPE),
    FILES_INCORRECTLY_REPORTED_FILESIZE(BAD_REQUEST),
    FILES_FILE_AWAITING_VIRUS_SCAN(FORBIDDEN),
    FILES_FILE_QUARANTINED(FORBIDDEN),
    FILES_NO_NAME_PROVIDED(BAD_REQUEST),
    FILES_EXCEPTION_WHILE_RETRIEVING_FILE(INTERNAL_SERVER_ERROR),

    /**
     * Competitions
     */
    COMPETITION_NOT_EDITABLE(BAD_REQUEST),
    COMPETITION_NOT_OPEN(BAD_REQUEST),
    COMPETITION_NOT_OPEN_OR_LATER(BAD_REQUEST),
    COMPETITION_NO_TEMPLATE(CONFLICT),

    /**
     * Applications
     */
    ASSIGNEE_SHOULD_BE_APPLICANT(BAD_REQUEST),
    APPLICATION_MUST_BE_SUBMITTED(BAD_REQUEST),
    APPLICATION_MUST_BE_INELIGIBLE(BAD_REQUEST),

    /**
     * Public content
     */
    PUBLIC_CONTENT_NOT_COMPLETE_TO_PUBLISH(BAD_REQUEST),
    PUBLIC_CONTENT_MILESTONES_NOT_COMPLETE_TO_PUBLISH(BAD_REQUEST),
    PUBLIC_CONTENT_ALREADY_INITIALISED(BAD_REQUEST),
    PUBLIC_CONTENT_NOT_INITIALISED(BAD_REQUEST),
    PUBLIC_CONTENT_IDS_INCONSISTENT(BAD_REQUEST),
    PUBLIC_CONTENT_KEYWORD_TOO_LONG(BAD_REQUEST),

    /**
     * Notifications
     */
    NOTIFICATIONS_UNABLE_TO_SEND_SINGLE(INTERNAL_SERVER_ERROR),
    NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE(INTERNAL_SERVER_ERROR),
    NOTIFICATIONS_UNABLE_TO_RENDER_TEMPLATE(INTERNAL_SERVER_ERROR),
    NOTIFICATIONS_UNABLE_TO_DETERMINE_NOTIFICATION_TARGETS(INTERNAL_SERVER_ERROR),

    /**
     * Emails
     */
    EMAILS_NOT_SENT_MULTIPLE(INTERNAL_SERVER_ERROR),

    /**
     * Users
     */
    USERS_DUPLICATE_EMAIL_ADDRESS(CONFLICT),
    USERS_EMAIL_VERIFICATION_TOKEN_NOT_FOUND(NOT_FOUND),
    USERS_EMAIL_VERIFICATION_TOKEN_EXPIRED(BAD_REQUEST),

    /**
     * Funding Panel
     */
    FUNDING_PANEL_DECISION_NOT_ALL_APPLICATIONS_REPRESENTED(BAD_REQUEST),
    FUNDING_PANEL_DECISION_NO_ASSESSOR_FEEDBACK_DATE_SET(BAD_REQUEST),
    FUNDING_PANEL_DECISION_WRONG_STATUS(BAD_REQUEST),

    /**
     * Assessor Journey
     */
    ASSESSMENT_CREATE_FAILED(BAD_REQUEST),
    ASSESSMENT_REJECTION_FAILED(BAD_REQUEST),
    ASSESSMENT_RECOMMENDATION_FAILED(BAD_REQUEST),
    ASSESSMENT_ACCEPT_FAILED(BAD_REQUEST),
    ASSESSMENT_SUBMIT_FAILED(BAD_REQUEST),
    ASSESSMENT_WITHDRAWN(FORBIDDEN),
    ASSESSMENT_WITHDRAW_FAILED(BAD_REQUEST),
    ASSESSMENT_NOTIFY_FAILED(BAD_REQUEST),
    COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE(BAD_REQUEST),
    COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE(BAD_REQUEST),
    COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_ACCEPTED_INVITE(BAD_REQUEST),
    COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_ACCEPTED_INVITE(BAD_REQUEST),
    COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_REJECTED_INVITE(BAD_REQUEST),
    COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_REJECTED_INVITE(BAD_REQUEST),
    COMPETITION_INVITE_CLOSED(BAD_REQUEST),
    COMPETITION_INVITE_CANNOT_DELETE_ONCE_SENT(BAD_REQUEST),
    COMPETITION_INVITE_EXPIRED(BAD_REQUEST),
    COMPETITION_INVITE_ALREADY_SENT(BAD_REQUEST),
    COMPETITION_CANNOT_RELEASE_FEEDBACK(BAD_REQUEST),

    /**
     * Project Setup
     */
    PROJECT_SETUP_DATE_MUST_START_ON_FIRST_DAY_OF_MONTH(BAD_REQUEST),
    PROJECT_SETUP_DATE_MUST_BE_IN_THE_FUTURE(BAD_REQUEST),
    PROJECT_SETUP_START_DATE_CANNOT_BE_CHANGED_ONCE_SPEND_PROFILE_HAS_BEEN_GENERATED(BAD_REQUEST),
    PROJECT_SETUP_PROJECT_MANAGER_MUST_BE_LEAD_PARTNER(BAD_REQUEST),
    PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_USER_ON_THE_PROJECT_FOR_THE_ORGANISATION(BAD_REQUEST),
    PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_PARTNER_ON_THE_PROJECT_FOR_THE_ORGANISATION(BAD_REQUEST),
    PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_SUBMITTED_IF_INCOMPLETE(BAD_REQUEST),
    PROJECT_SETUP_PROJECT_DETAILS_ADDRESS_SEARCH_OR_TYPE_MANUALLY(BAD_REQUEST),
	PROJECT_SETUP_PROJECT_ID_IN_URL_MUST_MATCH_PROJECT_ID_IN_MONITORING_OFFICER_RESOURCE(BAD_REQUEST),
    PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_UPDATED_IF_ALREADY_SUBMITTED(BAD_REQUEST),
    PROJECT_SETUP_PROJECT_ADDRESS_CANNOT_BE_UPDATED_IF_GOL_GENERATED(BAD_REQUEST),
    PROJECT_SETUP_PROJECT_MANAGER_CANNOT_BE_UPDATED_IF_GOL_GENERATED(BAD_REQUEST),
    PROJECT_SETUP_UNABLE_TO_CREATE_PROJECT_PROCESSES(INTERNAL_SERVER_ERROR),
    PROJECT_SETUP_CANNOT_PROGRESS_WORKFLOW(INTERNAL_SERVER_ERROR),
    CANNOT_FIND_ORG_FOR_GIVEN_PROJECT_AND_USER(NOT_FOUND),
    PROJECT_INVITE_INVALID_PROJECT_ID(BAD_REQUEST),
    PROJECT_TEAM_STATUS_APPLICATION_FINANCE_RECORD_FOR_APPLICATION_ORGANISATION_DOES_NOT_EXIST(NOT_FOUND),
    PROJECT_SETUP_INVITE_TARGET_USER_NOT_IN_CORRECT_ORGANISATION(BAD_REQUEST),
    PROJECT_SETUP_INVITE_TARGET_USER_ALREADY_EXISTS_ON_PROJECT(BAD_REQUEST),
    PROJECT_SETUP_INVITE_TARGET_USER_ALREADY_INVITED_ON_PROJECT(BAD_REQUEST),
    PROJECT_SETUP_CANNOT_INVITE_SELF(BAD_REQUEST),
    PROJECT_INVITE_INVALID(BAD_REQUEST),
    USER_ROLE_INVITE_INVALID(BAD_REQUEST),
    USER_ROLE_INVITE_INVALID_EMAIL(BAD_REQUEST),
    USER_ROLE_INVITE_TARGET_USER_ALREADY_INVITED(BAD_REQUEST),
    USER_ROLE_INVITE_EMAIL_TAKEN(BAD_REQUEST),
    NOT_AN_INTERNAL_USER_ROLE(BAD_REQUEST),
    PROJECT_SETUP_ALREADY_COMPLETE(BAD_REQUEST),
    CANNOT_GET_ANY_USERS_FOR_PROJECT(BAD_REQUEST),
    CREATE_PROJECT_FROM_APPLICATION_FAILS(BAD_REQUEST),

    /**
     * Non IFS competitions.
     */
    ONLY_NON_IFS_COMPETITION_VALID(BAD_REQUEST),
    /**
     * Project Bank details
     */
    BANK_DETAILS_CAN_ONLY_BE_SUBMITTED_ONCE(BAD_REQUEST),
    BANK_DETAILS_CANNOT_BE_UPDATED_BEFORE_BEING_SUBMITTED(BAD_REQUEST),
    BANK_DETAILS_DONT_EXIST_FOR_GIVEN_PROJECT_AND_ORGANISATION(NOT_FOUND),
    BANK_DETAILS_HAVE_ALREADY_BEEN_APPROVED_AND_CANNOT_BE_UPDATED(BAD_REQUEST),
    EXPERIAN_VALIDATION_FAILED(BAD_REQUEST),
    EXPERIAN_VALIDATION_FAILED_WITH_INCORRECT_ACC_NO(BAD_REQUEST),
    EXPERIAN_VALIDATION_FAILED_WITH_INCORRECT_BANK_DETAILS(BAD_REQUEST),
    EXPERIAN_VERIFICATION_FAILED(BAD_REQUEST),

    /**
     * Project Monitoring Officer
     */
    PROJECT_SETUP_MONITORING_OFFICER_CANNOT_BE_ASSIGNED_UNTIL_PROJECT_DETAILS_SUBMITTED(BAD_REQUEST),

    /**
     * Companies House
     */
    COMPANIES_HOUSE_NO_RESPONSE(INTERNAL_SERVER_ERROR),
    COMPANIES_HOUSE_UNABLE_TO_DECODE_SEARCH_STRING(INTERNAL_SERVER_ERROR),

    /**
     * Spend profile
     */
    SPEND_PROFILE_TOTAL_FOR_ALL_MONTHS_DOES_NOT_MATCH_ELIGIBLE_TOTAL_FOR_SPECIFIED_CATEGORY(BAD_REQUEST),
    SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE(BAD_REQUEST),
    SPEND_PROFILE_CANNOT_BE_GENERATED_UNTIL_ALL_VIABILITY_APPROVED(BAD_REQUEST),
    SPEND_PROFILE_CANNOT_BE_GENERATED_UNTIL_ALL_ELIGIBILITY_APPROVED(BAD_REQUEST),
    SPEND_PROFILE_HAS_ALREADY_BEEN_GENERATED(BAD_REQUEST),
    SPEND_PROFILE_HAS_BEEN_SUBMITTED_AND_CANNOT_BE_EDITED(BAD_REQUEST),
    SPEND_PROFILE_CSV_GENERATION_FAILURE(INTERNAL_SERVER_ERROR),
    SPEND_PROFILES_MUST_BE_COMPLETE_BEFORE_SUBMISSION(BAD_REQUEST),
    SPEND_PROFILES_HAVE_ALREADY_BEEN_SUBMITTED(BAD_REQUEST),
    SPEND_PROFILE_FINANCE_CONTACT_NOT_PRESENT(BAD_REQUEST),
    /**
     * Finance Checks
     */
    FINANCE_CHECKS_CANNOT_PROGRESS_WORKFLOW(INTERNAL_SERVER_ERROR),
    FINANCE_CHECKS_CONTAINS_FRACTIONS_IN_COST(BAD_REQUEST),
    FINANCE_CHECKS_COST_LESS_THAN_ZERO(BAD_REQUEST),
    FINANCE_CHECKS_COST_NULL(BAD_REQUEST),
    FINANCE_CHECKS_CANNOT_GENERATE_FOR_PROJECT(BAD_REQUEST),

    VIABILITY_HAS_ALREADY_BEEN_APPROVED(BAD_REQUEST),
    VIABILITY_RAG_STATUS_MUST_BE_SET(BAD_REQUEST),
    VIABILITY_CHECKS_NOT_APPLICABLE(NOT_FOUND),
    ELIGIBILITY_HAS_ALREADY_BEEN_APPROVED(BAD_REQUEST),
    ELIGIBILITY_RAG_STATUS_MUST_BE_SET(BAD_REQUEST),

    FINANCE_CHECKS_POST_ATTACH_NOT_UPLOADED(BAD_REQUEST),

    QUERIES_CANNOT_BE_SENT_AS_FINANCE_CONTACT_NOT_SUBMITTED(NOT_FOUND),
    
    /**
     * Offer letter
     */

    GRANT_OFFER_LETTER_GENERATION_UNABLE_TO_RENDER_TEMPLATE(INTERNAL_SERVER_ERROR),
    GRANT_OFFER_LETTER_GENERATION_UNABLE_TO_CONVERT_TO_PDF(INTERNAL_SERVER_ERROR),
    GRANT_OFFER_LETTER_GENERATION_FAILURE(BAD_REQUEST),

    SIGNED_GRANT_OFFER_LETTER_MUST_BE_UPLOADED_BEFORE_SUBMIT(BAD_REQUEST),
    GRANT_OFFER_LETTER_MUST_BE_AVAILABLE_BEFORE_SEND(BAD_REQUEST),
    GRANT_OFFER_LETTER_MUST_BE_SENT_BEFORE_UPLOADING_SIGNED_COPY(BAD_REQUEST),
    GRANT_OFFER_LETTER_CANNOT_SET_SIGNED_STATE(BAD_REQUEST),
    GRANT_OFFER_LETTER_NOT_READY_TO_APPROVE(BAD_REQUEST),
    GRANT_OFFER_LETTER_CANNOT_BE_REMOVED(BAD_REQUEST),

    /**
     * Other documents
     */
    PROJECT_SETUP_OTHER_DOCUMENTS_CAN_ONLY_SUBMITTED_BY_PROJECT_MANAGER(BAD_REQUEST),
    PROJECT_SETUP_OTHER_DOCUMENTS_MUST_BE_UPLOADED_BEFORE_SUBMIT(BAD_REQUEST),
    PROJECT_SETUP_OTHER_DOCUMENTS_APPROVAL_DECISION_MUST_BE_PROVIDED(BAD_REQUEST),
    PROJECT_SETUP_OTHER_DOCUMENTS_HAVE_ALREADY_BEEN_APPROVED(BAD_REQUEST),


    /**
     * SIL CRM
     */
    CONTACT_NOT_UPDATED(INTERNAL_SERVER_ERROR),

    /**
     * IFS Admin
     */
    ADMIN_INVALID_USER_ROLE(BAD_REQUEST);


    private ErrorTemplate errorTemplate;

    CommonFailureKeys(HttpStatus category) {
        this.errorTemplate = new ErrorTemplateImpl(name(), category);
    }

    @Override
    public String getErrorKey() {
        return errorTemplate.getErrorKey();
    }

    @Override
    public HttpStatus getCategory() {
        return errorTemplate.getCategory();
    }
}
