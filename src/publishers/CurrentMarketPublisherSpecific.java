package publishers;

import publishers.exceptions.MessagePublisherException;
import publishers.messages.MarketDataDTO;


public interface CurrentMarketPublisherSpecific extends MessagePublisherCommon {
  /**
   * Notifies all users of of current market conditions for the given stock.
   *
   * @param m
   */
  public void publishCurrentMarket(MarketDataDTO m)
          throws MessagePublisherException;
}