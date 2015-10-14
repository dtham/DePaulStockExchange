package publishers;

import client.User;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.CancelMessage;
import publishers.messages.FillMessage;
import publishers.messages.MarketMessage;

public class MessagePublisher implements
        MessagePublisherSpecific {

  private volatile static MessagePublisher instance;
  private MessagePublisherSpecific messagePublisherSubjectImpl;

  public static MessagePublisher getInstance() {
    if (instance == null) {
      synchronized (MessagePublisher.class) {
        if (instance == null) {
          instance = MessagePublisherSubjectFactory
                  .createMessagePublisher();
        }
      }
    }
    return instance;
  }

  protected MessagePublisher(MessagePublisherSpecific impl) {
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
  public synchronized void publishCancel(CancelMessage cm)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.publishCancel(cm);
  }

  @Override
  public synchronized void publishFill(FillMessage fm)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.publishFill(fm);
  }

  @Override
  public synchronized void publishMarketMessage(MarketMessage mm)
          throws MessagePublisherException {
    messagePublisherSubjectImpl.publishMarketMessage(mm);
  }
}