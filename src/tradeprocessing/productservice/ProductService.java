package tradeprocessing.productservice;

import constants.GlobalConstants.BookSide;
import constants.GlobalConstants.MarketState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import publishers.MessagePublisher;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.MarketDataDTO;
import publishers.messages.MarketMessage;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.Order;
import tradeable.Quote;
import tradeable.TradeableDTO;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;
import tradeprocessing.productbook.ProductBook;
import tradeprocessing.productbook.exceptions.DataValidationException;
import tradeprocessing.productbook.exceptions.NoSuchProductException;
import tradeprocessing.productbook.exceptions.OrderNotFoundException;
import tradeprocessing.productbook.exceptions.ProductAlreadyExistsException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.productservice.exceptions.InvalidMarketStateException;
import tradeprocessing.productservice.exceptions.InvalidMarketStateTransitionException;
import tradeprocessing.productservice.exceptions.ProductServiceException;
import tradeprocessing.tradeprocessor.exceptions.InvalidProductBookSideValueException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorFactoryException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;


public class ProductService {
  private volatile static ProductService instance;

  /**
   * As this class must own all the product books, you will need a structure
   * that contains all product books, accessible by the stock symbol name.
   */
  private HashMap<String, ProductBook> allBooks = new HashMap<>();

  /**
   * As this class must maintain a data member that holds the current market
   * state.
   */
  private MarketState state = MarketState.CLOSED;

  /**
   * As this is a Façade, this class should be implemented as a thread-safe
   * singleton.
   *
   * @return the instance of the ProductService
   */
  public static ProductService getInstance() {
    if (instance == null) {
      synchronized (ProductService.class) {
        if (instance == null) {
          instance = new ProductService();
        }
      }
    }
    return instance;
  }

  /**
   * This method will return a List of TradableDTOs containing any orders with
   * remaining quantity for the user and the stock specified.
   *
   * @param userName
   * @param product
   * @return a list of TradeableDTOs
   */
  public synchronized ArrayList<TradeableDTO> getOrdersWithRemainingQty(
          String userName, String product)
          throws ProductBookSideException, ProductBookException,
          ProductServiceException {
    validateInput(userName);
    validateInput(product);
    return allBooks.get(product).getOrdersWithRemainingQty(userName);
  }

  /**
   * This method will return a List of MarketDataDTO containing the best buy
   * price/volume and sell price/volume for the specified stock product.
   *
   * @param product
   * @return a List of MarketDataDTO
   */
  public synchronized MarketDataDTO getMarketData(String product)
          throws ProductServiceException {
    validateInput(product);
    return allBooks.get(product).getMarketData();
  }

  /**
   * This method should simply return the current market state.
   *
   * @return the current market state
   */
  public synchronized MarketState getMarketState() {
    return state;
  }

  /**
   * Returns the Bookdepth for the product passed in.
   *
   * @param product
   * @return a 2-D array of the product book depth
   */
  public synchronized String[][] getBookDepth(String product)
          throws NoSuchProductException, ProductServiceException {
    validateInput(product);
    if (!allBooks.containsKey(product)) {
       throw new NoSuchProductException("The product: " + product +
               "; does not exist in the product book.");
    }
    return allBooks.get(product).getBookDepth();
  }

  /**
   * This method should simply return an Arraylist containing all the keys in
   * the "allBooks" HashMap.
   *
   * @return an ArrayList of all Products
   */
  public synchronized ArrayList<String> getProductList() {
    return new ArrayList<>(allBooks.keySet());
  }

  private synchronized boolean isValidTransition(MarketState ms)
          throws ProductServiceException {
    validateInput(ms);
    ArrayList<MarketState> trans = new ArrayList<>(Arrays.asList(
            MarketState.CLOSED, MarketState.PREOPEN, MarketState.OPEN ));
    int msPass = trans.indexOf(ms);
    int msCurrent = trans.indexOf(state);
    int diff = msPass - msCurrent;
    if (msCurrent == 2 && msPass == 0) { return true; }
    if (msCurrent < msPass && diff == 1) { return true; }
    return false;
  }

  /**
   * This method should update the market state to the new value passed in.
   *
   * @param ms
   */
  public synchronized void setMarketState(MarketState ms)
          throws InvalidMarketStateTransitionException, InvalidMessageException,
          OrderNotFoundException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, TradeProcessorPriceTimeImplException,
          TradeableException, MessagePublisherException {
    validateInput(ms);
    if (!isValidTransition(ms)) {
      throw new InvalidMarketStateTransitionException("The market state transition: " +
              ms + "; is invalid, current market state is: " + state);
    }
    state = ms;
    MessagePublisher.getInstance().publishMarketMessage(
            new MarketMessage(state));
    if (state.equals(MarketState.OPEN)) {
      for (Entry<String, ProductBook> row : allBooks.entrySet()) {
        row.getValue().openMarket();
      }
    }
    if (state.equals(MarketState.CLOSED)) {
      for (Entry<String, ProductBook> row : allBooks.entrySet()) {
        row.getValue().closeMarket();
      }
    }
  }

  /**
   * This method will create a new stock product that can be used for trading.
   * This will result in the creation of a ProductBook object, and a new entry
   * in the "allBooks" HashMap.
   *
   * @param product
   */
  public synchronized void createProduct(String product)
          throws DataValidationException, ProductAlreadyExistsException,
          ProductBookException, ProductBookSideException,
          InvalidProductBookSideValueException, TradeProcessorFactoryException,
          ProductServiceException {
    validateInput(product);
    if (allBooks.containsKey(product)) {
      throw new ProductAlreadyExistsException("Product " + product +
              " already exists in the ProductBook.");
    }
    allBooks.put(product, new ProductBook(product));
  }

  /**
   * This method should forward the provided Quote to the appropriate product
   * book.
   *
   * @param q
   */
  public synchronized void submitQuote(Quote q)
          throws InvalidMarketStateException, NoSuchProductException,
          InvalidVolumeException, DataValidationException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException,
          TradeProcessorPriceTimeImplException, TradeableException,
          MessagePublisherException {
    validateInput(q);
    if (state.equals(MarketState.CLOSED)) {
      throw new InvalidMarketStateException("Marekt is closed!");
    }
    if (!allBooks.containsKey(q.getProduct())) {
      throw new NoSuchProductException("Product does not exist in any book.");
    }
    allBooks.get(q.getProduct()).addToBook(q);
  }


  /**
   * This method should forward the provided Order to the appropriate product
   * book.
   *
   * @param o
   * @return the string id of the order
   */
  public synchronized String submitOrder(Order o)
          throws InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, TradeProcessorPriceTimeImplException,
          MessagePublisherException {
    validateInput(o);
    if (state.equals(MarketState.CLOSED)) {
      throw new InvalidMarketStateException("Marekt is closed!");
    }
    if (state.equals(MarketState.PREOPEN) && o.getPrice().isMarket()) {
      throw new InvalidMarketStateException("Marekt is pre-open, cannot submit"
              + " MKT orders at this time.");
    }
    if (!allBooks.containsKey(o.getProduct())) {
      throw new NoSuchProductException("Product does not exist in any book.");
    }
    allBooks.get(o.getProduct()).addToBook(o);
    return o.getId();
  }

  /**
   * This method should forward the provided Order Cancel to the appropriate
   * product book.
   *
   * @param product
   * @param side
   * @param orderId
   * @throws InvalidMarketStateException
   * @throws NoSuchProductException
   * @throws InvalidMessageException
   * @throws OrderNotFoundException
   * @throws InvalidVolumeException
   */
  public synchronized void submitOrderCancel(String product, BookSide side,
          String orderId) throws InvalidMarketStateException,
          NoSuchProductException, InvalidMessageException,
          OrderNotFoundException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, MessagePublisherException {
    validateInput(product);
    validateInput(side);
    validateInput(orderId);
    if (state.equals(MarketState.CLOSED)) {
      throw new InvalidMarketStateException("Marekt is closed!");
    }
    if (!allBooks.containsKey(product)) {
      throw new NoSuchProductException("Product does not exist in any book.");
    }
    allBooks.get(product).cancelOrder(side, orderId);
  }

  /**
   * This method should forward the provided Quote Cancel to the appropriate
   * product book.
   *
   * @param userName
   * @param product
   */
  public synchronized void submitQuoteCancel(String userName, String product)
          throws InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException,
          InvalidVolumeException, MessagePublisherException {
    validateInput(userName);
    validateInput(product);
    if (state.equals(MarketState.CLOSED)) {
      throw new InvalidMarketStateException("Marekt is closed!");
    }
    if (!allBooks.containsKey(product)) {
      throw new NoSuchProductException("Product does not exist in any book.");
    }
    allBooks.get(product).cancelQuote(userName);
  }

  private void validateInput(String o)
          throws ProductServiceException {
    if (o == null || o.isEmpty()) {
      throw new ProductServiceException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(MarketState o) throws ProductServiceException {
    if (o == null || !(o instanceof MarketState)) {
      throw new ProductServiceException("Argument cannot be null or not instance of MarketState");
    }
  }

  private void validateInput(Quote o) throws ProductServiceException {
    if (o == null || !(o instanceof Quote)) {
      throw new ProductServiceException("Argument cannot be null or not instance of Quote");
    }
  }

  private void validateInput(Order o) throws ProductServiceException {
    if (o == null || !(o instanceof Order)) {
      throw new ProductServiceException("Argument cannot be null or not instance of Order");
    }
  }

  private void validateInput(BookSide o) throws ProductServiceException {
    if (o == null || !(o instanceof BookSide)) {
      throw new ProductServiceException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }
}