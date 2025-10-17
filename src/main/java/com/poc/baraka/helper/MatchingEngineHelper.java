package com.poc.baraka.helper;

import com.poc.baraka.dto.OrderDto;
import com.poc.baraka.dto.OrderResponse;
import com.poc.baraka.dto.TradeDto;
import com.poc.baraka.enums.OrderDirectionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Waqas Ahmed
 */
@Service
public class MatchingEngineHelper {
    private static final Logger log = LoggerFactory.getLogger(MatchingEngineHelper.class);

    // Buy orders sorted highest price first
    private final NavigableMap<BigDecimal, Queue<OrderDto>> buyOrdersMap = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    // Sell orders sorted lowest price first
    private final NavigableMap<BigDecimal, Queue<OrderDto>> sellOrdersMap = new ConcurrentSkipListMap<>();

    // Order storage
    private final Map<Long, OrderResponse> allOrders = new ConcurrentHashMap<>();

    public synchronized OrderResponse matchOrder(OrderDto order) {
        log.info("matchOrder(..) called");
        OrderResponse response;
        if (OrderDirectionEnum.valueOf(order.getDirection().toUpperCase()) == OrderDirectionEnum.BUY) {
            response = matchBuyOrder(order);
        } else {
            response = matchSellOrder(order);
        }

        allOrders.put(response.getOrder().getId(), response);

        return response;
    }
    private OrderResponse matchBuyOrder(OrderDto buyOrder) {
        // Initialize pendingAmount if not set
        if (buyOrder.getPendingAmount() == null) {
            buyOrder.setPendingAmount(buyOrder.getAmount());
        }

        OrderResponse response = OrderResponse.builder()
                .order(OrderDto.builder()
                        .id(buyOrder.getId())
                        .direction(buyOrder.getDirection())
                        .asset(buyOrder.getAsset())
                        .timestamp(buyOrder.getTimestamp())
                        .build())
                .build();


        while (buyOrder.getPendingAmount().compareTo(BigDecimal.ZERO) > 0 && !sellOrdersMap.isEmpty()) {
            BigDecimal bestAskPrice = sellOrdersMap.firstKey();

            // Price not acceptable
            if (buyOrder.getPrice().compareTo(bestAskPrice) < 0) break;

            Queue<OrderDto> sellQueue = sellOrdersMap.get(bestAskPrice);
            if (sellQueue == null || sellQueue.isEmpty()) {
                sellOrdersMap.remove(bestAskPrice);
                continue;
            }

            OrderDto sellOrder = sellQueue.peek();

            BigDecimal tradedAmount = buyOrder.getPendingAmount().min(sellOrder.getPendingAmount());
            if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) break; // safety

            // Record trades
            response.getOrder().getTrades().add(TradeDto.builder()
                    .orderId(sellOrder.getId())
                    .amount(tradedAmount)
                    .price(bestAskPrice)
                    .build());

            sellOrder.getTrades().add(TradeDto.builder()
                    .orderId(buyOrder.getId())
                    .amount(tradedAmount)
                    .price(bestAskPrice)
                    .build());

            // Update pending amounts
            buyOrder.setPendingAmount(buyOrder.getPendingAmount().subtract(tradedAmount));
            sellOrder.setPendingAmount(sellOrder.getPendingAmount().subtract(tradedAmount));

            // Remove fully filled sell order
            if (sellOrder.getPendingAmount().compareTo(BigDecimal.ZERO) == 0) {
                sellQueue.poll();
            }
            if (sellQueue.isEmpty()) {
                sellOrdersMap.remove(bestAskPrice);
            }
        }

        // Add remaining BUY order to order book if partially filled
        if (buyOrder.getPendingAmount().compareTo(BigDecimal.ZERO) > 0) {
            buyOrdersMap.computeIfAbsent(buyOrder.getPrice(), k -> new LinkedList<>()).add(buyOrder);
        }

        response.getOrder().setPendingAmount(buyOrder.getPendingAmount());
        return response;
    }

    private OrderResponse matchSellOrder(OrderDto sellOrder) {

        // Initialize pendingAmount if not set
        if (sellOrder.getPendingAmount() == null) {
            sellOrder.setPendingAmount(sellOrder.getAmount());
        }

        OrderResponse response = OrderResponse.builder().order(sellOrder).build();

        while (sellOrder.getPendingAmount().compareTo(BigDecimal.ZERO) > 0 && !buyOrdersMap.isEmpty()) {
            BigDecimal bestBidPrice = buyOrdersMap.firstKey();

            // Price not acceptable
            if (sellOrder.getPrice().compareTo(bestBidPrice) > 0) break;

            Queue<OrderDto> buyQueue = buyOrdersMap.get(bestBidPrice);
            if (buyQueue == null || buyQueue.isEmpty()) {
                buyOrdersMap.remove(bestBidPrice);
                continue;
            }

            OrderDto buyOrder = buyQueue.peek();

            BigDecimal tradedAmount = sellOrder.getPendingAmount().min(buyOrder.getPendingAmount());
            if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) break; // safety

            // Record trades
            response.getOrder().getTrades().add(TradeDto.builder()
                    .orderId(buyOrder.getId())
                    .amount(tradedAmount)
                    .price(bestBidPrice)
                    .build());

            buyOrder.getTrades().add(TradeDto.builder()
                    .orderId(sellOrder.getId())
                    .amount(tradedAmount)
                    .price(bestBidPrice)
                    .build());

            // Update pending amounts
            sellOrder.setPendingAmount(sellOrder.getPendingAmount().subtract(tradedAmount));
            buyOrder.setPendingAmount(buyOrder.getPendingAmount().subtract(tradedAmount));

            // Remove fully filled buy order
            if (buyOrder.getPendingAmount().compareTo(BigDecimal.ZERO) == 0) {
                buyQueue.poll();
            }
            if (buyQueue.isEmpty()) {
                buyOrdersMap.remove(bestBidPrice);
            }
        }

        // Add remaining SELL order to order book if partially filled
        if (sellOrder.getPendingAmount().compareTo(BigDecimal.ZERO) > 0) {
            sellOrdersMap.computeIfAbsent(sellOrder.getPrice(), k -> new LinkedList<>()).add(sellOrder);
        }

        response.getOrder().setPendingAmount(sellOrder.getPendingAmount());
        return response;
    }


    public OrderResponse getOrderById(long orderId) {
        return allOrders.get(orderId);
    }
}