package com.poc.baraka.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author Waqas Ahmed
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {

    OrderDto order;
}
