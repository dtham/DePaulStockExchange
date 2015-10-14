package publishers.messages;

import constants.GlobalConstants.BookSide;
import price.Price;
import publishers.messages.exceptions.InvalidMessageException;


public class FillMessage implements GeneralMarketMessage,
        Comparable<FillMessage> {

  protected GeneralMarketMessage fillMessageImpl;

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
  public FillMessage(String user,
          String product, Price price, int volume, String details,
          BookSide side, String id)
          throws InvalidMessageException {
    fillMessageImpl = MessageFactory.createFillMessageImpl(user,
            product, price, volume, details, side, id);
  }

  @Override
  public String getUser() {
    return fillMessageImpl.getUser();
  }

  @Override
  public String getProduct() {
    return fillMessageImpl.getProduct();
  }

  @Override
  public Price getPrice() {
    return fillMessageImpl.getPrice();
  }

  @Override
  public int getVolume() {
    return fillMessageImpl.getVolume();
  }

  @Override
  public String getDetails() {
    return fillMessageImpl.getDetails();
  }

  @Override
  public BookSide getSide() {
    return fillMessageImpl.getSide();
  }

  @Override
  public String getID() {
    return fillMessageImpl.getID();
  }

  @Override
  public int compareTo(FillMessage o) {
    return fillMessageImpl.getPrice().compareTo(o.getPrice());
  }

  @Override
  public String toString() {
    return fillMessageImpl.toString();
  }

  @Override
  public void setVolume(int volume) throws InvalidMessageException {
    fillMessageImpl.setVolume(volume);
  }

  @Override
  public void setDetails(String details) throws InvalidMessageException {
    fillMessageImpl.setDetails(details);
  }
}