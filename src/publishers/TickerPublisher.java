package publishers;

import client.User;
import price.Price;
import publishers.exceptions.MessagePublisherException;


public class TickerPublisher implements
        TickerPublisherSpecific {

  private volatile static TickerPublisher instance;
  private TickerPublisherSpecific messagePublisherSubjectImpl;

 public static TickerPublisher getInstance() {
    if (instance == null) {
      synchronized (TickerPublisher.class) {
        if (instance == null) {
          instance = MessagePublisherSubjectFactory
                  .createTickerPublisher();
        }
      }
    }
    return instance;
  }

  protected TickerPublisher(TickerPublisherSpecific impl) {
    messagePublisherSubjectImpl = impl;
  }

  @Override
  public synchronized void subscribe(User u, String product)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.subscribe(u, product);
  }

  @Override
  public synchronized void unSubscribe(User u, String product)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.unSubscribe(u, product);
  }

  @Override
  public synchronized void publishTicker(String product, Price p)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.publishTicker(product, p);
  }
}