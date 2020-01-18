package com.studerw.tda.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.studerw.tda.model.account.Duration;
import com.studerw.tda.model.account.EquityInstrument;
import com.studerw.tda.model.account.Instrument;
import com.studerw.tda.model.account.Order;
import com.studerw.tda.model.account.OrderLegCollection;
import com.studerw.tda.model.account.OrderLegCollection.Instruction;
import com.studerw.tda.model.account.OrderRequest;
import com.studerw.tda.model.account.OrderStrategyType;
import com.studerw.tda.model.account.OrderType;
import com.studerw.tda.model.account.Session;
import com.studerw.tda.model.account.Status;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FetchOrdersTestIT extends BaseTestIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchOrdersTestIT.class);

  @Test(expected = IllegalArgumentException.class)
  public void testPlaceOrderNoId() {
    httpTdaClient.placeOrder(null, new Order());
    fail("shouldn't get here");
  }


  /**
   * @see <a href="https://developer.tdameritrade.com/content/place-order-samples">Place Order
   * Samples</a>
   */
  @Test
  @Ignore
  public void testPlaceSimpleOrder() {
    this.httpTdaClient.placeOrder(getAccountId(), simpleOrder());
  }

  @Test
  public void testFetchOrders() {
    OrderRequest orderRequest = new OrderRequest();
    final List<Order> orders = this.httpTdaClient.fetchOrders(getAccountId(), orderRequest);
    assertThat(orders).isNotNull();
    orders.forEach(o -> LOGGER.debug(o.toString()));
  }

  @Test
  public void testFetchOrderByCriteria() {
    OrderRequest orderRequest = new OrderRequest(ZonedDateTime.now().minusDays(10),
        ZonedDateTime.now().plusDays(10));
    final List<Order> orders = this.httpTdaClient.fetchOrders(getAccountId(), orderRequest);
    assertThat(orders).isNotNull();
    orders.forEach(o -> LOGGER.debug(o.toString()));
  }

  @Test
  @Ignore
  public void testFetchAllOrders() {
    final List<Order> originalOrders = this.httpTdaClient.fetchOrders();
    LOGGER.debug("Initial count of orders: {}", originalOrders.size());

    this.httpTdaClient.placeOrder(getAccountId(), simpleOrder());
    final List<Order> orders = httpTdaClient.fetchOrders();
    LOGGER.debug("new count of orders: {}", orders.size());
    assertThat(orders.size()).isEqualTo(originalOrders.size() + 1);

    final Long orderId = orders.get(0).getOrderId();
    LOGGER.debug("OrderId: {}", orderId);
    httpTdaClient.cancelOrder(getAccountId(), String.valueOf(orderId));

    final List<Order> orders2 = httpTdaClient.fetchOrders();
    LOGGER.debug("final count of orders: {}", orders2.size());
    final Optional<Order> first = orders2.stream()
        .filter(order -> order.getOrderId().equals(orderId)).findFirst();
    assertThat(first.isPresent());
    assertThat(first.get().getStatus()).isEqualTo(Status.CANCELED);
  }

  @Test
  @Ignore
  public void testCancelOrder() {
    OrderRequest orderRequest = new OrderRequest();
    this.httpTdaClient.cancelOrder(getAccountId(), "99999999");
  }

  @Test
  public void testAllOrders() {
    final List<Order> orders = this.httpTdaClient.fetchOrders(new OrderRequest());
    assertThat(orders).isNotNull();
    LOGGER.debug("{}", orders);
  }

  @Test
  public void testFetchSpecificOrder() {
    final List<Order> orders = this.httpTdaClient.fetchOrders(new OrderRequest());
    if (orders.size() == 0) {
      LOGGER.warn("Can't complete test because no orders at the moment");
      return;
    }
    Long orderId = orders.get(0).getOrderId();
    final Order order = httpTdaClient.fetchOrder(getAccountId(), orderId);
    assertThat(order).isNotNull();
    assertThat(order.getOrderId()).isEqualTo(orderId);
    LOGGER.debug("{}", order);
  }

  @Test(expected = RuntimeException.class)
  public void testFetchBadOrder() {
    final Order order = this.httpTdaClient.fetchOrder(getAccountId(), -1L);
    fail("Should have thrown RuntimeException");

  }

  private Order simpleOrder() {
    Order order = new Order();
    order.setOrderType(OrderType.MARKET);
    order.setSession(Session.NORMAL);
    order.setDuration(Duration.DAY);
    order.setOrderStrategyType(OrderStrategyType.SINGLE);

    OrderLegCollection olc = new OrderLegCollection();
    olc.setInstruction(Instruction.BUY);
    olc.setQuantity(new BigDecimal("15.0"));
    order.getOrderLegCollection().add(olc);

    Instrument instrument = new EquityInstrument();
    instrument.setSymbol("MSFT");
    olc.setInstrument(instrument);
    LOGGER.debug(order.toString());
    return order;
  }
}