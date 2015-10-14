package tradeable;

import constants.GlobalConstants.BookSide;
import price.Price;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;


public class Quote {

  private String user;
  private String product;
  private QuoteSide buyQuoteSide;
  private QuoteSide sellQuoteSide;

  public Quote(String userName, String productSymbol, Price buyPrice,
          int buyVolume, Price sellPrice, int sellVolume)
          throws InvalidVolumeException, TradeableException {
    user = userName;
    product = productSymbol;
    buyQuoteSide = new QuoteSide(userName, productSymbol, buyPrice, buyVolume,
            BookSide.BUY);
    sellQuoteSide = new QuoteSide(userName, productSymbol, sellPrice, sellVolume,
            BookSide.SELL);
  }

  public String getUserName() {
    return user;
  }

  public String getProduct() {
    return product;
  }

  public QuoteSide getQuoteSide(BookSide side)
          throws TradeableException, InvalidVolumeException {
    if (side.equals(BookSide.BUY)) {
      return new QuoteSide(buyQuoteSide);
    }
    return new QuoteSide(sellQuoteSide);
  }

  @Override
  public String toString() {
    return String.format("%s quote: %s - %s", user, buyQuoteSide,
            sellQuoteSide);
  }
}