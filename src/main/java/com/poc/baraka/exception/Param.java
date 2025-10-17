package com.poc.baraka.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Waqas Ahmed
 */
@Data                 // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Param {

    private String name;
    private String reason;
}
