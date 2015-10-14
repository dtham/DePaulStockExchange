package publishers.messages;

import constants.GlobalConstants.BookSide;
import price.Price;
import publishers.messages.exceptions.InvalidMessageException;

public class GeneralMarketMessageImpl implements GeneralMarketMessage {

  /**
   * The String username of the user whose order or quote-side is being
   * cancelled/filled. Cannot be null or empty.
   */
  private String user;

  /**
   * The string stock symbol that the cancelled/filled order or quote-side was
   * submitted for; example ("IBM", "GE", etc.). Cannot be null or empty.
   */
  private String product;

  /**
   * The price specified in the cancelled/filled order or quote-side.
   * Cannot be null.
   */
  private Price price;

  /**
   * The quantity of the order or quote-side that was cancelled/filled.
   * Cannot be negative.
   */
  private int volume;

  /**
   * A text description of the cancellation/fulfillment. Cannot be null.
   */
  private String details;

  /**
   * The side (BUY/SELL) of the cancelled/filled order or quote-side.
   * Must be a valid side.
   */
  private BookSide side;

  /**
   * The String identifier of the cancelled/filled order or quote-side.
   * Cannot be null.
   */
  public String id;

  /**
   * Creates a general implementation that will be delegated to be cancel and
   * fill messages.
   *
   * @param user
   * @param product
   * @param price
   * @param volume
   * @param details
   * @param side
   * @param id
   * @throws InvalidMessageException
   */
  public GeneralMarketMessageImpl(String user, String product, Price price,
          int volume, String details, BookSide side, String id)
            throws InvalidMessageException {
    setUser(user);
    setProduct(product);
    setPrice(price);
    setVolume(volume);
    setDetails(details);
    setSide(side);
    setId(id);
  }

  @Override
  public final String getUser() {
    return user;
  }

  @Override
  public final String getProduct() {
    return product;
  }

  @Override
  public final Price getPrice() {
    return price;
  }

  @Override
  public final int getVolume() {
    return volume;
  }

  @Override
  public final String getDetails() {
    return details;
  }

  @Override
  public final BookSide getSide() {
    return side;
  }

  @Override
  public final String getID() {
    return id;
  }

  private void setUser(String user) throws InvalidMessageException {
    validateInput(user);
    this.user = user;
  }

  private void setProduct(String product) throws InvalidMessageException {
    validateInput(product);
    this.product = product;
  }

  private void setPrice(Price price) throws InvalidMessageException {
    validateInput(price);
    this.price = price;
  }

  @Override
  public final void setVolume(int volume) throws InvalidMessageException {
    validateInput(volume);
    this.volume = volume;
  }

  @Override
  public final void setDetails(String details) throws InvalidMessageException {
    validateInput(details);
    this.details = details;
  }

  private void setSide(BookSide side) throws InvalidMessageException {
    validateInput(side);
    this.side = side;
  }

  private void setId(String id) throws InvalidMessageException {
    validateInput(id);
    this.id = id;
  }

  @Override
  public String toString() {
    return "User: " + user + ", Product: " + product + ", Price: " + price +
            ", Volume: " + volume + ", Details: " + details + ", Side: " +
            side + ", ID: " + id;
  }

  private void validateInput(String o)
          throws InvalidMessageException {
    if (o == null || o.isEmpty()) {
      throw new InvalidMessageException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(Price o) throws InvalidMessageException {
    if (o == null || !(o instanceof Price)) {
      throw new InvalidMessageException("Argument must be of type Price and"
              + " cannot be null.");
    }
  }

  private void validateInput(BookSide o) throws InvalidMessageException {
    if (o == null || !(o instanceof BookSide)) {
      throw new InvalidMessageException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }

  private void validateInput(Object o) throws InvalidMessageException {
    if (o == null) {
      throw new InvalidMessageException("Argument cannot be null.");
    }
  }

  private void validateInput(int o) throws InvalidMessageException {
    if (o < 0) {
      throw new InvalidMessageException("Argument cannot be negative.");
    }
  }
}