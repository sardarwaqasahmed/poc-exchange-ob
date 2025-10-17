package com.poc.baraka.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * @author Waqas Ahmed
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvalidArgumentErrorResponse {

    private String type;
    private String title;
    private String detail;

    @JsonProperty("invalid-params")
    private List<Param> paramList;
}
