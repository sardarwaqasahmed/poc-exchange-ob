package com.poc.baraka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * @author Waqas Ahmed
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TradeDto {

    @Schema(description = "Id unique", example = "0001")
    long orderId;

    @Schema(description = "Amount to buy or sell", example = "100.0")
    BigDecimal amount;

    @Schema(description = "Price", example = "10.0")
    BigDecimal price;
}
