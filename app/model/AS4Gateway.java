package model;

/**
 * Author: yerlibilgin
 * Date:   17/10/15.
 */
public class AS4Gateway {
  public int id;
  public String name;
  public String partyID;
  public String backendAddress;
  public String mshAddress;
  /**
   * TRUE: just forward messages to this gateway when you receive
   * FALSE: send messages to minder
   */
  public boolean proxyMode;

  public AS4Gateway() {
    this.proxyMode = true;
  }

  public AS4Gateway(int id, String name, String partyID, String backendAddress, String mshAddress, boolean proxyMode) {
    this.id = id;
    this.name = name;
    this.partyID = partyID;
    this.backendAddress = backendAddress;
    this.mshAddress = mshAddress;
    this.proxyMode = proxyMode;
  }

  /**
   * Return backend address if it is valid, otherwise, return the address (default)
   * @return
   */
  public String getBackendAddress() {
    if (backendAddress == null || backendAddress.length() == 0) {
      return mshAddress;
    }

    return backendAddress;
  }
}

