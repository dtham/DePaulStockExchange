package tradeable;

import constants.GlobalConstants.BookSide;
import price.Price;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;


public class TradeableImpl implements Tradeable {

  TradeableImpl self = this;

  /**
   * The product (i.e., IBM, GOOG, AAPL, etc.) that the Tradeable works with.
   */
  private String product;

  /**
   * The price of the Tradable.
   */
  private Price price;

  /**
   * The original volume (i.e., the original quantity) of the Tradeable.
   */
  private int originalVolume;

  /**
   * The remaining volume (i.e., the remaining quantity) of the Tradeable.
   */
  private int remainingVolume;

  /**
   * The cancelled volume (i.e, the cancelled quantity) of the Tradeable.
   */
  private int cancelledVolume;

  /**
   * The user id associated with the Tradeable.
   */
  private String user;

  /**
   * The "side" (BUY/SELL) of the Tradeable.
   */
  private BookSide side;

  /**
   * Set to true if the Tradeable is part of a Quote, false if not (i.e., false
   * if it's part of an Order).
   */
  private boolean isQuote;

  /**
   * The Tradeable "id" - the value each tradeable is given once it is received
   * by the system.
   */
  private String id;

  public TradeableImpl(String theUserName,
          String theProductSymbol, Price theOrderPrice,
          int theOriginalVolume, boolean isItAQuote,
          BookSide theSide, String theId)
          throws InvalidVolumeException, TradeableException {
    setUser(theUserName);
    setProduct(theProductSymbol);
    setPrice(theOrderPrice);
    setOriginalVolume(theOriginalVolume);
    setRemainingVolume(theOriginalVolume);
    setCancelledVolume(0);
    setQuote(isItAQuote);
    setSide(theSide);
    setId(theId);
  }

  @Override
  public final String getProduct() {
    return product;
  }

  private void setProduct(String product)
          throws TradeableException {
    validateInput(product);
    self.product = product;
  }

  @Override
  public final Price getPrice() {
    return price;
  }

  private void setPrice(Price price) throws TradeableException {
    validateInput(price);
    self.price = price;
  }

  @Override
  public final int getOriginalVolume() {
    return originalVolume;
  }

  @Override
  public final int getRemainingVolume() {
    return remainingVolume;
  }

  @Override
  public final int getCancelledVolume() {
    return cancelledVolume;
  }

  @Override
  public final void setCancelledVolume(int newCancelledVolume)
          throws InvalidVolumeException {
    validateInput(newCancelledVolume);
    cancelledVolume = newCancelledVolume;
  }

  @Override
  public final void setRemainingVolume(int newRemainingVolume)
          throws InvalidVolumeException {
    validateInput(newRemainingVolume);
    remainingVolume = newRemainingVolume;
  }

  private void setOriginalVolume(int newOriginalVolume)
          throws InvalidVolumeException {
    if (newOriginalVolume < 1) {
      throw new InvalidVolumeException("Invalid original volume " +
              "is being set: " + newOriginalVolume);
    }
    originalVolume = newOriginalVolume;
  }

  @Override
  public final String getUser() {
    return user;
  }

  private void setUser(String user) throws TradeableException {
    validateInput(user);
    self.user = user;
  }

  @Override
  public final BookSide getSide() {
    return side;
  }

  private void setSide(BookSide theSide)
          throws TradeableException {
    validateInput(theSide);
    self.side = theSide;
  }

  @Override
  public final boolean isQuote() {
    return isQuote;
  }

  private void setQuote(boolean quote) {
    self.isQuote = quote;
  }

  @Override
  public final String getId() {
    return id;
  }

  private void setId(String id) throws TradeableException {
    validateInput(id);
    self.id = id;
  }

  private void validateInput(String o)
          throws TradeableException {
    if (o == null || o.isEmpty()) {
      throw new TradeableException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(Price o) throws TradeableException {
    if (o == null || !(o instanceof Price)) {
      throw new TradeableException("Argument must be of type Price and"
              + " cannot be null.");
    }
  }

  private void validateInput(BookSide o) throws TradeableException {
    if (o == null || !(o instanceof BookSide)) {
      throw new TradeableException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }

  private void validateInput(int o) throws InvalidVolumeException {
    if (o < 0 || o > originalVolume) {
      throw new InvalidVolumeException("Argument cannot be negative "
              + "or greater than the original volume.");
    }
  }
}