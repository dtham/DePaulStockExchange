package client;

import client.exceptions.TradableUserDataException;
import constants.GlobalConstants.BookSide;


public class TradableUserData {
  private String userName;
  private String product;
  private BookSide side;
  private TradableUserData self = this;
  String id;


  public TradableUserData(String theName, String theProduct, BookSide theSide,
          String theID) throws TradableUserDataException {
    setUserName(theName);
    setProduct(theProduct);
    setSide(theSide);
    setID(theID);
  }

  public String getUserName() {
    return userName;
  }

  public String getProduct() {
    return product;
  }


  public BookSide getSide() {
    return side;
  }


  public String getID() {
    return id;
  }

  private void setUserName(String user) throws TradableUserDataException{
    validateInput(user);
    userName = user;
  }

  private void setProduct(String prod) throws TradableUserDataException {
    validateInput(prod);
    product = prod;
  }

  private void setSide(BookSide theSide) throws TradableUserDataException {
    validateInput(theSide);
    side = theSide;
  }

  private void setID(String theID) throws TradableUserDataException {
    validateInput(theID);
    id = theID;
  }

  @Override
  public String toString() {
    return "User " + self.getUserName() + ": " + self.getSide() + " " +
            self.getProduct() + " (" + self.getID() + ")";
  }

  private void validateInput(String o)
          throws TradableUserDataException {
    if (o == null || o.isEmpty()) {
      throw new TradableUserDataException("Argument must be of type String and"
        + " cannot be null or empty.");
    }
  }

  private void validateInput(BookSide o) throws TradableUserDataException {
    if (o == null || !(o instanceof BookSide)) {
      throw new TradableUserDataException("Argument must be of type BookSide and"
              + " cannot be null.");
    }
  }
}