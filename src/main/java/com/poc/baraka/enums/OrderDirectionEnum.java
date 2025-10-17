package com.poc.baraka.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Direction of the order", allowableValues = {"BUY", "SELL"})
public enum OrderDirectionEnum {
    BUY,
    SELL;
}
