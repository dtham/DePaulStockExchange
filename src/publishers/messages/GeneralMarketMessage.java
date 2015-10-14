package publishers.messages;

import constants.GlobalConstants.BookSide;
import price.Price;
import publishers.messages.exceptions.InvalidMessageException;


public interface GeneralMarketMessage {

  /**
   * Returns the user of the order/quote side associated with this
   * cancel/fill message.
   *
   * @return user
   */
  public String getUser();

  /**
   * Returns the product of the order/quote side associated with this
   * cancel/fill message.
   *
   * @return product
   */
  public String getProduct();

  /**
   * Returns the price of the order/quote side associated with this
   * cancel/fill message.
   *
   * @return price
   */
  public Price getPrice();

  /**
   * Returns the volume of the order/quote side associated with this
   * cancel/fill message.
   *
   * @return volume
   */
  public int getVolume();

  /**
   * Returns the details associated with this cancel/fill message.
   *
   * @return details
   */
  public String getDetails();

  /**
   * Returns the side of the order/quote side associated with this
   * cancel/fill message.
   *
   * @return side
   */
  public BookSide getSide();

  /**
   * Returns the ID associated with this cancel/fill message.
   */
  public String getID();

  /**
   * Sets the Volume of a message.
   *
   * @param volume
   * @throws InvalidMessageException
   */
  public void setVolume(int volume) throws InvalidMessageException;

  /**
   * Sets the detail of a message.
   *
   * @param details
   * @throws InvalidMessageException
   */
  public void setDetails(String details) throws InvalidMessageException;
}