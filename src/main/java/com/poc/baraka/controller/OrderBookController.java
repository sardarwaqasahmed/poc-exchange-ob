package com.poc.baraka.controller;

import com.poc.baraka.dto.OrderRequest;
import com.poc.baraka.dto.OrderResponse;
import com.poc.baraka.service.OrderBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.poc.baraka.utils.JsonUtils.toJSON;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Orders", description = "Endpoints for placing and querying limit orders")
public class OrderBookController {

    private static final Logger log = LoggerFactory.getLogger(OrderBookController.class);

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    /**
     * Place a new limit order.
     */
    @Operation(summary = "Place a new limit order",
            description = "Creates a new BUY or SELL limit order and returns its current state",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order placed successfully",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            })
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("POST /orders request received: {}", toJSON(orderRequest));
        OrderResponse orderResponse = orderBookService.placeOrder(orderRequest);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable long orderId) {
        log.info("Get /orders/1 request received: {}", orderId);
        OrderResponse orderResponse = orderBookService.getOrderById(orderId);
        if (orderResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(OrderResponse.builder().order(null).build());
        }
        return ResponseEntity.ok(orderResponse);
    }
}
