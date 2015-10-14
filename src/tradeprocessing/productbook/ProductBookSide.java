package tradeprocessing.productbook;

import constants.GlobalConstants.BookSide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import price.Price;
import price.PriceFactory;
import publishers.MessagePublisher;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.CancelMessage;
import publishers.messages.FillMessage;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.TrabeableImplFactory;
import tradeable.Tradeable;
import tradeable.TradeableDTO;
import tradeable.exceptions.InvalidVolumeException;
import tradeprocessing.productbook.exceptions.OrderNotFoundException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.tradeprocessor.TradeProcessor;
import tradeprocessing.tradeprocessor.TradeProcessorFactory;
import tradeprocessing.tradeprocessor.exceptions.InvalidProductBookSideValueException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorFactoryException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;


public class ProductBookSide {

  ProductBookSide self = this;

  /**
   * The "side" that this ProductBookSideBehaviors represents - BUY or SELL.
   */
  private BookSide side;

  /**
   * A HashMap<Price, ArrayList<Tradeable>> of book entries for this side.
   */
  private Map<Price, ArrayList<Tradeable>> bookEntries;

  /**
   * Holds keys with no Tradeables to be removed at order/quote cancel.
   */
  private ArrayList<Price> removeBookEntryKeys = new ArrayList<>();

  /**
   * Holds Tradeables to be removed from the bookEntried;
   */
  private HashMap<Price, ArrayList<Tradeable>> removedTradeables =
          new HashMap<>();

  /**
   * A reference to the "TradeProcessor" object which will be used to execute
   * trades against a book side.
   */
  private TradeProcessor processor;

  /**
   * A reference back to the ProductBook object that this ProductBookSideBehaviors
   * belongs to.
   */
  private ProductBook parent;

  public ProductBookSide(ProductBook p, BookSide s)
          throws ProductBookSideException,
          InvalidProductBookSideValueException,
          TradeProcessorFactoryException {
    bookEntries = new HashMap<>();
    setBookSide(s);
    setParentProductBook(p);
    processor = TradeProcessorFactory.createTradeProcessor("price-time", self);
  }

  private void setBookSide(BookSide s) throws ProductBookSideException {
    validateInput(s);
    side = s;
  }

  private void setParentProductBook(ProductBook p) throws ProductBookSideException {
    validateInput(p);
    parent = p;
  }

  /**
   * This method will generate and return an ArrayList of TradableDTO's
   * containing information on all the orders in this ProductBookSideBehaviors that have
   * remaining quantity for the specified user.
   *
   * @param userName
   * @return an ArrayList of TradeAbleDTO's
   */
  public synchronized final ArrayList<TradeableDTO>
          getOrdersWithRemainingQty(String userName)
          throws ProductBookSideException {
    validateInput(userName);
    ArrayList<TradeableDTO> l = new ArrayList<>();
    for (Entry<Price, ArrayList<Tradeable>> row : bookEntries.entrySet()) {
      for (Tradeable t : row.getValue()) {
        if (t.getUser().equals(userName) &&
                t.getRemainingVolume() > 0 && !t.isQuote()) {
          l.add(new TradeableDTO(t.getProduct(), t.getPrice(), t.getOriginalVolume(),
                  t.getRemainingVolume(), t.getCancelledVolume(), t.getUser(),
                  t.getSide(), t.isQuote(), t.getId()));
        }
      }
    }
    return l;
  }

  /**
   * Helper method to sort prices in the bookEntries HashMap.
   *
   * @return an ArrayList of sorted Prices
   */
  private synchronized ArrayList<Price> sortPrices() {
    ArrayList<Price> sorted = new ArrayList<>(bookEntries.keySet());
    Collections.sort(sorted);
    if (side.equals(BookSide.BUY)) {
      Collections.reverse(sorted);
    }
    return sorted;
  }

  /**
   * This method should return an ArrayList of the Tradeables that are at
   * the best price in the "bookEntries" HashMap.
   *
   * @return an ArrayList of Tradeables at the best price in the "bookEntries"
   * HashMap.
   */
  public synchronized final ArrayList<Tradeable> getEntriesAtTopOfBook() {
    if (bookEntries.isEmpty()) { return null; }
    ArrayList<Price> sorted = sortPrices();
    return bookEntries.get(sorted.get(0));
  }

  /**
   * This method should return an array of Strings, where each index holds a
   * "Price x Volume" String.
   *
   * @return a String[] of "Price x Volume"s
   */
  public synchronized final String[] getBookDepth() {
    if (bookEntries.isEmpty()) {
      return new String[]{ "<Empty>"};
    }
    ArrayList<String> str = new ArrayList<>();
    String[] s = new String[bookEntries.size()];
    ArrayList<Price> sorted = sortPrices();
    for (Price p : sorted) {
      ArrayList<Tradeable> tradeable = bookEntries.get(p);
      int sum = 0;
      for (Tradeable t : tradeable) {
        sum += t.getRemainingVolume();
      }
      str.add(p + " x " + sum);
    }
    return str.toArray(s);
  }

  /**
   * This method should return all the Tradeables in this book side at the
   * specified price.
   *
   * @param price
   * @return an ArrayList of all Tradeables at the specified price
   */
  synchronized final ArrayList<Tradeable> getEntriesAtPrice(Price price)
          throws ProductBookSideException {
    validateInput(price);
    if (!bookEntries.containsKey(price)) { return null; }
    return bookEntries.get(price);
  }

  /**
   * This method should return true if the product book
   * (the "bookEntries" HashMap) contains a Market Price
   *
   * @return true or false if the product book contains a Market Price
   */
  public synchronized final boolean hasMarketPrice() {
    return bookEntries.containsKey(PriceFactory.makeMarketPrice());
  }

  /**
   * This method should return true if the ONLY Price in this product's book is
   * a Market Price.
   *
   * @return true or false if this book contains only a Market Price
   */
  public synchronized boolean hasOnlyMarketPrice() {
    return (bookEntries.size() == 1) && bookEntries.containsKey(
            PriceFactory.makeMarketPrice());
  }

  /**
   * This method should return the best Price in the book side. If the
   * "bookEntries" HashMap is empty, then return null.
   *
   * @return return best Price in book otherwise return null
   */
  public synchronized final Price topOfBookPrice() {
    if (bookEntries.isEmpty()) { return null; }
    ArrayList<Price> sorted = sortPrices();
    return sorted.get(0);
  }

  /**
   * This method should return the volume associated with the best Price in
   * the book side. If the "bookEntries" HashMap is empty, then return zero.
   *
   * @return the volume associated with the best Price otherwise 0
   */
  public synchronized final int topOfBookVolume() {
    if (bookEntries.isEmpty()) {
      return 0;
    }
    ArrayList<Price> sorted = sortPrices();
    ArrayList<Tradeable> tradeables = bookEntries.get(sorted.get(0));
    int s = 0;
    for (Tradeable t : tradeables) {
      s += t.getRemainingVolume();
    }
    return s;
  }

  /**
   * Returns true if the product book is empty, false otherwise.
   *
   * @return true is product book is empty, false otherwise
   */
  public synchronized final  boolean isEmpty() {
    return bookEntries.isEmpty();
  }

  /**
   * This method should cancel every Order or QuoteSide at every price in the
   * book.
   */
  public synchronized final void cancelAll()
          throws InvalidMessageException, OrderNotFoundException,
          InvalidVolumeException, ProductBookSideException,
          ProductBookException, MessagePublisherException {
    // Make a temp list of the Prices - the keys
    ArrayList<Price> prices = new ArrayList<>(bookEntries.keySet());
    // Make a copy of the bookEntries HashMap.
    HashMap<Price, ArrayList<Tradeable>> tempHash = new HashMap<>(bookEntries);
    for (Price p : prices) {
      ArrayList<Tradeable> tempList = new ArrayList<>(tempHash.get(p));
      for (Tradeable t: tempList) {
        if (t.isQuote()) {
          submitQuoteCancel(t.getUser());
        } else {
          submitOrderCancel(t.getId());
        }
      }
    }
    removeBookEntryEmptyKeys();
  }

  /**
   * This method should search the book (the “bookEntries” HashMap) for a Quote
   * from the specified user, once found, remove the Quote from the book, and
   * create a TradableDTO using data from that QuoteSide, and return the DTO
   * from the method.
   *
   * @param user
   * @return A TradeableDTO of the quote side if it exists otherwise return null
   */
  public synchronized final TradeableDTO removeQuote(String user)
          throws ProductBookSideException, InvalidVolumeException,
          ProductBookException {
    validateInput(user);
    TradeableDTO quote = null;
    for (Entry<Price, ArrayList<Tradeable>> row : bookEntries.entrySet()) {
      ListIterator<Tradeable> iterator = row.getValue().listIterator();
      int size = row.getValue().size();
      while (iterator.hasNext()) {
        Tradeable t = iterator.next();
        if (t.isQuote() && t.getUser().equals(user)) {
          quote = new TradeableDTO(t.getProduct(), t.getPrice(), t.getOriginalVolume(),
                  t.getRemainingVolume(), t.getCancelledVolume(), t.getUser(),
                  t.getSide(), false, t.getId());
          if (!removedTradeables.containsKey(row.getKey())) {
            removedTradeables.put(row.getKey(), new ArrayList<Tradeable>());
          }
          removedTradeables.get(row.getKey()).add(t);
          addOldEntry(t);
          if (size == 1) {
            removeBookEntryKeys.add(row.getKey());
          }
        }
      }
    }
    removeBookEntryEmptyKeys();
    removeTradeablesFromBookEntries();
    return quote;
  }

  /**
   * This method should cancel the Order (if possible) that has the specified
   * identifier.
   *
   * @param orderId
   */
  public synchronized final void submitOrderCancel(String orderId)
          throws InvalidMessageException, OrderNotFoundException,
          InvalidVolumeException, ProductBookSideException,
          ProductBookException, MessagePublisherException {
    validateInput(orderId);
    boolean isFound = false;
    for (Entry<Price, ArrayList<Tradeable>> row : bookEntries.entrySet()) {
      ListIterator<Tradeable> iterator = row.getValue().listIterator();
      int size = row.getValue().size();
      while (iterator.hasNext()) {
        Tradeable t = iterator.next();
        if (t.getId().equals(orderId)) {
          isFound = true;
          MessagePublisher.getInstance().publishCancel(new CancelMessage(
                  t.getUser(), t.getProduct(), t.getPrice(),
                  // is this remaining volume or cancelled volume
                  t.getRemainingVolume(), t.getSide() + " Order Cancelled",
                  t.getSide(), t.getId()));
          if (!removedTradeables.containsKey(row.getKey())) {
            removedTradeables.put(row.getKey(), new ArrayList<Tradeable>());
          }
          removedTradeables.get(row.getKey()).add(t);
          addOldEntry(t);
          if (size == 1) {
            removeBookEntryKeys.add(row.getKey());
          }
        }
      }
    }
    removeBookEntryEmptyKeys();
    removeTradeablesFromBookEntries();
    if (!isFound) {
      parent.checkTooLateToCancel(orderId);
    }
  }

  /**
   * This method should cancel the QuoteSide (if possible) that has the
   * specified userName.
   *
   * @param userName
   */
  public synchronized final void submitQuoteCancel(String userName)
          throws InvalidMessageException, ProductBookSideException,
          InvalidVolumeException, ProductBookException,
          MessagePublisherException {
    validateInput(userName);
    TradeableDTO quote = removeQuote(userName);
    if (quote != null) {
      MessagePublisher.getInstance().publishCancel(new CancelMessage(
              quote.user, quote.product, quote.price, quote.remainingVolume,
              "Quote " + quote.side + "-Side Cancelled.", quote.side,
              quote.id));
    }
  }

  /**
   * This method should add the Tradable passed in to the "parent" product
   * book's "old entries" list.
   *
   * @param t
   */
  public final void addOldEntry(Tradeable t)
          throws InvalidVolumeException, ProductBookSideException,
          ProductBookException {
    validateInput(t);
    parent.addOldEntry(t);
  }

  /**
   * This method should add the Tradable passed in to the book
   * (the "bookEntries" HashMap).
   *
   * @param trd
   */
  public synchronized final void addToBook(Tradeable trd)
          throws ProductBookSideException {
    validateInput(trd);
    if (bookEntries.containsKey(trd.getPrice())) {
      bookEntries.get(trd.getPrice()).add(trd);
    } else {
      ArrayList<Tradeable> l = new ArrayList<>();
      l.add(trd);
      bookEntries.put(trd.getPrice(), l);
    }
  }

  /**
   * This method will attempt to trade the provided Tradable against entries in
   * this ProductBookSide.
   *
   * @param trd
   * @return a HashMap of filled messages
   */
  public HashMap<String, FillMessage> tryTrade(Tradeable trd)
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException, MessagePublisherException {
    validateInput(trd);
    HashMap<String, FillMessage> allFills;
    if (side.equals(BookSide.BUY)) {
      allFills = trySellAgainstBuySideTrade(trd);
    } else {
      allFills = tryBuyAgainstSellSideTrade(trd);
    }
    for (Entry<String, FillMessage> row : allFills.entrySet()) {
      MessagePublisher.getInstance().publishFill(row.getValue());
    }
    return allFills;
  }

  /**
   * This method will try to fill the SELL side Tradable passed in against the
   * content of the book.
   *
   * @param trd
   * @return a HashMap of filled messages
   */
  public synchronized HashMap<String, FillMessage>
          trySellAgainstBuySideTrade(Tradeable trd)
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException {
    validateInput(trd);
    HashMap<String, FillMessage> allFills = new HashMap<>();
    HashMap<String, FillMessage> fillMsgs = new HashMap<>();
    while((trd.getRemainingVolume() > 0 && !bookEntries.isEmpty()) &&
            (trd.getPrice().lessOrEqual(topOfBookPrice()) ||
            trd.getPrice().isMarket())) {
      HashMap<String, FillMessage> temp = processor.doTrade(trd);
      fillMsgs = mergeFills(fillMsgs, temp);
    }
    allFills.putAll(fillMsgs);
    return allFills;
  }

  /**
   * This method will try to fill the BUY side Tradable passed in against the
   * content of the book.
   *
   * @param trd
   * @return a HashMap of fill messages
   */
  public synchronized HashMap<String, FillMessage>
          tryBuyAgainstSellSideTrade(Tradeable trd)
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException {
    validateInput(trd);
    HashMap<String, FillMessage> allFills = new HashMap<>();
    HashMap<String, FillMessage> fillMsgs = new HashMap<>();
    while((trd.getRemainingVolume() > 0 && !bookEntries.isEmpty()) &&
            (trd.getPrice().greaterOrEqual(topOfBookPrice()) ||
            trd.getPrice().isMarket())) {
      HashMap<String, FillMessage> temp = processor.doTrade(trd);
      fillMsgs = mergeFills(fillMsgs, temp);
    }
    allFills.putAll(fillMsgs);
    return allFills;
  }

  /**
   * This method is designed to merge multiple fill messages together into
   * one consistent HashMap.
   *
   * @param existing
   * @param newOnes
   * @return a HashMap of fill messages
   */
  private HashMap<String, FillMessage> mergeFills(
          HashMap<String, FillMessage> existing,
          HashMap<String, FillMessage> newOnes)
          throws InvalidMessageException, ProductBookSideException {
    validateInput(newOnes);
    if (newOnes.isEmpty()) {
      throw new ProductBookSideException("Argument newOnes cannot be empty in"
              + " mergeFills.");
    }
    validateInput(existing);
    if (existing.isEmpty()) {
      return new HashMap<>(newOnes);
    }
    HashMap<String, FillMessage> results = new HashMap<>(existing);
    for (String key : newOnes.keySet()) { // For each Trade Id key in the "newOnes" HashMap
      if (!existing.containsKey(key)) { // If the "existing" HashMap does not have that key...
        results.put(key, newOnes.get(key)); // ...then simply add this entry to the "results" HashMap
      } else { // Otherwise, the "existing" HashMap does have that key – we need to update the data
        FillMessage fm = results.get(key); // Get the FillMessage from the "results" HashMap
        // NOTE – for the below, you will need to make these 2 FillMessage methods "public"!
        fm.setVolume(newOnes.get(key).getVolume()); // Update the fill volume
        fm.setDetails(newOnes.get(key).getDetails()); // Update the fill details
      }
    }
    return results;
  }

  /**
   * This method will remove an key/value pair from the book (the "bookEntries"
   * HashMap) if the ArrayList associated with the Price passed in is empty.
   *
   * @param p
   */
  public synchronized void clearIfEmpty(Price p)
          throws ProductBookSideException {
    validateInput(p);
    if (bookEntries.get(p).isEmpty()) {
      bookEntries.remove(p);
    }
  }

  /**
   * This method is design to remove the Tradable passed in from the book
   * (when it has been traded or cancelled).
   *
   * @param t
   */
  public synchronized void removeTradeable(Tradeable t)
          throws ProductBookSideException {
    validateInput(t);
    ArrayList<Tradeable> entries = bookEntries.get(t.getPrice());
    if (entries == null) { return; }
    boolean removeOp = entries.remove(t);
    if (!removeOp) { return; }
    if (entries.isEmpty()) {
      clearIfEmpty(t.getPrice());
    }
  }

  /**
   * Removes all empty keys from the bookEntries HashMap.
   */
  private synchronized void removeBookEntryEmptyKeys()
          throws ProductBookSideException {
    for (Price key : removeBookEntryKeys) {
      //bookEntries.remove(key);
      clearIfEmpty(key);
    }
    removeBookEntryKeys = new ArrayList<>();
  }

  /**
   * Remove tradeables from the book entries.
   */
  private synchronized void removeTradeablesFromBookEntries()
          throws ProductBookSideException {
    for (Entry<Price, ArrayList<Tradeable>> row :
            removedTradeables.entrySet()) {
      if (bookEntries.containsKey(row.getKey()) &&
              bookEntries.get(row.getKey()) != null) {
        for (Tradeable t : row.getValue()) {
          //bookEntries.get(row.getKey()).remove(t);
          removeTradeable(t);
        }
      }
    }
    removedTradeables.clear();
  }

  private void validateInput(String o)
          throws ProductBookSideException {
    if (o == null || o.isEmpty()) {
      throw new ProductBookSideException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(ProductBook o) throws ProductBookSideException {
    if (o == null || !(o instanceof ProductBook)) {
      throw new ProductBookSideException("Argument cannot be null or not instance of ProductBook");
    }
  }

  private void validateInput(BookSide o) throws ProductBookSideException {
    if (o == null || !(o instanceof BookSide)) {
      throw new ProductBookSideException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }

  private void validateInput(Price o) throws ProductBookSideException {
    if (o == null || !(o instanceof Price)) {
      throw new ProductBookSideException("Argument must be of type Price and"
              + " cannot be null.");
    }
  }

  private void validateInput(Tradeable o) throws ProductBookSideException {
    if (o == null || !(o instanceof Tradeable)) {
      throw new ProductBookSideException("Argument must be of type Tradeable and"
              + " cannot be null.");
    }
  }

  private void validateInput(Object o) throws ProductBookSideException {
    if (o == null) {
      throw new ProductBookSideException("Argument cannot be null.");
    }
  }
}