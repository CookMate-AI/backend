package com.project.cook_mate.jwt;

import lombok.Getter;

@Getter
public class TokenValidationResult {
    private final boolean isValid;
    private final boolean isExpired;
    private final String errorMessage;

    private TokenValidationResult(boolean isValid, boolean isExpired, String errorMessage) {
        this.isValid = isValid;
        this.isExpired = isExpired;
        this.errorMessage = errorMessage;
    }

    public static TokenValidationResult valid() {
        return new TokenValidationResult(true, false, null);
    }

    public static TokenValidationResult expired() {
        return new TokenValidationResult(false, true, "Token has expired");
    }

    public static TokenValidationResult invalid(String message) {
        return new TokenValidationResult(false, false, message);
    }

}
