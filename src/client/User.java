package client;

import client.exceptions.PositionException;
import client.exceptions.TradeableUserDataException;
import constants.GlobalConstants.BookSide;
import java.util.ArrayList;
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
import usercommand.exceptions.AlreadyConnectedException;
import usercommand.exceptions.InvalidConnectionIdException;
import usercommand.exceptions.UserCommandException;
import usercommand.exceptions.UserNotConnectedException;


public interface User {

  /**
   * This will return the String username of this user.
   *
   * @return the user name
   */
  public String getUserName();

  /**
   * This will accept a String stock symbol ("IBM, "GE", etc), a Price object
   * holding the value of the last sale (trade) of that stock, and the quantity
   * (volume) of that last sale. This info is used by "Users" to track stock
   * sales and volumes and is sometimes displayed in a GUI.
   *
   * @param product
   * @param p
   * @param v
   */
  public void acceptLastSale(String product, Price p, int v);

  /**
   * This will accept a FillMessage object which contains information related to
   * an order or quote trade. This is like a receipt sent to the user to
   * document the details when an order or quote-side of theirs trades.
   *
   * @param fm
   */
  public void acceptMessage(FillMessage fm);

  /**
   * This will accept a CancelMessage object which contains information related
   * to an order or quote cancel. This is like a receipt sent to the user to
   * document the details when an order or quote-side of theirs is canceled.
   *
   * @param cm
   */
  public void acceptMessage(CancelMessage cm);

  /**
   * This will accept a String which contains market information related to a
   * Stock Symbol they are interested in.
   *
   * @param message
   */
  public void acceptMarketMessage(String message);

  /**
   * This will accept a stock symbol ("IBM", "GE", etc), a Price object holding
   * the value of the last sale (trade) of that stock, and a "char" indicator of
   * whether the "ticker" price represents an increase or decrease in the
   * Stock's price. This info is used by "users" to track stock price movement,
   * and is sometimes displayed in a GUI.
   *
   * @param product
   * @param p
   * @param direction
   */
  public void acceptTicker(String product, Price p, char direction);

  /**
   * This will accept a String stock symbol ("IBM", "GE", etc.), a Price object
   * holding the current BUY side price for that stock, an int holding the
   * current BUY side volume (quantity), a Price object holding the current SELL
   * side price for that stock, and an int holding the current SELL side volume
   * (quantity). These values as a group tell the user the "current market" for
   * a stock.<br /><br />
   * AMZN:   BUY 220@12.80 and SELL 100@12.85.<br /><br />
   * This info is used by "Users" to update their market display screen so that
   * they are always looking at the most current market data.
   *
   * @param product
   * @param bp
   * @param bv
   * @param sp
   * @param sv
   */
  public void acceptCurrentMarket(String product, Price bp, int bv, Price sp,
          int sv);

  /**
   * Instructs User object to connect to the trading system.
   */
  public void connect() throws AlreadyConnectedException,
          UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException;

  /**
   * Instructs a User object to disconnect from the trading system.
   */
  public void disconnect()
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException;

  /**
   * Requests the opening of the market display if the user is connected.
   */
  public void showMarketDisplay() throws UserNotConnectedException;

  /**
   * Allows the User object to submit a new Order request
   */
  public String submitOrder(String product, Price price,
          int volume, BookSide side) throws TradeableUserDataException,
          UserNotConnectedException, InvalidConnectionIdException,
          InvalidVolumeException, TradeableException,
          InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException,
          TradeProcessorPriceTimeImplException, MessagePublisherException,
          UserCommandException;

  /**
   * Allows the User object to submit a new Order Cancel request.
   *
   * @param product
   * @param side
   * @param orderId
   */
  public void submitOrderCancel(String product, BookSide side, String orderId)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, OrderNotFoundException,
          InvalidVolumeException, ProductBookSideException,
          ProductServiceException, ProductBookException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User object to submit a new Quote request.
   *
   * @param product
   * @param buyPrice
   * @param buyVolume
   * @param sellPrice
   * @param sellVolume
   */
  public void submitQuote(String product, Price buyPrice, int buyVolume,
          Price sellPrice, int sellVolume)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidVolumeException, TradeableException,
          InvalidMarketStateException, NoSuchProductException,
          DataValidationException, InvalidMessageException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, TradeProcessorPriceTimeImplException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User object to submit a new Quote Cancel request.
   *
   * @param product
   */
  public void submitQuoteCancel(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          InvalidMarketStateException, NoSuchProductException,
          InvalidMessageException, ProductBookSideException,
          ProductBookException, ProductServiceException, InvalidVolumeException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User object to subscribe for Current Market for the
   * specified Stock.
   *
   * @param product
   */
  public void subscribeCurrentMarket(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User object to subscribe for Last Sale for the specified Stock.
   *
   * @param product
   */
  public void subscribeLastSale(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User object to subscribe for Messages for the specified Stock.
   *
   * @param product
   */
  public void subscribeMessages(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User object to subscribe for Ticker for the specified Stock.
   *
   * @param product
   */
  public void subscribeTicker(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User object to unsubscribe from Current Market for the specified
   * stock.
   *
   * @param product
   */
  public void unSubscribeCurrentMarket(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User Object to unsubscribe from Last Sale for the specified
   * stock.
   *
   * @param product
   */
  public void unSubscribeLastSale(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User Object to unsubscribe from Messages for the specified
   * stock.
   *
   * @param product
   */
  public void unSubscribeMessages(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;

  /**
   * Allows the User Object to unsubscribe from Ticker for the specified stock.
   *
   * @param product
   */
  public void unSubscribeTicker(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          MessagePublisherException, UserCommandException;



  /**
   * Returns the value of the all Sock the User owns (has bought but not sold).
   *
   * @return the value of the all Sock the User owns
   */
  public Price getAllStockValue() throws PositionException,
          InvalidPriceOperation, PriceException;

  /**
   * Returns the difference between cost of all stock purchases and stock sales.
   *
   * @return the difference between cost of all stock purchases and stock sales
   */
  public Price getAccountCosts();

  /**
   * Returns the difference between current value of all stocks owned and the
   * account costs.
   *
   * @return difference between current value of all stocks owned and
   * account costs
   */
  public Price getNetAccountValue()
          throws PositionException, InvalidPriceOperation, PriceException;

  /**
   * Allows the User object to submit a Book Depth request for the
   * specified stock.
   *
   * @param product
   * @return a Book Depth request for the specified stock
   */
  public String[][] getBookDepth(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          NoSuchProductException, ProductServiceException, UserCommandException;

  /**
   * Allows the User object to query the market state (OPEN, PREOPEN, CLOSED).
   *
   * @return the market state (OPEN, PREOPEN, CLOSED)
   */
  public String getMarketState()
          throws UserNotConnectedException, InvalidConnectionIdException,
          UserCommandException;

  /**
   * Returns a list of order id’s for the orders this user has submitted.
   *
   * @return a list of order id’s for the orders this user has submitted
   */
  public ArrayList<TradeableUserData> getOrderIds();

  /**
   * Returns  available in the trading system.
   *
   * @return a list of the stock products
   */
  public ArrayList<String> getProductList();

  /**
   * Returns the value of the specified stock that this user owns.
   *
   * @param sym
   * @return the value of the specified stock
   */
  public Price getStockPositionValue(String sym)
          throws PositionException, InvalidPriceOperation;

  /**
   * Returns the volume of the specified stock that this user owns.
   *
   * @param product
   * @return the volume of the specified stock
   */
  public int getStockPositionVolume(String product) throws PositionException;

  /**
   * Returns a list of all the Stocks the user owns.
   *
   * @return ArrayList<String>
   */
  public ArrayList<String> getHoldings();

  /**
   * Gets a list of DTO’s containing information on all Orders for this user
   * for the specified product with remaining volume.
   *
   * @param product
   * @return list of DTO’s containing information on all Orders
   */
  public ArrayList<TradeableDTO> getOrdersWithRemainingQty(String product)
          throws UserNotConnectedException, InvalidConnectionIdException,
          ProductBookSideException, ProductBookException,
          ProductServiceException, UserCommandException;
}