package com.ikea.bloomfilter.constant;

public class Constants {

  public static final String QUERY_ITEM = "select * from items where item_no='%s'";
  public static final String QUERY_ALL_ITEM_NO = "select item_no from items";
  public static final String INSERT = "INSERT INTO items (item_no, item_name) VALUES(?, ?);";

}
