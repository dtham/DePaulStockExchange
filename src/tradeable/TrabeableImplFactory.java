package tradeable;

import constants.GlobalConstants.BookSide;
import price.Price;
import tradeable.exceptions.InvalidVolumeException;
import tradeable.exceptions.TradeableException;


public class TrabeableImplFactory {

  /**
   * This function creates and returns only the TradeableImpl at the
   * moment.
   *
   * @param theUserName the name of the user associated with this tradeable
   * @param theProductSymbol the stock symbol associated with this tradeable
   * @param theOrderPrice the order price associated with this tradeable
   * @param theOriginalVolume the original volume associated with this tradeable
   * @param isItAQuote whether or not this tradeable is a quote
   * @param theSide the book side associated with this tradeable
   * @param theId the id associated with this tradeable
   * @return a TradeableImlp object.
   * @throws InvalidVolumeException
   */
  public static TradeableImpl createTradeable(String theUserName,
          String theProductSymbol, Price theOrderPrice,
          int theOriginalVolume, boolean isItAQuote,
          BookSide theSide, String theId) throws InvalidVolumeException,
          TradeableException {
    // This function only returns one type of Impl. object at the moment.
    return new TradeableImpl(theUserName, theProductSymbol, theOrderPrice,
            theOriginalVolume, isItAQuote, theSide, theId);
  }
}