package publishers;

import client.User;
import publishers.exceptions.MessagePublisherException;


public interface MessagePublisherCommon {

  /**
   * Publishers will need a "subscribe" method for users to call so they can
   * subscribe for data. When calling "subscribe", users will provide a "User"
   * reference (a reference to themselves), and the String stock symbol
   * they are interested.
   *
   * @param u
   * @param product
   * @throws MessagePublisherException
   */
  public void subscribe(User u, String product)
          throws MessagePublisherException;

  /**
   * Publishers will need a "unSubscribe" method for users to call so they can
   * un-subscribe for data. When calling "unSubscribe", users will provide
   * a "User" reference (a reference to themselves), and the String stock symbol
   * they are interested un-subscribing from.
   *
   * @param u
   * @param product
   * @throws MessagePublisherException
   */
  public void unSubscribe(User u, String product)
          throws MessagePublisherException;
}