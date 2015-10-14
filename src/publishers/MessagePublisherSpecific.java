package publishers;

import publishers.exceptions.MessagePublisherException;
import publishers.messages.CancelMessage;
import publishers.messages.FillMessage;
import publishers.messages.MarketMessage;


public interface MessagePublisherSpecific extends MessagePublisherCommon {

  /**
   * Notifies the user of a canceled order.
   *
   * @param cm
   */
  public void publishCancel(CancelMessage cm) throws MessagePublisherException;

  /**
   * Notifies the user of a fulfilled order.
   *
   * @param fm
   */
  public void publishFill(FillMessage fm) throws MessagePublisherException;

  /**
   * Notifies all users of a market message.
   *
   * @param mm
   */
  public void publishMarketMessage(MarketMessage mm)
          throws MessagePublisherException;
}