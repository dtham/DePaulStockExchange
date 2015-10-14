package tradeable;

import constants.GlobalConstants.BookSide;
import price.Price;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;


public class Order implements Tradeable {

  private Tradeable thisOrder;

  public Order(String theUserName, String theProductSymbol,
          Price theOrderPrice, int theOriginalVolume,
          BookSide theSide)
          throws InvalidVolumeException, TradeableException {
    thisOrder = TrabeableImplFactory.createTradeable(theUserName,
            theProductSymbol, theOrderPrice, theOriginalVolume, false, theSide,
            theUserName + theProductSymbol + theOrderPrice + System.nanoTime());
  }

  @Override
  public String getProduct() {
    return thisOrder.getProduct();
  }

  @Override
  public Price getPrice() {
    return thisOrder.getPrice();
  }

  @Override
  public int getOriginalVolume() {
    return thisOrder.getOriginalVolume();
  }

  @Override
  public int getRemainingVolume() {
    return thisOrder.getRemainingVolume();
  }

  @Override
  public int getCancelledVolume() {
    return thisOrder.getCancelledVolume();
  }

  @Override
  public void setCancelledVolume(int newCancelledVolume)
          throws InvalidVolumeException {
    thisOrder.setCancelledVolume(newCancelledVolume);
  }

  @Override
  public void setRemainingVolume(int newRemainingVolume)
          throws InvalidVolumeException {
    thisOrder.setRemainingVolume(newRemainingVolume);
  }

  @Override
  public String getUser() {
    return thisOrder.getUser();
  }

  @Override
  public BookSide getSide() {
    return thisOrder.getSide();
  }

  @Override
  public boolean isQuote() {
    return thisOrder.isQuote();
  }

  @Override
  public String getId() {
    return thisOrder.getId();
  }

  @Override
  public String toString() {
    return String.format("%s order: %s %s %s at %s " +
           "(Original Vol: %s, CXL'd: %s), ID: %s", thisOrder.getUser(),
           thisOrder.getSide(), thisOrder.getRemainingVolume(),
           thisOrder.getProduct(), thisOrder.getPrice(), thisOrder.getOriginalVolume(),
           thisOrder.getCancelledVolume(), thisOrder.getId());
  }
}