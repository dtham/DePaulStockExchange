package client;

import client.exceptions.PositionException;
import client.exceptions.TradeableUserDataException;
import client.exceptions.UserException;
import constants.GlobalConstants.BookSide;
import gui.UserDisplayManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import price.Price;
import price.exceptions.InvalidPriceOperation;
import price.exceptions.PriceException;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.CancelMessage;
import publishers.messages.FillMessage;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.TradeableDTO;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;
import tradeprocessing.productbook.exceptions.DataValidationException;
import tradeprocessing.productbook.exceptions.NoSuchProductException;
import tradeprocessing.productbook.exceptions.OrderNotFoundException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.productservice.exceptions.InvalidMarketStateException;
import tradeprocessing.productservice.exceptions.ProductServiceException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;
import usercommand.UserCommandService;
import usercommand.exceptions.AlreadyConnectedException;
import usercommand.exceptions.InvalidConnectionIdException;
import usercommand.exceptions.UserCommandException;
import usercommand.exceptions.UserNotConnectedException;


public class UserImpl implements User {

  /**
   * A String to hold their user name.
   */
  private String userName;

  /**
   * A long value to hold their "connection id" – provided to them when they
   * connect to the system.
   */
  private long connectionId;

  /**
   * A String list of the stocks available in the trading system. The user
   * fills this list once connected based upon data received from the
   * trading system.
   */
  ArrayList<String> stocks = null;

  /**
   * A list of TradableUserData objects that contains information on the orders
   * this user has submitted (needed for cancelling).
   */
  ArrayList<TradeableUserData> trades;

  /**
   * A reference to a Position object (part of this assignment) which holds
   * the values of the users stocks, costs, etc.
   */
  Position position;

  private static final Logger log = Logger.getLogger(UserImpl.class.getName());

  /**
   * A reference to a UserDisplayManager object (part of this assignment) that
   * acts as a façade between the user and the market display.
   */
  UserDisplayManager udm;

  public UserImpl(String userName) throws UserException {
    setUserName(userName);
    position = new Position();
    trades = new ArrayList<>();
  }

  private void setUserName(String name) throws UserException {
    validateInput(name);
    userName = name;
  }

  /**
   * This method should return the String username of this user.
   *
   * @return the userName
   */
  @Override
  public final String getUserName() {
    return userName;
  }

  /**
   * This method should call the user display manager's updateLastSale method,
   * passing the same 3 parameters that were passed in. Then, call the Position
   * object's updateLastSale method passing the product and price passed in.
   *
   * @param product
   * @param price
   * @param volume
   */
  @Override
  public final void acceptLastSale(String product, Price price, int volume) {
    try {
      udm.updateLastSale(product, price, volume);
      position.updateLastSale(product, price);
    } catch(PositionException e) {
      log.log(Level.SEVERE, null, e);
    }
  }

  /**
   * This method will display the Fill Message in the market display and will
   * forward the data to the Position object.
   *
   * @param fm
   */
  @Override
  public final void acceptMessage(FillMessage fm) {
    try {
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      String msg = "{" + timestamp.toString() + "} Fill Message: " +
              fm.getSide() + " "+ fm.getVolume() + " " + fm.getProduct()
              + " at " + fm.getPrice() + ", " + fm.getDetails()
              + " [Tradeable Id: " + fm.getID() + "]";
      udm.updateMarketActivity(msg);
      position.updatePosition(fm.getProduct(), fm.getPrice(), fm.getSide(),
              fm.getVolume());
    } catch(PositionException | InvalidPriceOperation | PriceException e) {
      log.log(Level.SEVERE, null, e);
    }
  }

  /**
   * This method will display the Cancel Message in the market display.
   *
   * @param cm
   */
  @Override
  public final void acceptMessage(CancelMessage cm) {
    try {
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      String msg = "{" + timestamp.toString() + "} Cancel Message: " +
              cm.getSide() + " " + cm.getVolume() + " " + cm.getProduct()
              + " at " + cm.getPrice() + ", " +  cm.getDetails() +
              " [Tradeable Id: " + cm.getID() + "]";
      udm.updateMarketActivity(msg);
    } catch(Exception e) {
      log.log(Level.SEVERE, null, e);
    }
  }

  /**
   * This method will display the Market Message in the market display.
   *
   * @param message
   */
  @Override
  public final void acceptMarketMessage(String message) {
    try {
      udm.updateMarketState(message);
    } catch(Exception e) {
      log.log(Level.SEVERE, null, e);
    }
  }

  /**
   * This method will display the Ticker data in the market display.
   *
   * @param product
   * @param price
   * @param direction
   */
  @Override
  public final void acceptTicker(String product, Price price, char direction) {
    try {
      udm.updateTicker(product, price, direction);
    } catch(Exception e) {
      log.log(Level.SEVERE, null, e);
    }
  }

  /**
   * This method will display the Current Market data in the market display.
   *
   * @param product
   * @param bPrice
   * @param bVolume
   * @param sPrice
   * @param sVolume
   */
  @Override
  public final void acceptCurrentMarket(String product, Price bPrice, int bVolume,
          Price sPrice, int sVolume) {
    try {
      udm.updateMarketData(product, bPrice, bVolume, sPrice, sVolume);
    } catch(Exception e) {
      log.log(Level.SEVERE, null, e);
    }
  }

  /**
   * This method will connect the user to the trading system.
   */
  @Override
  public final void connect() throws AlreadyConnectedException,
          UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException {
    connectionId = UserCommandService.getInstance().connect(this);
    stocks = UserCommandService.getInstance()
            .getProducts(userName, connectionId);
  }

  /**
   * This method will disconnect the user from the trading system.
   */
  @Override
  public final void disconnect()
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException {
    UserCommandService.getInstance().disconnect(userName, connectionId);
  }

  /**
   * This method qwill activate the market display.
   */
  @Override
  public final void showMarketDisplay()
          throws UserNotConnectedException {
    if (stocks == null) {
      throw new UserNotConnectedException("User currently not connected.");
    }
    if (udm == null) {
      udm = new UserDisplayManager(this);
    }
    udm.showMarketDisplay();
  }

  /**
   * This method forwards the new order request to the user command service and
   * saves the resulting order id.
   *
   * @param product
   * @param price
   * @param volume
   * @param side
   * @return
   */
  @Override
  public final String submitOrder(String product, Price price, int volume,
          BookSide side) throws TradeableUserDataException,
          UserNotConnectedException, InvalidConnectionIdException,
          InvalidVolumeException, TradeableException,
          InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException,
          TradeProcessorPriceTimeImplException, MessagePublisherException,
          UserCommandException {
    String id = UserCommandService.getInstance().submitOrder(userName,
            connectionId, product, price, volume, side);
    trades.add(new TradeableUserData(userName, product, side, id));
    return id;
  }

  /**
   * This method forwards the order cancel request to the user command service.
   *
   * @param product
   * @param side
   * @param orderId
   */
  @Override
  public final void submitOrderCancel(String product, BookSide side,
        String orderId) throws UserNotConnectedException,
          InvalidConnectionIdException, InvalidMarketStateException,
          NoSuchProductException, InvalidMessageException,
          OrderNotFoundException, InvalidVolumeException,
          ProductBookSideException, ProductServiceException,
          ProductBookException, MessagePublisherException,
          UserCommandException {
    UserCommandService.getInstance().submitOrderCancel(userName, connectionId,
            product, side, orderId);
  }

  /**
   * This method forwards the new quote request to the user command service
   *
   * @param product
   * @param buyPrice
   * @param buyVolume
   * @param sellPrice
   * @param sellVolume
   */
  @Override
  public final void submitQuote(String product, Price buyPrice,
                int buyVolume, Price sellPrice, int sellVolume)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidVolumeException, TradeableException,
          InvalidMarketStateException, NoSuchProductException,
          DataValidationException, InvalidMessageException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, TradeProcessorPriceTimeImplException,
          MessagePublisherException, UserCommandException {
    UserCommandService.getInstance().submitQuote(userName, connectionId,
            product, buyPrice, buyVolume, sellPrice, sellVolume);
  }

  /**
   * This method forwards the quote cancel request to the user command service.
   *
   * @param product
   */
  @Override
  public final void submitQuoteCancel(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException,
          InvalidVolumeException, MessagePublisherException,
          UserCommandException {
    UserCommandService.getInstance().submitQuoteCancel(userName, connectionId,
            product);
  }

  /**
   * This method forwards the current market subscription to the user command
   * service.
   *
   * @param product
   */
  @Override
  public final void subscribeCurrentMarket(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    UserCommandService.getInstance().subscribeCurrentMarket(userName,
            connectionId, product);
  }


  @Override
  public void unSubscribeCurrentMarket(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    UserCommandService.getInstance().unSubscribeCurrentMarket(userName,
            connectionId, product);
  }

  /**
   * This method forwards the last sale subscription to the user command
   * service.
   *
   * @param product
   */
  @Override
  public final void subscribeLastSale(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    UserCommandService.getInstance().subscribeLastSale(userName, connectionId,
            product);
  }

  /**
   * This method forwards the last sale unsubscription to the user command
   * service.
   *
   * @param product
   */
  @Override
  public void unSubscribeLastSale(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    UserCommandService.getInstance().unSubscribeLastSale(userName, connectionId,
            product);
  }

  /**
   * This method forwards the message subscription to the user command service.
   *
   * @param product
   */
  @Override
  public final void subscribeMessages(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    UserCommandService.getInstance().subscribeMessages(userName, connectionId,
            product);
  }

  /**
   * This method forwards the message unsubscription to the user command service.
   *
   * @param product
   */
  @Override
  public void unSubscribeMessages(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
     UserCommandService.getInstance().unSubscribeMessages(userName, connectionId,
            product);
  }

  /**
   * This method forwards the ticker subscription to the user command service.
   *
   * @param product
   */
  @Override
  public final void subscribeTicker(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
    UserCommandService.getInstance().subscribeTicker(userName, connectionId,
            product);
  }

  /**
   * This method forwards the ticker unsubscription to the user command service.
   *
   * @param product
   */
  @Override
  public void unSubscribeTicker(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException {
     UserCommandService.getInstance().unSubscribeTicker(userName, connectionId,
            product);
  }

  /**
   * Returns the value of the all Sock the User owns (has bought but not sold).
   *
   * @return the price of all stock the User owns
   */
  @Override
  public final Price getAllStockValue()
          throws InvalidPriceOperation, PositionException, PriceException {
    return position.getAllStockValue();
  }

  /**
   * Returns the difference between cost of all stock purchases and stock sales.
   *
   * @return the difference b/t cost of all stock purchases vs. stock sales
   */
  @Override
  public final Price getAccountCosts() {
    return position.getAccountCosts();
  }

  /**
   * Returns the difference between current value of all stocks owned and the
   * account costs.
   *
   * @return the value of the account
   */
  @Override
  public final Price getNetAccountValue()
          throws PositionException, InvalidPriceOperation, PriceException {
    return position.getNetAccountValue();
  }

  /**
   * Allows the User object to submit a Book Depth request for the specified
   * stock.
   *
   * @param product
   * @return the book depth of the specified stock
   */
  @Override
  public final String[][] getBookDepth(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          NoSuchProductException, ProductServiceException,
          UserCommandException {
    return UserCommandService.getInstance().getBookDepth(userName, connectionId,
            product);
  }

  /**
   * Allows the User object to query the market state (OPEN, PREOPEN, CLOSED).
   *
   * @return the market state
   */
  @Override
  public final String getMarketState()
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException {
    return UserCommandService.getInstance().getMarketState(userName,
            connectionId);
  }

  /**
   * Returns a list of order id’s (a data member) for the orders this user has
   * submitted.
   *
   * @return a list of order id's this user has made
   */
  @Override
  public final ArrayList<TradeableUserData> getOrderIds() {
    return trades;
  }

  /**
   * Returns a list of stocks (a data member) available in the trading system.
   *
   * @return a list of all products available in the system
   */
  @Override
  public final ArrayList<String> getProductList() {
    return stocks;
  }

  /**
   * Returns the value of the specified stock that this user owns.
   *
   * @param sym
   * @return the value of the specified stock this user owns
   */
  @Override
  public final Price getStockPositionValue(String sym)
          throws PositionException, InvalidPriceOperation {
    return position.getStockPositionValue(sym);
  }

  /**
   * Returns the volume of the specified stock that this user owns.
   *
   * @param product
   * @return the volume of the specified stock this user owns
   */
  @Override
  public final int getStockPositionVolume(String product)
          throws PositionException{
    return position.getStockPositionVolume(product);
  }

  /**
   * Returns a list of all the Stocks the user owns.
   *
   * @return a list of all stocks the user owns
   */
  @Override
  public final ArrayList<String> getHoldings() {
    return position.getHoldings();
  }

  /**
   * Gets a list of DTO’s containing information on all Orders for this user for
   * the specified product with remaining volume.
   *
   * @param product
   * @return a list of DTO’s containing information on all Orders for this user
   */
  @Override
  public final ArrayList<TradeableDTO> getOrdersWithRemainingQty(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, UserCommandException {
    return UserCommandService.getInstance().getOrdersWithRemainingQty(userName,
            connectionId, product);
  }

  private void validateInput(String o)
          throws UserException {
    if (o == null || o.isEmpty()) {
      throw new UserException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }
}