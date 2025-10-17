package com.poc.baraka.exception;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author Waqas Ahmed
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingEngineException  extends Exception {

    private final List<String> errorList;

    public MatchingEngineException(List<String> errorList) {
        this.errorList = errorList;
    }

    public List<String> getErrorList() {
        return errorList;
    }
}
