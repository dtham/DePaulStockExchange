package publishers;

import price.Price;
import publishers.exceptions.MessagePublisherException;


public interface TickerPublisherSpecific extends MessagePublisherCommon {

  /**
   * Notifies users of the last sale out for the given stock.
   * (Stock Symbol, Price)
   *
   * @param product
   * @param p
   */
  public void publishTicker(String product, Price p)
          throws MessagePublisherException;
}