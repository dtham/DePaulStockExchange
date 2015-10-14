package client;

import client.exceptions.TradeableUserDataException;
import constants.GlobalConstants.BookSide;


public class TradeableUserData {

  /**
   * The name of the user.
   */
  private String userName;

  /**
   * The stock symbol.
   */
  private String product;

  /**
   * The side the order is on.
   */
  private BookSide side;

  private TradeableUserData self = this;

  /**
   * The order ID.
   */
  String id;

  /**
   * Public Constructor for TradeableUserData class.
   *
   * @param theName
   * @param theProduct
   * @param theSide
   * @param theID
   * @throws TradeableUserDataException
   */
  public TradeableUserData(String theName, String theProduct, BookSide theSide,
          String theID) throws TradeableUserDataException {
    setUserName(theName);
    setProduct(theProduct);
    setSide(theSide);
    setID(theID);
  }

  /**
   * Returns the use name for the tradeable that was submitted.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Returns the stock symbol for the tradeable that was submitted.
   *
   * @return the stock symbol
   */
  public String getProduct() {
    return product;
  }

  /**
   * Returns the side for the tradeable that was submitted.
   *
   * @return the side for the tradeable
   */
  public BookSide getSide() {
    return side;
  }

  /**
   * Returns the order ID for the tradeable that was submitted.
   *
   * @return the order id
   */
  public String getID() {
    return id;
  }

  private void setUserName(String user) throws TradeableUserDataException{
    validateInput(user);
    userName = user;
  }

  private void setProduct(String prod) throws TradeableUserDataException {
    validateInput(prod);
    product = prod;
  }

  private void setSide(BookSide theSide) throws TradeableUserDataException {
    validateInput(theSide);
    side = theSide;
  }

  private void setID(String theID) throws TradeableUserDataException {
    validateInput(theID);
    id = theID;
  }

  @Override
  public String toString() {
    return "User " + self.getUserName() + ": " + self.getSide() + " " +
            self.getProduct() + " (" + self.getID() + ")";
  }

  private void validateInput(String o)
          throws TradeableUserDataException {
    if (o == null || o.isEmpty()) {
      throw new TradeableUserDataException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(BookSide o) throws TradeableUserDataException {
    if (o == null || !(o instanceof BookSide)) {
      throw new TradeableUserDataException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }
}