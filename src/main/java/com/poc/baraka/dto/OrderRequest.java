package com.poc.baraka.dto;

import com.poc.baraka.enums.OrderDirectionEnum;
import com.poc.baraka.validator.EnumValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * @author Waqas Ahmed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to place a limit order")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    @Schema(description = "Asset symbol", example = "TST")
    @NotBlank(message = "Asset is mandatory")
    @Size(min = 3, max = 5, message = "Asset symbol must be between 3 and 5 characters")
    String asset;

    @Schema(description = "Price", example = "43250.00")
    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price;

    @Schema(description = "Amount to buy or sell", example = "0.25")
    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    BigDecimal amount;

    @Schema(description = "Order direction", example = "SELL or Buy")
    @NotNull(message = "Direction is mandatory")
    @EnumValidator(enumClass = OrderDirectionEnum.class, message = "Direction must be BUY or SELL")
    String direction;
}
