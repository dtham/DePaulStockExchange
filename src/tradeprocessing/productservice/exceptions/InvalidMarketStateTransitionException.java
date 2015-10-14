package tradeprocessing.productservice.exceptions;

public class InvalidMarketStateTransitionException extends Exception {

  public InvalidMarketStateTransitionException(String msg) {
    super(msg);
  }
}