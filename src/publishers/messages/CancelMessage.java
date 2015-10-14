package publishers.messages;

import constants.GlobalConstants.BookSide;
import price.Price;
import publishers.messages.exceptions.InvalidMessageException;


public class CancelMessage implements GeneralMarketMessage,
        Comparable<CancelMessage> {

  protected GeneralMarketMessage cancelMessageImpl;

    /**
   * Creates a cancel message object.
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
  public CancelMessage(String user,
          String product, Price price, int volume, String details,
          BookSide side, String id)
          throws InvalidMessageException {
    cancelMessageImpl = MessageFactory.createCancelMessageImpl(user,
            product, price, volume, details, side, id);
  }

  @Override
  public String getUser() {
    return cancelMessageImpl.getUser();
  }

  @Override
  public String getProduct() {
    return cancelMessageImpl.getProduct();
  }

  @Override
  public Price getPrice() {
    return cancelMessageImpl.getPrice();
  }

  @Override
  public int getVolume() {
    return cancelMessageImpl.getVolume();
  }

  @Override
  public String getDetails() {
    return cancelMessageImpl.getDetails();
  }

  @Override
  public BookSide getSide() {
    return cancelMessageImpl.getSide();
  }

  @Override
  public String getID() {
    return cancelMessageImpl.getID();
  }

  @Override
  public int compareTo(CancelMessage o) {
    return cancelMessageImpl.getPrice().compareTo(o.getPrice());
  }

  @Override
  public String toString() {
    return cancelMessageImpl.toString();
  }

  @Override
  public void setVolume(int volume) throws InvalidMessageException {
    cancelMessageImpl.setVolume(volume);
  }

  @Override
  public void setDetails(String details) throws InvalidMessageException {
    cancelMessageImpl.setDetails(details);
  }
}