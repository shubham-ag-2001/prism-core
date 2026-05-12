package com.prism.core.common.exception;

public final class ErrorCode {

    private ErrorCode() {}

    // Generic
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String BAD_REQUEST         = "BAD_REQUEST";
    public static final String UNAUTHORIZED        = "UNAUTHORIZED";
    public static final String FORBIDDEN           = "FORBIDDEN";
    public static final String INTERNAL_ERROR      = "INTERNAL_ERROR";

    // Auth
    public static final String OTP_EXPIRED         = "OTP_EXPIRED";
    public static final String OTP_INVALID         = "OTP_INVALID";
    public static final String OTP_ALREADY_USED    = "OTP_ALREADY_USED";
    public static final String TOKEN_INVALID       = "TOKEN_INVALID";
    public static final String TOKEN_EXPIRED       = "TOKEN_EXPIRED";

    // User
    public static final String USER_NOT_FOUND      = "USER_NOT_FOUND";
    public static final String PHONE_ALREADY_EXISTS = "PHONE_ALREADY_EXISTS";
    public static final String PAN_ALREADY_EXISTS  = "PAN_ALREADY_EXISTS";
    public static final String PAN_VERIFICATION_FAILED = "PAN_VERIFICATION_FAILED";

    // Scoring
    public static final String SCORING_JOB_NOT_FOUND = "SCORING_JOB_NOT_FOUND";
    public static final String SCORING_FAILED       = "SCORING_FAILED";
    public static final String SCORING_IN_PROGRESS  = "SCORING_IN_PROGRESS";
    public static final String NO_SCORE_AVAILABLE   = "NO_SCORE_AVAILABLE";
    public static final String ONBOARDING_INCOMPLETE = "ONBOARDING_INCOMPLETE";

    // Provider
    public static final String PROVIDER_ERROR       = "PROVIDER_ERROR";
    public static final String SMS_EXTRACTION_FAILED = "SMS_EXTRACTION_FAILED";
    public static final String EMPLOYER_NOT_FOUND   = "EMPLOYER_NOT_FOUND";

    // Bank
    public static final String BANK_NOT_FOUND       = "BANK_NOT_FOUND";

    // Account
    public static final String ACCOUNT_ALREADY_INACTIVE = "ACCOUNT_ALREADY_INACTIVE";
}
