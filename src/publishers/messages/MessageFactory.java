package publishers.messages;

import constants.GlobalConstants.BookSide;
import constants.GlobalConstants.MarketState;
import price.Price;
import publishers.messages.exceptions.InvalidMessageException;


class MessageFactory {

  /**
   * Creates a CancelMessageImpl object the cancel message will delegate to.
   *
   * @param user
   * @param product
   * @param price
   * @param volume
   * @param details
   * @param side
   * @param id
   * @return a cancel message impl object.
   * @throws InvalidMessageException
   */
  public static CancelMessageImpl createCancelMessageImpl(String user,
          String product, Price price, int volume, String details,
          BookSide side, String id)
          throws InvalidMessageException {
    return new CancelMessageImpl(user, product, price, volume,
            details, side, id);
  }

    /**
   * Creates a FillMessageImpl object the fill message will delegate to.
   *
   * @param user
   * @param product
   * @param price
   * @param volume
   * @param details
   * @param side
   * @param id
   * @return a fill message impl object.
   * @throws InvalidMessageException
   */
  public static FillMessageImpl createFillMessageImpl(String user,
          String product, Price price, int volume, String details,
          BookSide side, String id)
          throws InvalidMessageException {
    return new FillMessageImpl(user, product, price, volume,
            details, side, id);
  }

  /**
   * Creates a MarketMessageImpl object the market message will delegate to.
   *
   * @param state
   * @return a market message impl object.
   * @throws InvalidMessageException
   */
  public static MarketMessageImpl createMarketMessageImpl(MarketState state)
          throws InvalidMessageException {
    return new MarketMessageImpl(state);
  }

  /**
   * Creates a general Market Message used by the CancelMessageImpl and
   * FillMessageImpl to delegate requests to.
   *
   * @param user
   * @param product
   * @param price
   * @param volume
   * @param details
   * @param side
   * @param id
   * @return
   * @throws InvalidMessageException
   */
  protected static GeneralMarketMessage createGeneralMarketMessageImpl(String user,
          String product, Price price, int volume, String details,
          BookSide side, String id)
          throws InvalidMessageException {
    return new GeneralMarketMessageImpl(user, product, price, volume,
            details, side, id);
  }
}