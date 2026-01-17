package com.jobboard.util;

import com.jobboard.exception.BadRequestException;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[1-9]\\d{1,14}$");

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://(?:[-\\w.])+(?:[:\\d]+)?(?:/(?:[\\w/_.])*(?:\\?(?:[\\w&=%.])*)?(?:#(?:\\w*))?)?$");

    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Invalid email format");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BadRequestException("Invalid phone number format");
        }
    }

    public static void validateUrl(String url, String fieldName) {
        if (url != null && !url.trim().isEmpty() && !URL_PATTERN.matcher(url).matches()) {
            throw new BadRequestException("Invalid " + fieldName + " URL format");
        }
    }

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(fieldName + " is required");
        }
    }

    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value != null) {
            int length = value.trim().length();
            if (length < minLength || length > maxLength) {
                throw new BadRequestException(
                        fieldName + " must be between " + minLength + " and " + maxLength + " characters");
            }
        }
    }

    public static void validatePositive(Number value, String fieldName) {
        if (value != null && value.doubleValue() <= 0) {
            throw new BadRequestException(fieldName + " must be positive");
        }
    }

    public static void validateRange(Number value, String fieldName, double min, double max) {
        if (value != null) {
            double val = value.doubleValue();
            if (val < min || val > max) {
                throw new BadRequestException(fieldName + " must be between " + min + " and " + max);
            }
        }
    }
}