package usercommand;

import client.User;
import constants.GlobalConstants.BookSide;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import price.Price;
import publishers.CurrentMarketPublisher;
import publishers.LastSalePublisher;
import publishers.MessagePublisher;
import publishers.TickerPublisher;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.Order;
import tradeable.Quote;
import tradeable.TradeableDTO;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;
import tradeprocessing.productbook.exceptions.DataValidationException;
import tradeprocessing.productbook.exceptions.NoSuchProductException;
import tradeprocessing.productbook.exceptions.OrderNotFoundException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.productservice.ProductService;
import tradeprocessing.productservice.exceptions.InvalidMarketStateException;
import tradeprocessing.productservice.exceptions.ProductServiceException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;
import usercommand.exceptions.AlreadyConnectedException;
import usercommand.exceptions.InvalidConnectionIdException;
import usercommand.exceptions.UserCommandException;
import usercommand.exceptions.UserNotConnectedException;

public class UserCommandService {

  private volatile static UserCommandService instance;

  /**
   * A HashMap<String, Long> to hold user name and connection id pairs.
   */
  HashMap<String, Long> connectedUserIds;

  /**
   * A HashMap<String, User> to hold user name and user object pairs.
   */
  HashMap<String, User> connectedUsers;

  /**
   * A HashMap<String, Long> to hold user name and connection-time pairs
   * (connection time is stored as a long).
   */
  HashMap<String, Long> connectedTime;

  private UserCommandService() {
    connectedUserIds = new HashMap<>();
    connectedUsers = new HashMap<>();
    connectedTime = new HashMap<>();
  }

  public static UserCommandService getInstance() {
    if (instance == null) {
      synchronized(UserCommandService.class) {
        if (instance == null) {
          instance = new UserCommandService();
        }
      }
    }
    return instance;
  }

  /**
   * This is a utility method that will be used by many of the methods in this
   * class to verify the integrity of the user name and connection id passed in
   * with many of the method calls found here.
   *
   * @param userName
   * @param connId
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdExceptions
   */
  private void verifyUser(String userName, long connId)
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException {
    validateInput(userName);
    if (!connectedUserIds.containsKey(userName)) {
      throw new UserNotConnectedException("User not connected to the system");
    }
    if (!connectedUserIds.get(userName).equals((Long) connId)) {
      throw new InvalidConnectionIdException("Connection ID is not valid");
    }
  }

  /**
   * This method will connect the user to the trading system.
   *
   * @param user
   * @return the connectedUserId
   * @throws AlreadyConnectedException
   */
  public synchronized long connect(User user) throws AlreadyConnectedException,
          UserCommandException {
    validateInput(user);
    if (connectedUserIds.containsKey(user.getUserName())) {
      throw new AlreadyConnectedException("User already connected to the"
              + " system.");
    }
    connectedUserIds.put(user.getUserName(), System.nanoTime());
    connectedUsers.put(user.getUserName(), user);
    connectedTime.put(user.getUserName(), System.currentTimeMillis());
    return connectedUserIds.get(user.getUserName());
  }

  /**
   * This method will disconnect the user from the trading system.
   *
   * @param userName
   * @param connId
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   */
  public synchronized void disconnect(String userName, long connId)
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException {
    verifyUser(userName, connId);
    connectedUserIds.remove(userName);
    connectedUsers.remove(userName);
    connectedTime.remove(userName);
  }

  /**
   * Forwards the call of "getBookDepth" to the
   * ProductService.
   *
   * @param userName
   * @param connId
   * @param product
   * @return the book depth for the specified stock
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws NoSuchProductException
   * @throws ProductServiceException
   */
  public String[][] getBookDepth(String userName, long connId, String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          NoSuchProductException, ProductServiceException,
          UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    return ProductService.getInstance().getBookDepth(product);
  }

  /**
   * Forwards the call of "getMarketState" to the ProductService.
   *
   * @param userName
   * @param connId
   * @return the current market state
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   */
  public String getMarketState(String userName, long connId)
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException {
    verifyUser(userName, connId);
    return ProductService.getInstance().getMarketState().toString();
  }

  /**
   * Forwards the call of "getOrdersWithRemainingQty" to the ProductService.
   *
   * @param userName
   * @param connId
   * @param product
   * @return
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws ProductBookSideException
   * @throws ProductBookException
   * @throws ProductServiceException
   */
  public synchronized ArrayList<TradeableDTO> getOrdersWithRemainingQty(
          String userName, long connId, String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    return ProductService.getInstance().getOrdersWithRemainingQty(userName,
            product);
  }

  /**
   * This method should return a sorted list of the available stocks on this
   * system, received from the ProductService.
   *
   * @param userName
   * @param connId
   * @return
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   */
  public ArrayList<String> getProducts(String userName, long connId)
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException {
    verifyUser(userName, connId);
    ArrayList<String> list = ProductService.getInstance().getProductList();
    Collections.sort(list);
    return list;
  }

  /**
   * This method will create an order object using the data passed in, and will
   * forward the order to the ProductService's "submitOrder" method.
   *
   * @param userName
   * @param connId
   * @param product
   * @param price
   * @param volume
   * @param side
   * @return
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws InvalidVolumeException
   * @throws TradeableException
   * @throws InvalidMarketStateException
   * @throws NoSuchProductException
   * @throws InvalidMessageException
   * @throws ProductBookSideException
   * @throws ProductBookException
   * @throws ProductServiceException
   * @throws TradeProcessorPriceTimeImplException
   */
  public String submitOrder(String userName, long connId, String product,
          Price price, int volume, BookSide side)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidVolumeException, TradeableException,
          InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException,
          TradeProcessorPriceTimeImplException, MessagePublisherException,
          UserCommandException {
    validateInput(product);
    validateInput(price);
    validateInput(side);
    verifyUser(userName, connId);
    Order o = new Order(userName, product, price, volume, side);
    return ProductService.getInstance().submitOrder(o);
  }

  /**
   * This method will forward the provided information to the ProductService's
   * "submitOrderCancel" method.
   *
   * @param userName
   * @param connId
   * @param product
   * @param side
   * @param orderId
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws InvalidMarketStateException
   * @throws NoSuchProductException
   * @throws InvalidMessageException
   * @throws OrderNotFoundException
   * @throws InvalidVolumeException
   * @throws ProductBookSideException
   * @throws ProductServiceException
   * @throws ProductBookException
   */
  public void submitOrderCancel(String userName, long connId, String product,
          BookSide side, String orderId) throws UserNotConnectedException,
          InvalidConnectionIdException, InvalidMarketStateException,
          NoSuchProductException, InvalidMessageException,
          OrderNotFoundException, InvalidVolumeException,
          ProductBookSideException, ProductServiceException,
          ProductBookException, MessagePublisherException, UserCommandException {
    validateInput(product);
    validateInput(side);
    validateInput(orderId);
    verifyUser(userName, connId);
    ProductService.getInstance().submitOrderCancel(product, side, orderId);
  }

  /**
   * This method will create a quote object using the data passed in, and will
   * forward the quote to the ProductService's "submitQuote" method.
   *
   * @param userName
   * @param connId
   * @param product
   * @param bPrice
   * @param bVolume
   * @param sPrice
   * @param sVolume
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws InvalidVolumeException
   * @throws TradeableException
   * @throws InvalidMarketStateException
   * @throws NoSuchProductException
   * @throws DataValidationException
   * @throws InvalidMessageException
   * @throws ProductBookSideException
   * @throws ProductBookException
   * @throws ProductServiceException
   * @throws TradeProcessorPriceTimeImplException
   */
  public void submitQuote(String userName, long connId, String product,
          Price bPrice, int bVolume, Price sPrice, int sVolume)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidVolumeException, TradeableException,
          InvalidMarketStateException, NoSuchProductException,
          DataValidationException, InvalidMessageException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, TradeProcessorPriceTimeImplException,
          MessagePublisherException, UserCommandException {
    validateInput(product);
    validateInput(bPrice);
    validateInput(sPrice);
    verifyUser(userName, connId);
    Quote q = new Quote(userName, product, bPrice, bVolume, sPrice, sVolume);
    ProductService.getInstance().submitQuote(q);
  }

  /**
   * This method will forward the provided data to the ProductService's
   * "submitQuoteCancel" method.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws InvalidMarketStateException
   * @throws NoSuchProductException
   * @throws InvalidMessageException
   * @throws ProductBookSideException
   * @throws ProductBookException
   * @throws ProductServiceException
   */
  public void submitQuoteCancel(String userName, long connId, String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException,
          InvalidVolumeException, MessagePublisherException,
          UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    ProductService.getInstance().submitQuoteCancel(userName, product);
  }

  /**
   * This method will forward the subscription request to the
   * CurrentMarketPublisher.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws MessagePublisherException
   */
  public void subscribeCurrentMarket(String userName, long connId,
          String product) throws UserNotConnectedException,
          InvalidConnectionIdException, MessagePublisherException,
          UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    CurrentMarketPublisher.getInstance().subscribe(connectedUsers.get(userName),
            product);
  }

  /**
   * This method will forward the subscription request to the LastSalePublisher.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws MessagePublisherException
   */
  public void subscribeLastSale(String userName, long connId, String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    LastSalePublisher.getInstance().subscribe(connectedUsers.get(userName),
            product);
  }

  /**
   * This method will forward the subscription request to the MessagePublisher.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws MessagePublisherException
   */
  public void subscribeMessages(String userName, long connId, String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    MessagePublisher.getInstance().subscribe(connectedUsers.get(userName),
            product);
  }

  /**
   * This method will forward the subscription request to the TickerPublisher.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws MessagePublisherException
   */
  public void subscribeTicker(String userName, long connId, String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    TickerPublisher.getInstance().subscribe(connectedUsers.get(userName),
            product);
  }

  /**
   * This method will forward the un-subscribe request to the
   * CurrentMarketPublisher.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws MessagePublisherException
   */
  public void unSubscribeCurrentMarket(String userName, long connId,
          String product) throws UserNotConnectedException,
          InvalidConnectionIdException, MessagePublisherException,
          UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    CurrentMarketPublisher.getInstance().unSubscribe(connectedUsers.get(userName),
            product);
  }

  /**
   * This method will forward the un-subscribe request to the LastSalePublisher.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws MessagePublisherException
   */
  public void unSubscribeLastSale(String userName, long connId,
          String product) throws UserNotConnectedException,
          InvalidConnectionIdException, MessagePublisherException, UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    LastSalePublisher.getInstance().unSubscribe(connectedUsers.get(userName),
            product);
  }

  /**
   * This method will forward the un-subscribe request to the TickerPublisher.
   *
   * @param userName
   * @param connId
   * @param product
   * @throws UserNotConnectedException
   * @throws InvalidConnectionIdException
   * @throws MessagePublisherException
   */
  public void unSubscribeTicker(String userName, long connId, String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    TickerPublisher.getInstance().unSubscribe(connectedUsers.get(userName),
            product);
  }

  public void unSubscribeMessages(String userName, long connId,
          String product) throws UserNotConnectedException,
          InvalidConnectionIdException, MessagePublisherException, UserCommandException {
    validateInput(product);
    verifyUser(userName, connId);
    MessagePublisher.getInstance().unSubscribe(connectedUsers.get(userName),
            product);
  }

  private void validateInput(String o)
          throws UserCommandException {
    if (o == null || o.isEmpty()) {
      throw new UserCommandException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(Price o) throws UserCommandException {
    if (o == null || !(o instanceof Price)) {
      throw new UserCommandException("Argument must be of type Price and"
              + " cannot be null.");
    }
  }

  private void validateInput(BookSide o) throws UserCommandException {
    if (o == null || !(o instanceof BookSide)) {
      throw new UserCommandException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }

  private void validateInput(Object o)
          throws UserCommandException {
    if (o == null) {
      throw new UserCommandException("Argument must be of type User and"
        + " cannot be null.");
    }
  }
}