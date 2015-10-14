package publishers.messages;

import constants.GlobalConstants.MarketState;
import publishers.messages.exceptions.InvalidMessageException;


public class MarketMessageImpl implements StateOfMarket {

  /**
   * Holds the state of the Market (either CLOSED, OPEN, PREOPEN).
   */
  private MarketState state;

  public MarketMessageImpl(MarketState state) throws InvalidMessageException {
    setState(state);
  }

  private void setState(MarketState state) throws InvalidMessageException {
    validateInput(state);
    this.state = state;
  }

  @Override
  public final MarketState getState() {
    return state;
  }

  private void validateInput(MarketState o) throws InvalidMessageException {
    if (o == null || !(o instanceof MarketState)) {
      throw new InvalidMessageException("Argument must be of type MarketState"
              + " and cannot be null.");
    }
  }
}