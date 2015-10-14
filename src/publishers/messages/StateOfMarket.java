/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package publishers.messages;

import constants.GlobalConstants.MarketState;


public interface StateOfMarket {

  /**
   * Returns the state of the Market (CLOSED, OPEN, PREOPEN).
   *
   * @return the state of the market.
   */
  MarketState getState();
}
