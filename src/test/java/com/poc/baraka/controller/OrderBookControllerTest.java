package com.poc.baraka.controller;

import com.poc.baraka.controller.OrderBookController;
import com.poc.baraka.dto.OrderDto;
import com.poc.baraka.dto.OrderRequest;
import com.poc.baraka.dto.OrderResponse;
import com.poc.baraka.service.OrderBookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderBookControllerTest {

	@Mock
	private OrderBookService orderBookService;

	@InjectMocks
	private OrderBookController controller;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testPlaceOrder_Success() {
		// Prepare mock request
		OrderRequest request = OrderRequest.builder()
				.asset("BTC")
				.amount(BigDecimal.valueOf(1.5))
				.price(BigDecimal.valueOf(10000.0))
				.direction("BUY")
				.build();

		// Prepare mock response
		OrderResponse mockResponse = OrderResponse.builder().order(
				OrderDto.builder()
				.id(1L)
				.asset("BTC")
				.amount(BigDecimal.valueOf(1.5))
				.price(BigDecimal.valueOf(10000.0))
				.direction("BUY")
				.pendingAmount(BigDecimal.valueOf(1.5))
				.build()).build();

		when(orderBookService.placeOrder(any(OrderRequest.class))).thenReturn(mockResponse);

		// Call controller
		ResponseEntity<?> responseEntity = controller.placeOrder(request);

		// Verify
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		assertTrue(responseEntity.getBody() instanceof OrderResponse);
		OrderResponse response = (OrderResponse) responseEntity.getBody();
		assertEquals(mockResponse.getOrder().getId(), response.getOrder().getId());
		assertEquals(mockResponse.getOrder().getAsset(), response.getOrder().getAsset());
		assertEquals(mockResponse.getOrder().getAmount(), response.getOrder().getAmount());
		assertEquals(mockResponse.getOrder().getDirection(), response.getOrder().getDirection());

		verify(orderBookService, times(1)).placeOrder(any(OrderRequest.class));
	}

	@Test
	void testGetOrder_Found() {
		long orderId = 1L;

		OrderResponse mockResponse = OrderResponse.builder().order(
				OrderDto.builder()
				.id(orderId)
				.asset("BTC")
				.amount(BigDecimal.valueOf(2.0))
				.price(BigDecimal.valueOf(10500.0))
				.direction("SELL")
				.pendingAmount(BigDecimal.valueOf(2.0))
				.build())
				.build();

		when(orderBookService.getOrderById(orderId)).thenReturn(mockResponse);

		ResponseEntity<?> responseEntity = controller.getOrder(orderId);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertTrue(responseEntity.getBody() instanceof OrderResponse);
		OrderResponse response = (OrderResponse) responseEntity.getBody();
		assertEquals(orderId, response.getOrder().getId());
		assertEquals("BTC", response.getOrder().getAsset());

		verify(orderBookService, times(1)).getOrderById(orderId);
	}

	@Test
	void testGetOrder_NotFound() {
		long orderId = 99L;

		when(orderBookService.getOrderById(orderId)).thenReturn(null);

		ResponseEntity<?> responseEntity = controller.getOrder(orderId);

		// check that body is not null
		assertNotNull(responseEntity.getBody());

		// check type
		assertTrue(responseEntity.getBody() instanceof OrderResponse);

		OrderResponse response = (OrderResponse) responseEntity.getBody();

		// order should be null
		assertNull(response.getOrder());

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
		verify(orderBookService, times(1)).getOrderById(orderId);
	}

}

