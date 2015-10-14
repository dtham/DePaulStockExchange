package tradeprocessing.tradeprocessor;

import java.util.HashMap;
import publishers.messages.FillMessage;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.Tradeable;
import tradeable.exceptions.InvalidVolumeException;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;


public interface TradeProcessor {

  /**
   * This TradeProcessor method will be called when it has been determined that
   * a Tradable (i.e., a Buy Order, a Sell QuoteSide, etc.) can trade against
   * the content of the book.
   * The return value from this function will be a HashMap<String, FillMessage>
   * containing String trade identifiers (the key) and a Fill Message object
   * (the value).
   *
   * @param trd
   * @return a HashMap<String, FillMessage>
   */
  public HashMap<String, FillMessage> doTrade(Tradeable trd)
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException;
}