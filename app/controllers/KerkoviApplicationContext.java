package controllers;


import as4Interceptor.AS4Adapter;
import backend.GenericAS4Corner1;
import backend.GenericAS4Corner4;
import java.util.Properties;

public class KerkoviApplicationContext{
  public static AS4Adapter as4Adapter;
  public static GenericAS4Corner1 genericCorner1;
  public static GenericAS4Corner4 genericCorner4;
  public static Properties serviceProperties;
  public static Properties actionProperties;
  public static Properties pmodes;
  public static String myPartyID;
}
