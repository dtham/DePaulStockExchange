package publishers.messages;

import constants.GlobalConstants.BookSide;
import price.Price;
import publishers.messages.exceptions.InvalidMessageException;


public class FillMessageImpl implements GeneralMarketMessage {

    GeneralMarketMessage generalMessage;

  /**
   * The Impl object a fill message will delegate to.
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
  public FillMessageImpl(String user,
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
    String str = generalMessage.toString();
    return str.substring(0, str.indexOf("ID") - 2);
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