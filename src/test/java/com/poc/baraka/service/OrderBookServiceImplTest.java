package com.poc.baraka.service;

/**
 * @author Waqas Ahmed
 */

import com.poc.baraka.common.IdGenerator;
import com.poc.baraka.dto.OrderDto;
import com.poc.baraka.dto.OrderRequest;
import com.poc.baraka.dto.OrderResponse;
import com.poc.baraka.helper.MatchingEngineHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderBookServiceImplTest {

    private IdGenerator idGenerator;
    private MatchingEngineHelper matchingEngineHelper;
    private OrderBookServiceImpl orderBookService;

    @BeforeEach
    void setUp() {
        idGenerator = mock(IdGenerator.class);
        matchingEngineHelper = mock(MatchingEngineHelper.class);
        orderBookService = new OrderBookServiceImpl(idGenerator, matchingEngineHelper);
    }

    @Test
    void testPlaceOrder() {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setAsset("BTC");
        request.setAmount(new BigDecimal("1.5"));
        request.setPrice(new BigDecimal("10000"));
        request.setDirection("BUY");

        when(idGenerator.nextId()).thenReturn(101L);

        OrderResponse mockedResponse = OrderResponse.builder()
                .order(OrderDto.builder()
                        .id(101L)
                        .asset("BTC")
                        .amount(new BigDecimal("1.5"))
                        .price(new BigDecimal("10000"))
                        .direction("BUY")
                        .build())
                .build();

        when(matchingEngineHelper.matchOrder(any(OrderDto.class))).thenReturn(mockedResponse);

        // Act
        OrderResponse response = orderBookService.placeOrder(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getOrder());
        assertEquals(101L, response.getOrder().getId());
        assertEquals("BTC", response.getOrder().getAsset());
        assertEquals(new BigDecimal("1.5"), response.getOrder().getAmount());
        assertEquals("BUY", response.getOrder().getDirection());

        // Verify that ID was generated and passed to matching engine
        ArgumentCaptor<OrderDto> captor = ArgumentCaptor.forClass(OrderDto.class);
        verify(matchingEngineHelper, times(1)).matchOrder(captor.capture());

        OrderDto capturedOrder = captor.getValue();
        assertEquals(101L, capturedOrder.getId());
        assertEquals("BTC", capturedOrder.getAsset());
        assertEquals("BUY", capturedOrder.getDirection());

        verify(idGenerator, times(1)).nextId();
    }

    @Test
    void testGetOrderById() {
        // Arrange
        long orderId = 101L;
        OrderResponse mockedResponse = OrderResponse.builder()
                .order(OrderDto.builder()
                        .id(orderId)
                        .asset("BTC")
                        .amount(new BigDecimal("1.5"))
                        .price(new BigDecimal("10000"))
                        .direction("BUY")
                        .build())
                .build();

        when(matchingEngineHelper.getOrderById(orderId)).thenReturn(mockedResponse);

        // Act
        OrderResponse response = orderBookService.getOrderById(orderId);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.getOrder().getId());
        assertEquals("BTC", response.getOrder().getAsset());

        verify(matchingEngineHelper, times(1)).getOrderById(orderId);
    }
}
