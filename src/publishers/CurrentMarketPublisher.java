package publishers;

import client.User;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.MarketDataDTO;


public class CurrentMarketPublisher implements CurrentMarketPublisherSpecific {

  private volatile static CurrentMarketPublisher instance;
  private CurrentMarketPublisherSpecific messagePublisherSubjectImpl;

  public static CurrentMarketPublisher getInstance() {
    if (instance == null) {
      synchronized (CurrentMarketPublisher.class) {
        if (instance == null) {
          instance = MessagePublisherSubjectFactory
                  .createCurrentMarketPublisher();
        }
      }
    }
    return instance;
  }

  protected CurrentMarketPublisher(CurrentMarketPublisherSpecific impl) {
    messagePublisherSubjectImpl = impl;
  }

  @Override
  public synchronized void subscribe(User u, String product)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.subscribe(u, product);
  }

  @Override
  public synchronized void unSubscribe(User u, String product) throws
          MessagePublisherException {
    messagePublisherSubjectImpl.unSubscribe(u, product);
  }

  @Override
  public synchronized void publishCurrentMarket(MarketDataDTO m)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.publishCurrentMarket(m);
  }
}