package constants;


public interface GlobalConstants {
  /*
   * An enum type indicating in what state the stock exchange is in.
   */
  public static enum MarketState {CLOSED, OPEN, PREOPEN};

  /**
   * An enum type indicating which "side" the Tradeable represents: BUY or SELL
   */
  public static enum BookSide {BUY, SELL}
}