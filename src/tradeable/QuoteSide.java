package tradeable;

import constants.GlobalConstants.BookSide;
import price.Price;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;


public class QuoteSide implements Tradeable {

  private Tradeable thisQuoteSide;

  public QuoteSide(String theUserName, String theProductSymbol,
          Price theOrderPrice, int theOriginalVolume,
          BookSide theSide) throws InvalidVolumeException, TradeableException {
    thisQuoteSide = TrabeableImplFactory.createTradeable(theUserName,
            theProductSymbol, theOrderPrice, theOriginalVolume,
            true, theSide, theUserName + theProductSymbol +
            System.nanoTime());
  }

  /**
   * Copy Constructor.
   */
  public QuoteSide(QuoteSide qs)
          throws InvalidVolumeException, TradeableException {
    thisQuoteSide = TrabeableImplFactory.createTradeable(qs.getUser(),
            qs.getProduct(), qs.getPrice(), qs.getOriginalVolume(),
            qs.isQuote(), qs.getSide(), qs.getUser() + qs.getProduct() +
            System.nanoTime());
  }

  @Override
  public String getProduct() {
    return thisQuoteSide.getProduct();
  }

  @Override
  public Price getPrice() {
    return thisQuoteSide.getPrice();
  }

  @Override
  public int getOriginalVolume() {
    return thisQuoteSide.getOriginalVolume();
  }

  @Override
  public int getRemainingVolume() {
    return thisQuoteSide.getRemainingVolume();
  }

  @Override
  public int getCancelledVolume() {
    return thisQuoteSide.getCancelledVolume();
  }

  @Override
  public void setCancelledVolume(int newCancelledVolume)
          throws InvalidVolumeException {
    thisQuoteSide.setCancelledVolume(newCancelledVolume);
  }

  @Override
  public void setRemainingVolume(int newRemainingVolume)
          throws InvalidVolumeException {
    thisQuoteSide.setRemainingVolume(newRemainingVolume);
  }

  @Override
  public String getUser() {
    return thisQuoteSide.getUser();
  }

  @Override
  public BookSide getSide() {
    return thisQuoteSide.getSide();
  }

  @Override
  public boolean isQuote() {
    return thisQuoteSide.isQuote();
  }

  @Override
  public String getId() {
    return thisQuoteSide.getId();
  }

  @Override
  public String toString() {
    return String.format("%s x %s " +
           "(Original Vol: %s, CXL'd: %s) [%s]", thisQuoteSide.getPrice(),
           thisQuoteSide.getRemainingVolume(),
           thisQuoteSide.getOriginalVolume(),
           thisQuoteSide.getCancelledVolume(), thisQuoteSide.getId());
  }
}