package tradeable;

import constants.GlobalConstants.BookSide;
import price.Price;


public class TradeableDTO {

  /**
   * The product (i.e., IBM, GOOG, AAPL, etc.) that the Tradeable works with.
   */
  public String product;

  /**
   * The price of the Tradable.
   */
  public Price price;

  /**
   * The original volume (i.e., the original quantity) of the Tradeable.
   */
  public int originalVolume;

  /**
   * The remaining volume (i.e., the remaining quantity) of the Tradeable.
   */
  public int remainingVolume;

  /**
   * The cancelled volume (i.e, the cancelled quantity) of the Tradeable.
   */
  public int cancelledVolume;

  /**
   * The user id associated with the Tradeable.
   */
  public String user;

  /**
   * The "side" (BUY/SELL) of the Tradeable.
   */
  public BookSide side;

  /**
   * Set to true if the Tradeable is part of a Quote, false if not (i.e., false
   * if it's part of an Order).
   */
  public boolean isQuote;

  /**
   * The Tradeable "id" - the value each tradeable is given once it is received
   * by the system.
   */
  public String id;

  public TradeableDTO(String theProduct, Price thePrice, int theOriginalVolume,
          int theRemainingVolume, int theCancelledVolume, String theUser,
          BookSide theBookSide, boolean isItAQuote, String theID) {
    product = theProduct;
    price = thePrice;
    originalVolume = theOriginalVolume;
    remainingVolume = theRemainingVolume;
    cancelledVolume = theCancelledVolume;
    user = theUser;
    side = theBookSide;
    isQuote = isItAQuote;
    id = theID;
  }

  @Override
  public String toString() {
    return String.format("%s %s %s %s at %s " +
           "(Original Vol: %s, CXL'd: %s), isQuote: %s, ID: %s", user, side,
           product, remainingVolume, price, originalVolume, cancelledVolume,
           isQuote, id);
  }
}