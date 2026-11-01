package com.pranov.devops.common;

/** A request that is well-formed but breaks a business rule (e.g. ordering an unavailable dish). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
