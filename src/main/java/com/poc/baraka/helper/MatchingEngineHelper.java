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

        // Until there is pending amount and we have seller left
        while (buyOrder.getPendingAmount().compareTo(BigDecimal.ZERO) > 0 && !sellOrdersMap.isEmpty()) {
            BigDecimal bestAskPrice = sellOrdersMap.firstKey();

            // Price not acceptable, in case buyer want to buy at very low rate
            if (buyOrder.getPrice().compareTo(bestAskPrice) < 0) break;

            Queue<OrderDto> sellQueue = sellOrdersMap.get(bestAskPrice);
            if (sellQueue == null || sellQueue.isEmpty()) {
                sellOrdersMap.remove(bestAskPrice);
                continue;
            }

            OrderDto sellOrder = sellQueue.peek();

            BigDecimal tradedAmount = buyOrder.getPendingAmount().min(sellOrder.getPendingAmount());
            // If no pending amount left then break
            if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) break;

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
            // Remove price level
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
        // Until there is pending amount and we have buyer left
        while (sellOrder.getPendingAmount().compareTo(BigDecimal.ZERO) > 0 && !buyOrdersMap.isEmpty()) {
            BigDecimal bestBidPrice = buyOrdersMap.firstKey();

            // Price not acceptable, in case seller want to sell at very high rate
            if (sellOrder.getPrice().compareTo(bestBidPrice) > 0) break;

            Queue<OrderDto> buyQueue = buyOrdersMap.get(bestBidPrice);
            if (buyQueue == null || buyQueue.isEmpty()) {
                buyOrdersMap.remove(bestBidPrice);
                continue;
            }

            OrderDto buyOrder = buyQueue.peek();

            BigDecimal tradedAmount = sellOrder.getPendingAmount().min(buyOrder.getPendingAmount());
            if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) break;

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

    public synchronized OrderResponse cancelOrder(long orderId) {
        log.info("cancelOrder(..) called for id={}", orderId);
        OrderResponse orderResponse = allOrders.get(orderId);
        if (orderResponse == null) {
            log.warn("Order id={} not found in allOrders", orderId);
            orderResponse = new OrderResponse();
            orderResponse.setCanceled(false);
            return orderResponse;
        }

        OrderDto order = orderResponse.getOrder();
        boolean removed = false;

        if (OrderDirectionEnum.valueOf(order.getDirection().toUpperCase()) == OrderDirectionEnum.BUY) {
            removed = removeFromQueue(buyOrdersMap, order);
        } else {
            removed = removeFromQueue(sellOrdersMap, order);
        }

        if (removed) {
            // Update allOrders: mark as canceled or remove
            order.setPendingAmount(BigDecimal.ZERO);
            orderResponse.setCanceled(true);
            log.info("Order id={} canceled successfully", orderId);
        } else {
            log.warn("Order id={} not found in order book queues", orderId);
        }

        return orderResponse;
    }
    private boolean removeFromQueue(NavigableMap<BigDecimal, Queue<OrderDto>> ordersMap, OrderDto order) {
        Queue<OrderDto> queue = ordersMap.get(order.getPrice());
        if (queue != null) {
            boolean removed = queue.removeIf(o -> o.getId() == order.getId());
            if (queue.isEmpty()) {
                ordersMap.remove(order.getPrice());
            }
            return removed;
        }
        return false;
    }
}