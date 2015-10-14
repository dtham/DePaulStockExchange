package publishers;

import client.User;
import price.Price;
import publishers.exceptions.MessagePublisherException;


public class LastSalePublisher implements
        LastSalePublisherSpecific {

  private volatile static LastSalePublisher instance;
  private LastSalePublisherSpecific messagePublisherSubjectImpl;

 public static LastSalePublisher getInstance() {
    if (instance == null) {
      synchronized (LastSalePublisher.class) {
        if (instance == null) {
          instance = MessagePublisherSubjectFactory
                  .createLastSalePublisher();
        }
      }
    }
    return instance;
  }

  protected LastSalePublisher(LastSalePublisherSpecific impl) {
    messagePublisherSubjectImpl = impl;
  }

  @Override
  public synchronized void subscribe(User u, String product) throws MessagePublisherException {
    messagePublisherSubjectImpl.subscribe(u, product);
  }

  @Override
  public synchronized void unSubscribe(User u, String product) throws MessagePublisherException {
    messagePublisherSubjectImpl.unSubscribe(u, product);
  }

  @Override
  public synchronized void publishLastSale(String product, Price p, int v)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.publishLastSale(product, p, v);
  }
}