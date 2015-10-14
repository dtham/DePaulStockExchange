package tradeprocessing.tradeprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import price.Price;
import publishers.messages.FillMessage;
import publishers.messages.exceptions.InvalidMessageException;
import tradeable.Tradeable;
import tradeable.exceptions.InvalidVolumeException;
import tradeprocessing.productbook.ProductBookSide;
import tradeprocessing.productbook.exceptions.ProductBookException;
import tradeprocessing.productbook.exceptions.ProductBookSideException;
import tradeprocessing.tradeprocessor.exceptions.InvalidProductBookSideValueException;
import tradeprocessing.tradeprocessor.exceptions.TradeProcessorPriceTimeImplException;

public class TradeProcessorPriceTimeImpl implements TradeProcessor {

  /**
   * These objects will need to keep track of fill messages, so a HashMap is
   * needed indexed by trade identifier.
   */
  private HashMap<String, FillMessage> fillMessages = new HashMap<>();

  /**
   * A TradeProcessorPriceTimeImpl needs to maintain a reference to the
   * ProductBookSide that this object belongs to, so you need a ProductBookSide
   * data member to refer to the book side this object belongs to.
   */
  ProductBookSide parent;

  public TradeProcessorPriceTimeImpl(ProductBookSide pbs)
          throws InvalidProductBookSideValueException {
    setProductBookSide(pbs);
  }

  private void setProductBookSide(ProductBookSide pbs)
          throws InvalidProductBookSideValueException {
    validateInput(pbs);
    parent = pbs;
  }

  /**
   * This method will be used at various times when executing a trade.
   * All trades result in Fill Messages.
   *
   * @param fm
   * @return a String key
   */
  private String makeFillKey(FillMessage fm)
          throws TradeProcessorPriceTimeImplException {
    validateInput(fm);
    return fm.getUser() + fm.getID() + fm.getPrice();
  }

  /**
   * This Boolean method checks the content of the "fillMessages" HashMap to
   * see if the FillMessage passed in is a fill message for an existing known
   * trade or if it is for a new previously unrecorded trade.
   *
   * @param fm
   * @return returns true or false based on whether the fill message is new
   * or new
   */
  private boolean isNewFill(FillMessage fm)
          throws TradeProcessorPriceTimeImplException {
    validateInput(fm);
    String key = makeFillKey(fm);
    if (!fillMessages.containsKey(key)) { return true; }
    FillMessage oldFill = fillMessages.get(key);
    if (!oldFill.getSide().equals(fm.getSide())) { return true; }
    if (!oldFill.getID().equals(fm.getID())) { return true; }
    return false;
  }

  /**
   * This method should add a FillMessage either to the "fillMessages" HashMap
   * if it is a new trade, or should update an existing fill message is another
   * part of an existing trade.
   *
   * @param fm
   */
  private void addFillMessage(FillMessage fm)
          throws InvalidMessageException,
          TradeProcessorPriceTimeImplException {
    validateInput(fm);
    if (isNewFill(fm)) {
      String key = makeFillKey(fm);
      fillMessages.put(key, fm);
    } else {
      String key = makeFillKey(fm);
      FillMessage oldFill = fillMessages.get(key);
      oldFill.setVolume(oldFill.getVolume() + fm.getVolume());
      oldFill.setDetails(fm.getDetails());
    }
  }

  /**
   * This TradeProcessor method will be called when it has been determined that
   * a Tradable (i.e., a Buy Order, a Sell QuoteSide, etc.) can trade against
   * the content of the book. The return value from this function will be a
   * HashMap<String, FillMessage> containing String trade identifiers (the key)
   * and a Fill Message object (the value).
   *
   * @param trd
   * @return HashMap<String, FillMessage> containing String trade identifiers
   * (the key) and a Fill Message object (the value)
   */
  @Override
  public final HashMap<String, FillMessage> doTrade(Tradeable trd)
          throws InvalidMessageException, InvalidVolumeException,
          ProductBookSideException, ProductBookException,
          TradeProcessorPriceTimeImplException {
    validateInput(trd);
    fillMessages = new HashMap<>();
    ArrayList<Tradeable> tradedOut = new ArrayList<>();
    ArrayList<Tradeable> entriesAtPrice = parent.getEntriesAtTopOfBook();
    for (Tradeable t : entriesAtPrice) {
      if (trd.getRemainingVolume() != 0) {
        // No
        if (trd.getRemainingVolume() >= t.getRemainingVolume()) {
          // yes
          tradedOut.add(t);
          Price tPrice;
          if (t.getPrice().isMarket()) {
            // yes
            tPrice = trd.getPrice();
          } else {
            // no
            tPrice = t.getPrice();
          }
          FillMessage tFill = new FillMessage(t.getUser(), t.getProduct(),
                  tPrice, t.getRemainingVolume(), "leaving " + 0 , t.getSide(),
                  t.getId());
          addFillMessage(tFill);
          FillMessage trdFill = new FillMessage (trd.getUser(), t.getProduct(),
                  tPrice, t.getRemainingVolume(), "leaving " +
                  (trd.getRemainingVolume() - t.getRemainingVolume()),
                  trd.getSide(), trd.getId());
          addFillMessage(trdFill);
          trd.setRemainingVolume(trd.getRemainingVolume()
                  - t.getRemainingVolume());
          t.setRemainingVolume(0);
          parent.addOldEntry(t);
        } else {
          // no
          int remainder = t.getRemainingVolume() - trd.getRemainingVolume();
          Price tPrice;
          if (t.getPrice().isMarket()) {
            // yes
            tPrice = trd.getPrice();
          } else {
            // no
            tPrice = t.getPrice();
          }
          FillMessage tFill = new FillMessage(t.getUser(), t.getProduct(),
                  tPrice, trd.getRemainingVolume(), "leaving " +
                  remainder, t.getSide(), t.getId());
          addFillMessage(tFill);
          FillMessage trdFill = new FillMessage(trd.getUser(), t.getProduct(),
                  tPrice, trd.getRemainingVolume(),
                  "leaving " + 0, trd.getSide(), trd.getId());
          addFillMessage(trdFill);
          trd.setRemainingVolume(0);
          t.setRemainingVolume(remainder);
          parent.addOldEntry(trd);
        }
      }
    }
    // Yes
    // After for section
    for (Tradeable t : tradedOut) {
      entriesAtPrice.remove(t);
    }
    if (entriesAtPrice.isEmpty()) {
      parent.clearIfEmpty(parent.topOfBookPrice());
    }
    return fillMessages;
  }

  private void validateInput(ProductBookSide o)
          throws InvalidProductBookSideValueException {
    if (o == null) {
      throw new InvalidProductBookSideValueException("Argument cannot be null.");
    }
  }

  private void validateInput(Object o)
          throws TradeProcessorPriceTimeImplException {
    if (o == null) {
      throw new TradeProcessorPriceTimeImplException("Argument cannot be null.");
    }
  }
}