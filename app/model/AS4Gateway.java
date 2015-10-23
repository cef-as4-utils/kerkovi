package model;

/**
 * Author: yerlibilgin
 * Date:   17/10/15.
 */
public class AS4Gateway {
  public int id;
  public String name;
  public String c2PartyID;
  public String c3PartyID;
  public String c2Address;
  public String c3Address;
  public ConformancePhase phase;
  public boolean oneWayDone;
  public boolean twoWayDone;

  public AS4Gateway() {
    this.phase = ConformancePhase.CONNECTIVITY;
  }

  public AS4Gateway(int id, String name, String c2PartyID, String c3PartyID, String c2Address, String c3Address, ConformancePhase phase, boolean oneWayDone, boolean twoWayDone) {
    this.id = id;
    this.name = name;
    this.c2PartyID = c2PartyID;
    this.c3PartyID = c3PartyID;
    this.c2Address = c2Address;
    this.c3Address = c3Address;
    this.phase = phase;
    this.oneWayDone = oneWayDone;
    this.twoWayDone = twoWayDone;
  }
}

