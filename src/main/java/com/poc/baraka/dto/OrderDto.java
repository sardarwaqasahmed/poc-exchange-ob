package com.poc.baraka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Waqas Ahmed
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to place a limit order")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDto {

    @Schema(description = "Id unique", example = "0001")
    long id;

    @Schema(description = "Timestamp of transaction", example = "2021-12-08T13:34:44.498775730Z")
    String timestamp;

    @Schema(description = "Asset symbol", example = "TST")
    String asset;

    @Schema(description = "Price", example = "10.0")
    BigDecimal price;

    @Schema(description = "Amount to buy or sell", example = "100.0")
    BigDecimal amount;

    @Schema(description = "Order direction", example = "SELL or Buy")
    String direction;

    @Schema(description = "Pending Amount", example = "90.0")
    BigDecimal pendingAmount;

    @Schema(description = "Trade that is done for filling the order", example = "")
    @Builder.Default
    List<TradeDto> trades = new ArrayList<>();
}
