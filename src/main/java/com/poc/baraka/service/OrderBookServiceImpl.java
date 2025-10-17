package com.poc.baraka.service;

import com.poc.baraka.common.IdGenerator;
import com.poc.baraka.dto.OrderDto;
import com.poc.baraka.dto.OrderRequest;
import com.poc.baraka.dto.OrderResponse;
import com.poc.baraka.helper.MatchingEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Waqas Ahmed
 */
@Service
public class OrderBookServiceImpl implements OrderBookService {

    private static final Logger log = LoggerFactory.getLogger(OrderBookServiceImpl.class);

    private final IdGenerator idGenerator;
    private final MatchingEngineHelper matchingEngineHelper;

    public OrderBookServiceImpl(IdGenerator idGenerator, MatchingEngineHelper matchingEngineHelper) {
        this.idGenerator = idGenerator;
        this.matchingEngineHelper = matchingEngineHelper;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        log.info("placeOrder execution starts");
        return matchingEngineHelper.matchOrder(mapToOrderDTO(orderRequest));
    }

    @Override
    public OrderResponse getOrderById(long orderId) {
        log.info("getOrderById(...)");
        return matchingEngineHelper.getOrderById(orderId);
    }

    private OrderDto mapToOrderDTO(OrderRequest orderRequest) {
        OrderDto order =  new OrderDto();
        order.setId(idGenerator.nextId());
        order.setTimestamp(Instant.now().toString());
        BeanUtils.copyProperties(orderRequest, order);
        return order;
    }
}
