package tradeprocessing.productbook;

import constants.GlobalConstants.BookSide;
import constants.GlobalConstants.MarketState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map.Entry;
import price.Price;
import price.PriceFactory;
import publishers.CurrentMarketPublisher;
import publishers.LastSalePublisher;
import publishers.MessagePublisher;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.CancelMessage;
import publishers.messages.FillMessage;
import publishers.messages.MarketDataDTO;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.Order;
import tradeable.Quote;
import tradeable.Tradeable;
import tradeable.TradeableDTO;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;
import tradeprocessing.productbook.exceptions.DataValidationException;
import tradeprocessing.productbook.exceptions.OrderNotFoundException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.productservice.ProductService;
import tradeprocessing.tradeprocessor.exceptions.InvalidProductBookSideValueException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorFactoryException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;

public class ProductBook {

  /**
   * The String stock symbol that this book represents (i.e., MSFT,
   * IBM, AAPL, etc).
   */
  private String symbol;

  /**
   * ProductBookSide that maintains the Buy side of this book.
   */
  private ProductBookSide buySide;

  /**
   * ProductBookSide that maintains the Sell side of this book.
   */
  private ProductBookSide sellSide;

  /**
   * A String that will hold the toString results of the latest Market Data
   * values (the prices and the volumes at the top of the buy and sell sides).
   */
  private String lastCurrentMarket = "";

  /**
   * A list of the current quotes in this book for each user.
   */
  private HashSet<String> userQuotes = new HashSet<>();

  /**
   * A list of “old” Tradeables (those that have been completely traded or
   * cancelled) organized by price and by user. This should be represented like
   * you represented the book entries for one side of the book in the
   * ProductBookSide class.
   */
  private HashMap<Price, ArrayList<Tradeable>> oldEntries = new HashMap<>();

  public ProductBook(String sym)
          throws ProductBookException, ProductBookSideException,
          InvalidProductBookSideValueException, TradeProcessorFactoryException {
    setSymbol(sym);
    buySide = new ProductBookSide(this, BookSide.BUY);
    sellSide = new ProductBookSide(this, BookSide.SELL);
  }

  private void setSymbol(String sym) throws ProductBookException {
    validateInput(sym);
    symbol = sym;
  }


  public synchronized final ArrayList<TradeableDTO>
          getOrdersWithRemainingQty(String userName)
          throws ProductBookSideException, ProductBookException {
    validateInput(userName);
    ArrayList<TradeableDTO> t = new ArrayList<>();
    t.addAll(buySide.getOrdersWithRemainingQty(userName));
    t.addAll(sellSide.getOrdersWithRemainingQty(userName));
    return t;
  }

  /**
   * This method id designed to determine if it is too late to cancel an order
   * (meaning it has already been traded out or cancelled).
   *
   * @param orderId
   */
  public synchronized final void checkTooLateToCancel(String orderId)
          throws OrderNotFoundException, InvalidMessageException,
          ProductBookException, MessagePublisherException {
    validateInput(orderId);
    boolean isFound = false;
    for(Entry<Price, ArrayList<Tradeable>> row : oldEntries.entrySet()) {
      ListIterator<Tradeable> iterator = row.getValue().listIterator();
      while (iterator.hasNext()) {
        Tradeable t = iterator.next();
        if (t.getId().equals(orderId)) {
          isFound = true;
          MessagePublisher.getInstance().publishCancel(new CancelMessage(
                  t.getUser(), t.getProduct(), t.getPrice(),
                  // is this remaining volume or cancelled volume
                  t.getRemainingVolume(), "Too late to cancel order ID: " +
                  t.getId(), t.getSide(), t.getId()));
        }
      }
    }
    if (!isFound) {
      throw new OrderNotFoundException("The order with the"
              + " specified order id: " + orderId + "; could not be found.");
    }
  }

  /**
   * This method is should return a 2-dimensional array of Strings that contain
   * the prices and volumes at all prices present in the buy and sell sides of
   * the book.
   *
   * @return 2-dimensional array of Strings
   */
  public synchronized final String[][] getBookDepth() {
    String[][] bd = new String[2][];
    bd[0] = buySide.getBookDepth();
    bd[1] = sellSide.getBookDepth();
    return bd;
  }

  /**
   * This method should create a MarketDataDTO containing the best buy side
   * price and volume, and the best sell side price an volume.
   *
   * @return MarketDataDTO
   */
  public synchronized final MarketDataDTO getMarketData() {
    Price topBuyPrice = buySide.topOfBookPrice();
    Price topSellPrice = sellSide.topOfBookPrice();
    if (topBuyPrice == null) {
      topBuyPrice = PriceFactory.makeLimitPrice("0");
    }
    if (topSellPrice == null) {
      topSellPrice = PriceFactory.makeLimitPrice("0");
    }
    int bestBuySideVolume = buySide.topOfBookVolume();
    int bestSellSideVolume = sellSide.topOfBookVolume();
    return new MarketDataDTO(symbol, topBuyPrice, bestBuySideVolume,
            topSellPrice, bestSellSideVolume);
  }

  /**
   * This method should add the Tradable passed in to the "oldEntries" HashMap.
   *
   * @param t
   */
  public synchronized final void addOldEntry(Tradeable t)
          throws InvalidVolumeException, ProductBookException {
    validateInput(t);
    if (!oldEntries.containsKey(t.getPrice())) {
      oldEntries.put(t.getPrice(), new ArrayList<Tradeable>());
    }
    t.setCancelledVolume(t.getRemainingVolume());
    t.setRemainingVolume(0);
    oldEntries.get(t.getPrice()).add(t);
  }

  /**
   * This method will "Open" the book for trading. Any resting Order and
   * QuoteSides that are immediately tradable upon opening should be traded.
   */
  public synchronized final void openMarket()
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException, MessagePublisherException {
    Price buyPrice = buySide.topOfBookPrice();
    Price sellPrice = sellSide.topOfBookPrice();
    if (buyPrice == null || sellPrice == null) { return; }
    while (buyPrice.greaterOrEqual(sellPrice) || buyPrice.isMarket()
            || sellPrice.isMarket()) {
      ArrayList<Tradeable> topOfBuySide = buySide.getEntriesAtPrice(buyPrice);
      HashMap<String, FillMessage> allFills = null;
      ArrayList<Tradeable> toRemove = new ArrayList<>();
      for (Tradeable t : topOfBuySide) {
        allFills = sellSide.tryTrade(t);
        if (t.getRemainingVolume() == 0) {
          toRemove.add(t);
        }
      }
      for (Tradeable t : toRemove) {
        buySide.removeTradeable(t);
        addOldEntry(t);
      }
      updateCurrentMarket();
      Price lastSalePrice = determineLastSalePrice(allFills);
      int lastSaleVolume = determineLastSaleQuantity(allFills);
      LastSalePublisher.getInstance().publishLastSale(symbol, lastSalePrice,
              lastSaleVolume);
      buyPrice = buySide.topOfBookPrice();
      sellPrice = sellSide.topOfBookPrice();
      if (buyPrice == null || sellPrice == null) { break; }
    }
  }

  /**
   * This method will “Close” the book for trading.
   */
  public synchronized final void closeMarket()
          throws InvalidMessageException, OrderNotFoundException,
          InvalidVolumeException, ProductBookSideException,
          ProductBookException, TradeableException, MessagePublisherException {
    buySide.cancelAll();
    sellSide.cancelAll();
    updateCurrentMarket();
  }

  /**
   * This method will cancel the Order specified by the provided orderId on the
   * specified side.
   *
   * @param side
   * @param orderId
   */
  public synchronized final void cancelOrder(BookSide side, String orderId)
          throws InvalidMessageException, OrderNotFoundException,
          InvalidVolumeException, ProductBookSideException,
          ProductBookException, MessagePublisherException {
    validateInput(side);
    validateInput(orderId);
    if (side.equals(BookSide.BUY)) {
      buySide.submitOrderCancel(orderId);
    } else {
      sellSide.submitOrderCancel(orderId);
    }
    updateCurrentMarket();
  }

  /**
   * This method will cancel the specified user’s Quote on the both the BUY and
   * SELL sides.
   *
   * @param userName
   */
  public synchronized final void cancelQuote(String userName)
          throws InvalidMessageException, ProductBookSideException,
          ProductBookException, InvalidVolumeException,
          MessagePublisherException {
    validateInput(userName);
    buySide.submitQuoteCancel(userName);
    sellSide.submitQuoteCancel(userName);
    updateCurrentMarket();
  }

  /**
   * This method should add the provided Quote’s sides to the Buy and Sell
   * ProductSideBooks.
   *
   * @param q
   * @throws InvalidVolumeException
   * @throws DataValidationException
   */
  public synchronized final void addToBook(Quote q)
          throws InvalidVolumeException, DataValidationException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, TradeProcessorPriceTimeImplException,
          TradeableException, MessagePublisherException {
    validateInput(q);
    if (q.getQuoteSide(BookSide.SELL).getPrice().lessOrEqual(
            q.getQuoteSide(BookSide.BUY).getPrice())) {
      throw new DataValidationException("Sell Price is less than or equal to"
              + " buy price.");
    }
    if (q.getQuoteSide(BookSide.SELL).getPrice().lessOrEqual(
            PriceFactory.makeLimitPrice("0")) ||
            q.getQuoteSide(BookSide.BUY).getPrice().lessOrEqual(
            PriceFactory.makeLimitPrice("0"))) {
      throw new DataValidationException("Buy or Sell Price cannot be less than"
              + " or equal to zero.");
    }
    if (q.getQuoteSide(BookSide.SELL).getOriginalVolume() <= 0 ||
            q.getQuoteSide(BookSide.BUY).getOriginalVolume() <= 0) {
      throw new DataValidationException("Volume of a Buy or Sell side quote"
              + " cannot be less than or equal to zero,");
    }
    if (userQuotes.contains(q.getUserName())) {
      buySide.removeQuote(q.getUserName());
      sellSide.removeQuote(q.getUserName());
      updateCurrentMarket();
    }
    addToBook(BookSide.BUY, q.getQuoteSide(BookSide.BUY));
    addToBook(BookSide.SELL, q.getQuoteSide(BookSide.SELL));
    userQuotes.add(q.getUserName());
    updateCurrentMarket();
  }

  /**
   * This method should add the provided Order to the appropriate
   * ProductSideBook.
   *
   * @param o
   */
  public synchronized final void addToBook(Order o)
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException, MessagePublisherException {
    validateInput(o);
    addToBook(o.getSide(), o);
    updateCurrentMarket();
  }

  /**
   * This method needs to determine if the "market" for this stock product has
   * been updated by some market action.
   */
  public synchronized final void updateCurrentMarket()
          throws MessagePublisherException {
    String var = buySide.topOfBookPrice() +
            String.valueOf(buySide.topOfBookVolume()) +
            sellSide.topOfBookPrice() +
            String.valueOf(sellSide.topOfBookVolume());
    if (!lastCurrentMarket.equals(var)) {
      MarketDataDTO current = new MarketDataDTO(symbol,
              (buySide.topOfBookPrice() == null) ?
              PriceFactory.makeLimitPrice("0")
              : buySide.topOfBookPrice(),
              buySide.topOfBookVolume(),
              (sellSide.topOfBookPrice() == null) ?
              PriceFactory.makeLimitPrice("0") : sellSide.topOfBookPrice(),
              sellSide.topOfBookVolume());
      CurrentMarketPublisher.getInstance().publishCurrentMarket(current);
      lastCurrentMarket = var;
    }
  }

  /**
   * This method will take a HashMap of FillMessages passed in and determine
   * from the information it contains what the Last Sale price is.
   *
   * @param fills
   * @return the last sale price
   */
  private synchronized Price determineLastSalePrice(
          HashMap<String, FillMessage> fills) throws ProductBookException {
    validateInput(fills);
    if (fills.isEmpty()) {
      throw new ProductBookException("Argument fills in determineLastSalePrice"
              + " cannot be empty.");
    }
    ArrayList<FillMessage> msgs = new ArrayList<>(fills.values());
    Collections.sort(msgs);
    return msgs.get(0).getPrice();
  }

  /**
   * This method will take a HashMap of FillMessages passed in and determine
   * from the information it contains what the Last Sale quantity (volume) is.
   *
   * @param fills
   * @return
   */
  private synchronized int determineLastSaleQuantity(
          HashMap<String, FillMessage> fills) throws ProductBookException {
    validateInput(fills);
    if (fills.isEmpty()) {
      throw new ProductBookException("Argument fills in"
              + " determineLastSaleQuantity cannot be empty.");
    }
    ArrayList<FillMessage> msgs = new ArrayList<>(fills.values());
    Collections.sort(msgs);
    return msgs.get(0).getVolume();
  }

  /**
   * This method is a key part of the trading system; this method deals with the
   * addition of Tradeables to the Buy/Sell ProductSideBook and handles the
   * results of any trades as a result from that addition.
   *
   * @param side
   * @param trd
   */
  private synchronized void addToBook(BookSide side, Tradeable trd)
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException, MessagePublisherException {
    validateInput(side);
    validateInput(trd);
    if (ProductService.getInstance().getMarketState().equals(
            MarketState.PREOPEN)) {
      if (side.equals(BookSide.BUY)) {
        buySide.addToBook(trd);
      } else {
        sellSide.addToBook(trd);
      }
      return;
    }
    HashMap<String, FillMessage> allFills = null;
    if (side.equals(BookSide.BUY)) {
      allFills = sellSide.tryTrade(trd);
    } else {
      allFills = buySide.tryTrade(trd);
    }
    if (allFills != null && !allFills.isEmpty()) {
      updateCurrentMarket();
      int diff = trd.getOriginalVolume() - trd.getRemainingVolume();
      Price lastSalePrice = determineLastSalePrice(allFills);
      LastSalePublisher.getInstance().publishLastSale(symbol,
              lastSalePrice, diff);
    }
    if (trd.getRemainingVolume() > 0) {
      if (trd.getPrice().isMarket()) {
          MessagePublisher.getInstance().publishCancel(new CancelMessage(
                  trd.getUser(), trd.getProduct(), trd.getPrice(),
                  // is this remaining volume or cancelled volume
                  trd.getRemainingVolume(), trd.getSide() + " Order Cancelled",
                  trd.getSide(), trd.getId()));
          addOldEntry(trd);
      } else {
        if (side.equals(BookSide.BUY)) {
          buySide.addToBook(trd);
        } else {
          sellSide.addToBook(trd);
        }
      }
    }
  }

  private void validateInput(String o)
          throws ProductBookException {
    if (o == null || o.isEmpty()) {
      throw new ProductBookException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(BookSide o) throws ProductBookException {
    if (o == null || !(o instanceof BookSide)) {
      throw new ProductBookException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }

  private void validateInput(Tradeable o) throws ProductBookException {
    if (o == null || !(o instanceof Tradeable)) {
      throw new ProductBookException("Argument must be of type Tradeable and"
              + " cannot be null.");
    }
  }

  private void validateInput(Quote o) throws ProductBookException {
    if (o == null || !(o instanceof Quote)) {
      throw new ProductBookException("Argument must be of type Quote and"
              + " cannot be null.");
    }
  }

  private void validateInput(Order o) throws ProductBookException {
    if (o == null || !(o instanceof Order)) {
      throw new ProductBookException("Argument must be of type Order and"
              + " cannot be null.");
    }
  }

  private void validateInput(Object o) throws ProductBookException {
    if (o == null) {
      throw new ProductBookException("Argument cannot be null.");
    }
  }
}