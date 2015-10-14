package publishers;

import client.User;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import price.Price;
import publishers.exceptions.MessagePublisherException;
import publishers.messages.CancelMessage;
import publishers.messages.FillMessage;
import publishers.messages.MarketDataDTO;
import publishers.messages.MarketMessage;

class MessagePublisherSubjectImpl
  implements CurrentMarketPublisherSpecific,
  LastSalePublisherSpecific, TickerPublisherSpecific,
  MessagePublisherSpecific {

  /**
   * A hash map of of keys representing stock symbols and a HashSet of users
   * subscribed to those symbols for stock market updates.
   */
  private Map<String, Set<User>> subscribers;

  /**
   * A hash map of the most recent ticker price for a stock symbol.
   */
  private Map<String, Price> stockTickerValue;

  protected MessagePublisherSubjectImpl() {
    subscribers = new HashMap<>();
    stockTickerValue = new HashMap<>();
  }

  @Override
  public synchronized final void subscribe(User u, String product)
          throws MessagePublisherException {
    validateInput(u);
    validateInput(product);
    // Create the HashSet of users if the Product does not exist in the
    // subscribers hash map
    createUserSetForProduct(product);
    Set<User> set = subscribers.get(product);
    if (set.contains(u)) {
      throw new MessagePublisherException("The users is already subscribed to "
              + "receive updates for this stock symbol: " + product);
    }
    set.add(u);
  }

  @Override
  public synchronized final void unSubscribe(User u, String product)
          throws MessagePublisherException {
    validateInput(u);
    validateInput(product);
    Set<User> set = subscribers.get(product);
    if (set == null) {
      throw new MessagePublisherException("No one is registered for this "
              + "stock symbol: " + product);
    }
    if (!set.contains(u)) {
      throw new MessagePublisherException("The user is not subscribed to "
              + "receive updates for this stock symbol: " + product);
    }
    set.remove(u);
  }

  private synchronized void createUserSetForProduct(String product)
          throws MessagePublisherException {
    validateInput(product);
    if (!subscribers.containsKey(product)) {
      subscribers.put(product, new HashSet<User>());
    }
  }

  @Override
  public synchronized void publishCurrentMarket(MarketDataDTO m)
          throws MessagePublisherException {
    validateInput(m);
    if (!subscribers.containsKey(m.product)) { return; }
    Set<User> users = subscribers.get(m.product);
    for (User u : users) {
      u.acceptCurrentMarket(m.product, m.buyPrice, m.buyVolume, m.sellPrice,
              m.sellVolume);
    }
  }

  @Override
  public synchronized void publishLastSale(String product, Price p, int v)
          throws MessagePublisherException {
    validateInput(p);
    validateInput(product);
    if (!subscribers.containsKey(product)) { return; }
    Set<User> users = subscribers.get(product);
    for (User u : users) {
      u.acceptLastSale(product, p, v);
    }
    TickerPublisher.getInstance().publishTicker(product, p);
  }

  @Override
  public synchronized void publishTicker(String product, Price p)
          throws MessagePublisherException {
    validateInput(p);
    validateInput(product);
    if (!subscribers.containsKey(product)) { return; }
    char direction = ' ';
    Price val = stockTickerValue.get(product);
    if (val != null) {
      if (p.equals(val)) {
        direction = '=';
      } else if (p.greaterThan(val)) {
        direction = '\u2191';
      } else if (p.lessThan(val)) {
        direction = '\u2193';
      }
    }
    stockTickerValue.put(product, p);
    Set<User> users = subscribers.get(product);
    for (User u : users) {
      u.acceptTicker(product, p, direction);
    }
  }

  @Override
  public synchronized void publishCancel(CancelMessage cm)
          throws MessagePublisherException {
    validateInput(cm);
    String p = cm.getProduct();
    if (!subscribers.containsKey(p)) { return; }
    for (User u : subscribers.get(p)) {
      if (u.getUserName().equals(cm.getUser())) {
        u.acceptMessage(cm);
      }
    }
  }

  @Override
  public synchronized void publishFill(FillMessage fm)
          throws MessagePublisherException {
    validateInput(fm);
    String p = fm.getProduct();
    if (!subscribers.containsKey(p)) { return; }
    for (User u : subscribers.get(p)) {
      if (u.getUserName().equals(fm.getUser())) {
        u.acceptMessage(fm);
      }
    }
  }

  @Override
  public synchronized void publishMarketMessage(MarketMessage mm)
          throws MessagePublisherException {
    validateInput(mm);
    HashSet<User> allUsers = new HashSet<>();
    for (Set<User> users : subscribers.values()) {
      for (User u : users) {
        if (!allUsers.contains(u)) {
          allUsers.add(u);
        }
      }
    }
    for (User u : allUsers) {
      u.acceptMarketMessage(mm.getState().toString());
    }
  }

  private void validateInput(String o)
          throws MessagePublisherException {
    if (o == null || o.isEmpty()) {
      throw new MessagePublisherException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(User o) throws MessagePublisherException {
    if (o == null) {
      throw new MessagePublisherException("Argument must be of type User and"
              + " cannot be null.");
    }
  }

  private void validateInput(Price o) throws MessagePublisherException {
    if (o == null || !(o instanceof Price)) {
      throw new MessagePublisherException("Argument must be of type Price and"
              + " cannot be null.");
    }
  }

  private void validateInput(Object o) throws MessagePublisherException {
    if (o == null) {
      throw new MessagePublisherException("Argument cannot be null.");
    }
  }
}