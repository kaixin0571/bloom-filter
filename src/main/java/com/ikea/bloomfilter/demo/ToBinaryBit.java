package com.ikea.bloomfilter.demo;

import org.apache.commons.lang3.StringUtils;

public class ToBinaryBit {

  public static void main(String[] args) {
    System.out.println(toBinary("abc"));
  }

  private static String toBinary(String str) {
    char[] strChar = str.toCharArray();
    String result = "";
    for (int i = 0; i < strChar.length; i++) {
      result += Integer.toBinaryString(strChar[i]) + " ";
    }
    return result;
  }

}
