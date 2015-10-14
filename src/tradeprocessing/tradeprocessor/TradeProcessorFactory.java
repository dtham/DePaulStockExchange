package tradeprocessing.tradeprocessor;

import tradeprocessing.productbook.ProductBookSide;
import tradeprocessing.tradeprocessor.exceptions.InvalidProductBookSideValueException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorFactoryException;


public class TradeProcessorFactory {

  private synchronized static TradeProcessor
          createTradeProcessorPriceTimeImpl(ProductBookSide pbs)
          throws InvalidProductBookSideValueException {
    return new TradeProcessorPriceTimeImpl(pbs);
  }

  /**
   * Creates a TradeProcessor based on the type passed in. Types include:<br />
   * price-time
   *
   * @return a TradeProcessor
   */
  public synchronized static TradeProcessor
          createTradeProcessor(String type, ProductBookSide pbs)
          throws InvalidProductBookSideValueException,
          TradeProcessorFactoryException {
    validateInput(type);
    validateInput(pbs);
    TradeProcessor processor;
    switch(type) {
      case "price-time":
      default:
        processor = createTradeProcessorPriceTimeImpl(pbs);
    }
    return processor;
  }

  private synchronized static void validateInput(String o)
          throws TradeProcessorFactoryException {
    if (o == null || o.isEmpty()) {
      throw new TradeProcessorFactoryException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private synchronized static void validateInput(Object o)
          throws TradeProcessorFactoryException {
    if (o == null) {
      throw new TradeProcessorFactoryException("Argument cannot be null.");
    }
  }
}