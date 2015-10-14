package tradeable;

import constants.GlobalConstants.BookSide;
import price.Price;
import tradeable.exceptions.InvalidVolumeException;

public interface Tradeable {

  /**
   * @return the product symbol (i.e, IBM, GOOG, AAPL, etc.) that the Tradeable
   * works with.
   */
  public String getProduct();

  /**
   * @return the price of the Tradeable
   */
  public Price getPrice();

  /**
   * @return the original volume (i.e., the original quantity) of the Tradeable
   */
  public int getOriginalVolume();

  /**
   * @return the remaining volume (i.e., the remaining quantity) of the Tradeable
   */
  public int getRemainingVolume();

  /**
   * @return the cancelled volume (i.e., the cancelled quantity)
   */
  public int getCancelledVolume();

  /**
   * Sets the Tradeable's cancelled quantity to the value passed in.
   *
   * @param newCancelledVolume the cancelled volume to be set
   * @throws InvalidVolumeException
   */
  public void setCancelledVolume(int newCancelledVolume)
          throws InvalidVolumeException;

  /**
   * Sets the Tradeable's remaining quantity to the value passed in.
   *
   * @param newRemainingVolume the remaining volume to be set
   * @throws InvalidVolumeException
   */
  public void setRemainingVolume(int newRemainingVolume)
          throws InvalidVolumeException;

  /**
   * @return the User ID associated with the Tradeable
   */
  String getUser();

  /**
   * @return the "side" (BUY/SELL) of the Tradeable
   */
  BookSide getSide();

  /**
   * @return true if the Tradeable is part of a Quote, returns if not (i.e.,
   * false if it's part of an order)
   */
  boolean isQuote();

  /**
   * @return the Tradeable "id"
   */
  String getId();
}
