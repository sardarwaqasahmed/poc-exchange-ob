package com.poc.baraka.service;

import com.poc.baraka.dto.OrderRequest;
import com.poc.baraka.dto.OrderResponse;

public interface OrderBookService {
    OrderResponse placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderById(long orderId);

    OrderResponse cancelOrder(long orderId);
}
