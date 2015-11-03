package model;

/**
 * Author: yerlibilgin
 * Date:   17/10/15.
 */
public class AS4Gateway {
  public int id;
  public String name;
  public String partyID;
  public String address;
  /**
   * TRUE: just forward messages to this gateway when you receive
   * FALSE: send messages to minder
   */
  public boolean proxyMode;

  public AS4Gateway() {
    this.proxyMode = true;
  }

  public AS4Gateway(int id, String name, String partyID, String address, boolean proxyMode) {
    this.id = id;
    this.name = name;
    this.partyID = partyID;
    this.address = address;
    this.proxyMode = proxyMode;
  }
}

