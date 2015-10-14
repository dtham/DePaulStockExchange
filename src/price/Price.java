package price;

import price.exceptions.InvalidPriceOperation;
import price.exceptions.PriceException;


public class Price implements Comparable<Price> {

  Price self = this;
  boolean isMarketPrice;
  long value;

  private final int BEFORE = -1;
  private final int EQUAL = 0;
  private final int AFTER = 1;

  /**
   * Creates a Limit Price.
   *
   * @param value
   */
  public Price(long value) {
    self.value = value;
    self.isMarketPrice = false;
  }

  /**
   * Creates a Market Price.
   */
  public Price() {
    self.value = 0;
    self.isMarketPrice = true;
  }

  public Price add(Price p) throws InvalidPriceOperation,
          PriceException {
    validateInput(p);
    if (self.isMarket() || p.isMarket()) {
      throw new InvalidPriceOperation("Invalid Price Operation in add: "
              + "Current Price or Price passed in is a Market Price!");
    }
    return PriceFactory.makeLimitPrice(self.value + p.value);
  }

  public Price subtract(Price p) throws InvalidPriceOperation,
          PriceException {
    validateInput(p);
    if (self.isMarket() || p.isMarket()) {
      throw new InvalidPriceOperation("Invalid Price Operation in subtract: "
              + "Current Price or Price passed in is a Market Price!");
    }
    return PriceFactory.makeLimitPrice(self.value - p.value);
  }

  public Price multiply(int p) throws InvalidPriceOperation {
    if (self.isMarket()) {
      throw new InvalidPriceOperation("Invalid Price Operation in multiply: "
              + "Current Price is a Market Price!");
    }
    return PriceFactory.makeLimitPrice(self.value * p);
  }

  public boolean isMarket() {
    return self.isMarketPrice;
  }

  public boolean isNegative() {
    return ((self.isMarket() || self.value >= 0) ?
            false : true );
  }

  public boolean greaterOrEqual(Price p) {
    if (p.isMarket() || self.isMarket()) { return false; }
    int comparison = self.compareTo(p);
    return ((comparison == self.AFTER || comparison == self.EQUAL) ?
            true : false);
  }

  public boolean greaterThan(Price p) {
    if (p.isMarket() || self.isMarket()) { return false; }
    int comparison = self.compareTo(p);
    return (comparison == self.AFTER ? true : false);
  }

  public boolean lessOrEqual(Price p) {
    if (p.isMarket() || self.isMarket()) { return false; }
    int comparison = self.compareTo(p);
    return ((comparison == self.BEFORE || comparison == self.EQUAL) ?
            true : false);
  }

  public boolean lessThan(Price p) {
    if (p.isMarket() || self.isMarket()) { return false; }
    int comparison = self.compareTo(p);
    return (comparison == self.BEFORE ? true : false);
  }

  public boolean equals(Price p) {
    if (p.isMarket() || self.isMarket()) { return false; }
    return ((self.compareTo(p) == self.EQUAL) ? true : false);
  }

  @Override
  public int compareTo(Price p) {
    // checks to see is self and p are the same object
    if (self.value == p.value) { return self.EQUAL; }

    if (self.value < p.value) { return self.BEFORE; }
    if (self.value > p.value) { return self.AFTER; }

    // Default is equal
    return self.EQUAL;
  }

  @Override
  public String toString() {
    return (self.isMarket() ? "MKT" : String.format("$%,.2f", self.value / 100.0));
  }

  private void validateInput(Price o) throws PriceException {
    if (o == null || !(o instanceof Price)) {
      throw new PriceException("Argument must be of type Price and"
              + " cannot be null.");
    }
  }
}