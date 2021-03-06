ALTER TABLE activity_state
    MODIFY COLUMN activity_type
        ENUM('APPLICATION_ASSESSMENT',
        'PROJECT_SETUP',
        'PROJECT_SETUP_COMPANIES_HOUSE_DETAILS',
        'PROJECT_SETUP_PROJECT_DETAILS',
        'PROJECT_SETUP_MONITORING_OFFICER_ASSIGNMENT',
        'PROJECT_SETUP_BANK_DETAILS',
        'PROJECT_SETUP_FINANCE_CHECKS',
        'PROJECT_SETUP_SPEND_PROFILE',
        'PROJECT_SETUP_GRANT_OFFER_LETTER')
        NOT NULL;