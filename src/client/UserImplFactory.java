package client;

import client.exceptions.UserException;


public class UserImplFactory {

  public static UserImpl createUser(String userName)
          throws UserException {
    return new UserImpl(userName);
  }
}