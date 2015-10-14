package publishers.messages;

import price.Price;


public class MarketDataDTO {

  /**
   * The stock product (i.e., IBM, GOOG, AAPL, etc.) that these market data
   * elements describe.
   */
  public String product;

  /**
   * The current BUY side price of the Stock.
   */
  public Price buyPrice;

  /**
   * The current BUY side volume (quantity) of the Stock.
   */
  public int buyVolume;

  /**
   * The current SELL side price of the Stock.
   */
  public Price sellPrice;

  /**
   * The current SELL side volume (quantity) of the Stock.
   */
  public int sellVolume;

  public MarketDataDTO(String product, Price buyPrice, int buyVolume,
          Price sellPrice, int sellVolume) {
    this.product = product;
    this.buyPrice = buyPrice;
    this.buyVolume = buyVolume;
    this.sellPrice = sellPrice;
    this.sellVolume = sellVolume;
  }

  @Override
  public String toString() {
    return "Product: " + product + ". Buy Price: " + buyPrice +
            ", Buy Volume: " + buyVolume + ", Sell Price: " + sellPrice +
            ", Sell Volume: " + sellVolume;
  }
}