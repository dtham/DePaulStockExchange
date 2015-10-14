package publishers.messages;

import constants.GlobalConstants.BookSide;
import price.Price;
import publishers.messages.exceptions.InvalidMessageException;


public class CancelMessageImpl implements GeneralMarketMessage {

  GeneralMarketMessage generalMessage;

  /**
   * The Impl object a cancel message will delegate to.
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
  public CancelMessageImpl(String user,
          String product, Price price, int volume, String details,
          BookSide side, String id)
          throws InvalidMessageException {
    generalMessage = MessageFactory.createGeneralMarketMessageImpl(user,
            product, price, volume, details, side, id);
  }

  @Override
  public String getUser() {
    return generalMessage.getUser();
  }

  @Override
  public String getProduct() {
    return generalMessage.getProduct();
  }

  @Override
  public Price getPrice() {
    return generalMessage.getPrice();
  }

  @Override
  public int getVolume() {
    return generalMessage.getVolume();
  }

  @Override
  public String getDetails() {
    return generalMessage.getDetails();
  }

  @Override
  public BookSide getSide() {
    return generalMessage.getSide();
  }

  @Override
  public String getID() {
    return generalMessage.getID();
  }

  @Override
  public String toString() {
    return generalMessage.toString();
  }

  @Override
  public void setVolume(int volume) throws InvalidMessageException {
    generalMessage.setVolume(volume);
  }

  @Override
  public void setDetails(String details) throws InvalidMessageException {
    generalMessage.setDetails(details);
  }
}