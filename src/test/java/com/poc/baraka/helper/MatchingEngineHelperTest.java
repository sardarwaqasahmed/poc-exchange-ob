package com.poc.baraka.helper;

/**
 * @author Waqas Ahmed
 */

import com.poc.baraka.dto.OrderDto;
import com.poc.baraka.dto.OrderResponse;
import com.poc.baraka.dto.TradeDto;
import com.poc.baraka.enums.OrderDirectionEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineHelperTest {

    private MatchingEngineHelper matchingEngine;

    @BeforeEach
    void setUp() {
        matchingEngine = new MatchingEngineHelper();
    }

    @Test
    void testPlaceBuyOrder_NoMatchingSellOrder() {
        OrderDto buyOrder = OrderDto.builder()
                .id(1L)
                .asset("BTC")
                .amount(new BigDecimal("1.0"))
                .price(new BigDecimal("10000"))
                .direction(OrderDirectionEnum.BUY.name())
                .build();

        OrderResponse response = matchingEngine.matchOrder(buyOrder);

        assertNotNull(response);
        assertEquals(buyOrder.getId(), response.getOrder().getId());
        assertEquals(buyOrder.getAmount(), response.getOrder().getPendingAmount());
    }

    @Test
    void testPlaceSellOrder_NoMatchingBuyOrder() {
        OrderDto sellOrder = OrderDto.builder()
                .id(2L)
                .asset("BTC")
                .amount(new BigDecimal("2.0"))
                .price(new BigDecimal("11000"))
                .direction(OrderDirectionEnum.SELL.name())
                .build();

        OrderResponse response = matchingEngine.matchOrder(sellOrder);

        assertNotNull(response);
        assertEquals(sellOrder.getId(), response.getOrder().getId());
        assertEquals(sellOrder.getAmount(), response.getOrder().getPendingAmount());
    }

    @Test
    void testBuyOrderMatchesExistingSellOrder() {
        // Existing sell order
        OrderDto sellOrder = OrderDto.builder()
                .id(3L)
                .asset("BTC")
                .amount(new BigDecimal("1.5"))
                .price(new BigDecimal("10000"))
                .direction(OrderDirectionEnum.SELL.name())
                .build();

        matchingEngine.matchOrder(sellOrder);

        // Incoming buy order
        OrderDto buyOrder = OrderDto.builder()
                .id(4L)
                .asset("BTC")
                .amount(new BigDecimal("1.0"))
                .price(new BigDecimal("10000"))
                .direction(OrderDirectionEnum.BUY.name())
                .build();

        OrderResponse response = matchingEngine.matchOrder(buyOrder);

        assertNotNull(response);
        assertEquals(new BigDecimal("0.0"), response.getOrder().getPendingAmount());
        assertEquals(1, response.getOrder().getTrades().size());

        TradeDto trade = response.getOrder().getTrades().get(0);
        assertEquals(sellOrder.getId(), trade.getOrderId());
        assertEquals(new BigDecimal("1.0"), trade.getAmount());
        assertEquals(new BigDecimal("10000"), trade.getPrice());
    }

    @Test
    void testSellOrderMatchesExistingBuyOrder() {
        // Existing buy order
        OrderDto buyOrder = OrderDto.builder()
                .id(5L)
                .asset("BTC")
                .amount(new BigDecimal("2.0"))
                .price(new BigDecimal("10500"))
                .direction(OrderDirectionEnum.BUY.name())
                .build();

        matchingEngine.matchOrder(buyOrder);

        // Incoming sell order
        OrderDto sellOrder = OrderDto.builder()
                .id(6L)
                .asset("BTC")
                .amount(new BigDecimal("1.5"))
                .price(new BigDecimal("10500"))
                .direction(OrderDirectionEnum.SELL.name())
                .build();

        OrderResponse response = matchingEngine.matchOrder(sellOrder);

        assertNotNull(response);
        assertEquals(new BigDecimal("0.0"), response.getOrder().getPendingAmount());
        assertEquals(1, response.getOrder().getTrades().size());

        TradeDto trade = response.getOrder().getTrades().get(0);
        assertEquals(buyOrder.getId(), trade.getOrderId());
        assertEquals(new BigDecimal("1.5"), trade.getAmount());
        assertEquals(new BigDecimal("10500"), trade.getPrice());
    }

    @Test
    void testGetOrderById() {
        OrderDto buyOrder = OrderDto.builder()
                .id(7L)
                .asset("BTC")
                .amount(new BigDecimal("1.0"))
                .price(new BigDecimal("10000"))
                .direction(OrderDirectionEnum.BUY.name())
                .build();

        matchingEngine.matchOrder(buyOrder);

        OrderResponse response = matchingEngine.getOrderById(7L);

        assertNotNull(response);
        assertEquals(buyOrder.getId(), response.getOrder().getId());
    }

    @Test
    void testGetOrderById_NotFound() {
        OrderResponse response = matchingEngine.getOrderById(999L);
        assertNull(response);
    }
}
