package com.ikea.bloomfilter.demo;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class InsertData {

  private static final String SQL = "INSERT INTO items (item_no, item_name) VALUES('%s', '%s');";

  public static void main(String[] args) throws IOException {
    List<String> data = Lists.newArrayList();
    for (int i = 1; i < 10000; i++) {
      data.add(String.format(SQL, i, "item" + i));
    }

    Files.write(Paths.get("insert_data.sql"), data, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

  }

}
