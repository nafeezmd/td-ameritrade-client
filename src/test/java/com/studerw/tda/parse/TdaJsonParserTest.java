package com.studerw.tda.parse;

import static org.assertj.core.api.Assertions.assertThat;

import com.studerw.tda.model.AssetType;
import com.studerw.tda.model.ParseQuotesTest;
import com.studerw.tda.model.account.CashEquivalentInstrument;
import com.studerw.tda.model.account.EquityInstrument;
import com.studerw.tda.model.account.Instrument;
import com.studerw.tda.model.account.MarginAccount;
import com.studerw.tda.model.account.MarginCurrentBalances;
import com.studerw.tda.model.account.MarginInitialBalances;
import com.studerw.tda.model.account.MarginProjectedBalances;
import com.studerw.tda.model.account.Order;
import com.studerw.tda.model.account.OrderLegCollection;
import com.studerw.tda.model.account.OrderLegCollection.Instruction;
import com.studerw.tda.model.account.OrderLegCollection.OrderLegType;
import com.studerw.tda.model.account.OrderStrategyType;
import com.studerw.tda.model.account.Position;
import com.studerw.tda.model.account.SecuritiesAccount;
import com.studerw.tda.model.account.SecuritiesAccount.Type;
import com.studerw.tda.model.history.Candle;
import com.studerw.tda.model.history.PriceHistory;
import com.studerw.tda.model.instrument.FullInstrument;
import com.studerw.tda.model.instrument.Fundamental;
import com.studerw.tda.model.marketdata.Mover;
import com.studerw.tda.model.marketdata.Mover.Direction;
import com.studerw.tda.model.option.Option;
import com.studerw.tda.model.option.Option.PutCall;
import com.studerw.tda.model.option.OptionChain;
import com.studerw.tda.model.option.OptionChain.Strategy;
import com.studerw.tda.model.option.Underlying;
import com.studerw.tda.model.quote.EquityQuote;
import com.studerw.tda.model.quote.EtfQuote;
import com.studerw.tda.model.quote.ForexQuote;
import com.studerw.tda.model.quote.IndexQuote;
import com.studerw.tda.model.quote.MutualFundQuote;
import com.studerw.tda.model.quote.OptionQuote;
import com.studerw.tda.model.quote.Quote;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TdaJsonParserTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TdaJsonParserTest.class);
  private TdaJsonParser tdaJsonParser = new TdaJsonParser();

  @Test
  public void parseQuoteTest() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/equity-quote-resp.json")) {
      List<Quote> quotes = tdaJsonParser.parseQuotes(in);
      assertThat(quotes).size().isEqualTo(1);
      EquityQuote equityQuote = (EquityQuote) quotes.get(0);
      assertThat(equityQuote).isInstanceOf(EquityQuote.class);
      assertThat(equityQuote.getAssetType()).isEqualTo(AssetType.EQUITY);
      assertThat(equityQuote).isNotNull();
      assertThat(equityQuote.getOpenPrice()).isEqualTo("139.15");
      LOGGER.debug(equityQuote.toString());
    }
  }

  @Test
  public void parseQuotesTest() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader()
        .getResourceAsStream("com/studerw/tda/parse/quotes-resp.json")) {
      List<Quote> quotes = tdaJsonParser.parseQuotes(in);
      quotes.forEach(quote -> LOGGER.debug(quote.toString()));
      assertThat(quotes).size().isEqualTo(6);
      assertThat(quotes.get(0).getAssetType()).isEqualTo(AssetType.MUTUAL_FUND);
      assertThat(quotes.get(0)).isInstanceOf(MutualFundQuote.class);
      assertThat(quotes.get(1).getAssetType()).isEqualTo(AssetType.EQUITY);
      assertThat(quotes.get(1)).isInstanceOf(EquityQuote.class);
      assertThat(quotes.get(2).getSymbol()).isEqualTo("NOK/JPY");
      assertThat(quotes.get(2).getAssetType()).isEqualTo(AssetType.FOREX);
      assertThat(quotes.get(2)).isInstanceOf(ForexQuote.class);
      assertThat(quotes.get(3)).isInstanceOf(IndexQuote.class);
      IndexQuote indexQuote = (IndexQuote) quotes.get(3);
      assertThat(indexQuote.getClosePrice()).isEqualTo("2926.46");
      assertThat(quotes.get(4)).isInstanceOf(OptionQuote.class);
      assertThat(quotes.get(5)).isInstanceOf(EtfQuote.class);
    }
  }

  @Test
  public void parsePriceHistoryTest() throws Exception {
    try (InputStream in = ParseQuotesTest.class.getClassLoader()
        .getResourceAsStream("com/studerw/tda/parse/price-history-resp.json")) {
      PriceHistory priceHistory = tdaJsonParser.parsePriceHistory(in);
      assertThat(priceHistory).isNotNull();
      assertThat(priceHistory.getCandles().size()).isEqualTo(5418);
      assertThat(priceHistory.getSymbol()).isEqualTo("MSFT");
      assertThat(priceHistory.isEmpty()).isFalse();
      LOGGER.debug(priceHistory.toString());
      Candle candle = priceHistory.getCandles().get(5416);
      LOGGER.debug(candle.toString());
      assertThat(candle.getOpen()).isEqualTo("137.15");
      assertThat(candle.getHigh()).isEqualTo("137.15");
      assertThat(candle.getLow()).isEqualTo("137.15");
      assertThat(candle.getClose()).isEqualTo("137.15");
      assertThat(candle.getVolume()).isEqualTo(530);
      assertThat(candle.getDatetime()).isEqualTo(1568417880000L);
    }
  }

  @Test
  public void parseAccountTest() throws Exception {
    try (InputStream in = ParseQuotesTest.class.getClassLoader()
        .getResourceAsStream("com/studerw/tda/parse/account-resp.json")) {
      final SecuritiesAccount account = tdaJsonParser.parseAccount(in);
      assertThat(account).isNotNull();
      assertThat(account.getAccountId()).isEqualTo("1234567890");
      assertThat(account.getType()).isEqualTo(Type.MARGIN);
      assertThat(account).isInstanceOf(MarginAccount.class);
      assertThat(account.getDayTrader()).isFalse();
      LOGGER.debug(account.toString());

      MarginAccount marginAccount = (MarginAccount) account;
      assertThat(marginAccount).isNotNull();

      final MarginProjectedBalances projectedBalances = marginAccount.getProjectedBalances();
      assertThat(projectedBalances.getBuyingPower()).isEqualTo("4047.16");
      assertThat(projectedBalances.getAvailableFunds()).isEqualTo("2023.58");
      assertThat(projectedBalances.getDayTradingBuyingPower()).isEqualTo("0.0");
      assertThat(projectedBalances.getInCall()).isFalse();

      final MarginInitialBalances initialBalances = marginAccount.getInitialBalances();
      assertThat(initialBalances.getMoneyMarketFund()).isNotNull();
      assertThat(initialBalances.getAccruedInterest()).isEqualTo("0.0");
      assertThat(initialBalances.getAvailableFundsNonMarginableTrade())
          .isEqualTo("2023.58");
      assertThat(initialBalances.getBuyingPower()).isEqualTo("4047.16");
      assertThat(initialBalances.getMaintenanceCall()).isEqualTo("0.0");

      final MarginCurrentBalances currentBalances = marginAccount.getCurrentBalances();
      assertThat(currentBalances.getMoneyMarketFund()).isNotNull();
      assertThat(currentBalances.getMaintenanceCall()).isEqualTo("0.0");
      assertThat(currentBalances.getLiquidationValue()).isEqualTo("3252.47");
      assertThat(currentBalances.getMoneyMarketFund()).isEqualTo("200.72");
      assertThat(currentBalances.getLongMarketValue()).isEqualTo("3051.75");
      assertThat(currentBalances.getSma()).isEqualTo("2224.3");

      final List<Position> positions = marginAccount.getPositions();
      assertThat(positions).hasSize(2);
      Position pos1 = marginAccount.getPositions().get(0);
      assertThat(pos1.getMarketValue()).isEqualTo("200.72");
      assertThat(pos1.getAveragePrice()).isEqualTo("1.0");
      assertThat(pos1.getSettledShortQuantity()).isEqualTo("0.0");

      Instrument inst1 = pos1.getInstrument();
      assertThat(inst1 != null);
      assertThat(inst1 instanceof CashEquivalentInstrument);
      assertThat(inst1.getAssetType().equals(Instrument.AssetType.CASH_EQUIVALENT));

      Position pos2 = account.getPositions().get(1);
      assertThat(pos2.getAveragePrice()).isEqualTo("10.05625");
      assertThat(pos2.getCurrentDayProfitLoss()).isEqualTo("-443.6");
      assertThat(pos2.getCurrentDayProfitLossPercentage()).isEqualTo("-0.13");
      assertThat(pos2.getMarketValue()).isEqualTo("3051.75");

      Instrument inst2 = pos2.getInstrument();
      assertThat(inst2).isNotNull();
      assertThat(inst2 instanceof EquityInstrument);
      assertThat(inst2.getCusip()).isEqualTo("345370860");
      assertThat(inst2.getSymbol()).isEqualTo("F");

      LOGGER.debug("{}", account);
    }
  }

  @Test
  public void parseAccountsTest() throws Exception {
    try (InputStream in = ParseQuotesTest.class.getClassLoader()
        .getResourceAsStream("com/studerw/tda/parse/accounts-resp.json")) {
      final List<SecuritiesAccount> accounts = tdaJsonParser.parseAccounts(in);
      assertThat(accounts).hasSize(1);
      final SecuritiesAccount account = accounts.get(0);
      assertThat(account).isNotNull();
      assertThat(account.getAccountId()).isEqualTo("1234567890");
      assertThat(account.getType()).isEqualTo(Type.MARGIN);
      assertThat(account).isInstanceOf(MarginAccount.class);
      assertThat(account.getDayTrader()).isFalse();
      LOGGER.debug(account.toString());

      MarginAccount marginAccount = (MarginAccount) account;
      assertThat(marginAccount).isNotNull();

      final MarginProjectedBalances projectedBalances = marginAccount.getProjectedBalances();
      assertThat(projectedBalances.getBuyingPower()).isEqualTo("4047.16");
      assertThat(projectedBalances.getAvailableFunds()).isEqualTo("2023.58");
      assertThat(projectedBalances.getDayTradingBuyingPower()).isEqualTo("0.0");
      assertThat(projectedBalances.getInCall()).isFalse();

      final MarginInitialBalances initialBalances = marginAccount.getInitialBalances();
      assertThat(initialBalances.getMoneyMarketFund()).isNotNull();
      assertThat(initialBalances.getAccruedInterest()).isEqualTo("0.0");
      assertThat(initialBalances.getAvailableFundsNonMarginableTrade())
          .isEqualTo("2023.58");
      assertThat(initialBalances.getBuyingPower()).isEqualTo("4047.16");
      assertThat(initialBalances.getMaintenanceCall()).isEqualTo("0.0");

      final MarginCurrentBalances currentBalances = marginAccount.getCurrentBalances();
      assertThat(currentBalances.getMoneyMarketFund()).isNotNull();
      assertThat(currentBalances.getMaintenanceCall()).isEqualTo("0.0");
      assertThat(currentBalances.getLiquidationValue()).isEqualTo("3266.38");
      assertThat(currentBalances.getMoneyMarketFund()).isEqualTo("200.72");
      assertThat(currentBalances.getLongMarketValue()).isEqualTo("3065.66");
      assertThat(currentBalances.getSma()).isEqualTo("2224.3");

      final List<Position> positions = marginAccount.getPositions();
      assertThat(positions).hasSize(2);
      Position pos1 = marginAccount.getPositions().get(0);
      assertThat(pos1.getMarketValue()).isEqualTo("200.72");
      assertThat(pos1.getAveragePrice()).isEqualTo("1.0");
      assertThat(pos1.getSettledShortQuantity()).isEqualTo("0.0");

      Instrument inst1 = pos1.getInstrument();
      assertThat(inst1 != null);
      assertThat(inst1 instanceof CashEquivalentInstrument);
      assertThat(inst1.getAssetType().equals(Instrument.AssetType.CASH_EQUIVALENT));

      Position pos2 = account.getPositions().get(1);
      assertThat(pos2.getAveragePrice()).isEqualTo("10.05625");
      assertThat(pos2.getCurrentDayProfitLoss()).isEqualTo("-429.69");
      assertThat(pos2.getCurrentDayProfitLossPercentage()).isEqualTo("-0.12");
      assertThat(pos2.getMarketValue()).isEqualTo("3065.66");

      Instrument inst2 = pos2.getInstrument();
      assertThat(inst2).isNotNull();
      assertThat(inst2 instanceof EquityInstrument);
      assertThat(inst2.getCusip()).isEqualTo("345370860");
      assertThat(inst2.getSymbol()).isEqualTo("F");

      LOGGER.debug("{}", account);

    }
  }

  @Test
  public void testParseOrders() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/orders-resp.json")) {
      final List<Order> orders = tdaJsonParser.parseOrders(in);
      assertThat(orders).size().isEqualTo(1);
      Order order = orders.get(0);
      assertThat(order.getOrderStrategyType()).isEqualTo(OrderStrategyType.SINGLE);
      assertThat(order.getOrderId()).isEqualTo(999999999L);
      assertThat(order.getEditable()).isFalse();
      assertThat(order.getQuantity()).isEqualTo("15.0");


      OrderLegCollection olc = order.getOrderLegCollection().get(0);
      assertThat(olc.getOrderLegType()).isEqualTo(OrderLegType.EQUITY);
      assertThat(olc.getInstruction()).isEqualTo(Instruction.BUY);
      assertThat(olc.getQuantity()).isEqualTo("15.0");

      assertThat(olc.getInstrument().getAssetType()).isEqualTo(Instrument.AssetType.EQUITY);
      assertThat(olc.getInstrument().getSymbol()).isEqualToIgnoringCase("msft");


      LOGGER.debug(orders.toString());
    }
  }

  @Test
  public void testParseCusipResp() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/cusip-resp.json")) {
      final com.studerw.tda.model.instrument.Instrument instrument = tdaJsonParser
          .parseInstrumentArraySingle(in);
      assertThat(instrument.getAssetType()).isEqualTo(com.studerw.tda.model.instrument.Instrument.AssetType.BOND);
      assertThat(instrument.getBondPrice()).isEqualTo("99.86");
      assertThat(instrument.getSymbol()).isEqualTo("7954505R2");
      assertThat(instrument.getCusip()).isEqualTo("7954505R2");
      assertThat(instrument.getExchange()).isEqualTo("OTC");
      LOGGER.debug(instrument.toString());
    }
  }

  @Test
  public void testParseInstrumentMap() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/multi-instrument-resp.json")) {
      final List<com.studerw.tda.model.instrument.Instrument> instruments = tdaJsonParser
          .parseInstrumentMap(in);
      assertThat(instruments).hasSize(54);
      instruments.stream().forEach(instrument -> {
        assertThat(instrument.getAssetType()).isNotNull();
        assertThat(instrument.getBondPrice()).isNull();
        assertThat(instrument.getSymbol()).isNotNull();
        assertThat(instrument.getCusip()).isNotNull();
        assertThat(instrument.getExchange()).isNotNull();
        assertThat(instrument.getDescription()).isNotNull();
        LOGGER.debug("{}", instrument);
      });
    }
  }

  @Test
  public void testParseInstrumentFundamental() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/instrument-fundamental-resp.json")) {
      final FullInstrument instrument = tdaJsonParser.parseFullInstrumentMap(in).get(0);
      assertThat(instrument.getAssetType()).isEqualTo(com.studerw.tda.model.instrument.Instrument.AssetType.EQUITY);
      assertThat(instrument.getSymbol()).isEqualTo("MSFT");
      assertThat(instrument.getCusip()).isEqualTo("594918104");
      assertThat(instrument.getExchange()).isEqualTo("NASDAQ");
      Fundamental fundamental = instrument.getFundamental();
      assertThat(fundamental).isNotNull();
      assertThat(fundamental.getMarketCapFloat()).isEqualTo("7521.585");
      assertThat(fundamental.getDividendDate()).isEqualTo("2019-11-20 00:00:00.000");
      LOGGER.debug(instrument.toString());
    }
  }

  @Test
  public void testParseMovers() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/movers-resp.json")) {
      List<Mover> movers = tdaJsonParser.parseMovers(in);
      assertThat(movers).size().isEqualTo(10);
      Mover nike = movers.get(4);
      assertThat(nike.getChange()).isEqualTo("0.011221824513882137");
      assertThat(nike.getDescription()).isEqualTo("Nike, Inc. Common Stock");
      assertThat(nike.getDirection()).isEqualTo(Direction.up);
      assertThat(nike.getLast()).isEqualTo("104.53");
      assertThat(nike.getSymbol()).isEqualTo("NKE");
      assertThat(nike.getTotalVolume()).isEqualTo(9394813);
      movers.forEach(m -> LOGGER.debug("{}", m));
    }
  }

  @Test
  public void testParseOptionChain() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/optionChain-resp.json")) {
      final OptionChain optionChain = tdaJsonParser.parseOptionChain(in);
      assertThat(optionChain.getSymbol()).isEqualToIgnoringCase("MSFT");
      assertThat(optionChain.getStatus()).isEqualTo("SUCCESS");
      assertThat(optionChain.getStrategy()).isEqualTo(Strategy.SINGLE);
      assertThat(optionChain.getIndex()).isFalse();
      assertThat(optionChain.getDelayed()).isTrue();
      assertThat(optionChain.getOtherFields()).isEmpty();

      final Underlying underlying = optionChain.getUnderlying();
      assertThat(underlying.getSymbol()).isEqualToIgnoringCase("MSFT");
      assertThat(underlying.getClose()).isEqualTo("152.32");
      assertThat(underlying.getTradeTime()).isEqualTo(1574902778840L);
      assertThat(underlying.getFiftyTwoWeekHigh()).isEqualTo("152.5");
      assertThat(underlying.getDelayed()).isTrue();
      assertThat(underlying.getOtherFields()).isEmpty();

      final Map<String, Map<BigDecimal, List<Option>>> putExpDateMap = optionChain
          .getPutExpDateMap();
      final Map<BigDecimal, List<Option>> bigDecimalListMap = putExpDateMap.get("2020-01-10:42");
      final List<Option> options = bigDecimalListMap.get(new BigDecimal("135.0"));
      assertThat(options.size()).isEqualTo(1);
      Option option = options.get(0);
      assertThat(option.getPutCall()).isEqualTo(PutCall.PUT);
      assertThat(option.getBidPrice()).isEqualTo("0.23");
      assertThat(option.getTradeTimeInLong()).isEqualTo(1574886746322L);
      assertThat(option.getRho()).isEqualTo("-0.01");
      assertThat(option.getMini()).isFalse();
      assertThat(option.getInTheMoney()).isFalse();
      assertThat(option.getOtherFields()).isNotEmpty();

      final Map<String, Map<BigDecimal, List<Option>>> callExpDateMap = optionChain
          .getCallExpDateMap();
      final Map<BigDecimal, List<Option>> bigDecimalListMap2 = callExpDateMap.get("2020-01-03:35");
      final List<Option> options2 = bigDecimalListMap2.get(new BigDecimal("135.0"));
      assertThat(options2.size()).isEqualTo(1);
      Option option2 = options2.get(0);
      assertThat(option2.getPutCall()).isEqualTo(PutCall.CALL);
      assertThat(option2.getBidPrice()).isEqualTo("17.3");
      assertThat(option2.getTradeTimeInLong()).isEqualTo(1574446609655L);
      assertThat(option2.getRho()).isEqualTo("0.11");
      assertThat(option2.getMini()).isFalse();
      assertThat(option2.getInTheMoney()).isTrue();
      assertThat(option2.getOtherFields()).isNotEmpty();
      LOGGER.debug(optionChain.toString());
    }
  }

  @Test
  public void testParseOptionChainNan() throws IOException {
    try (InputStream in = ParseQuotesTest.class.getClassLoader().
        getResourceAsStream("com/studerw/tda/parse/optionChain-NAN-resp.json")) {
      final OptionChain optionChain = tdaJsonParser.parseOptionChain(in);

      final Map<String, Map<BigDecimal, List<Option>>> putExpDateMap = optionChain
          .getPutExpDateMap();
      final Map<BigDecimal, List<Option>> bigDecimalListMap = putExpDateMap.get("2020-01-10:42");
      final List<Option> options = bigDecimalListMap.get(new BigDecimal("135.0"));
      assertThat(options.size()).isEqualTo(1);
      Option option = options.get(0);
      assertThat(option.getPutCall()).isEqualTo(PutCall.PUT);
      assertThat(option.getBidPrice()).isEqualTo("0.23");
      assertThat(option.getTradeTimeInLong()).isEqualTo(1574886746322L);
      assertThat(option.getRho()).isEqualTo("-0.01");
      assertThat(option.getTheta()).isEqualTo("0");
      assertThat(option.getMini()).isFalse();
      assertThat(option.getInTheMoney()).isFalse();
      assertThat(option.getOtherFields()).isNotEmpty();
      LOGGER.debug(optionChain.toString());
    }
  }
}

