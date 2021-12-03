package com.ikea.bloomfilter.demo;

import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import java.util.List;

public class GuavaBloomFilterDemo {

  private static final int SIZE = 1000000;

  public static void main(String[] args) {
    BloomFilter<Integer> bloomFilter = BloomFilter
        .create(Funnels.integerFunnel(), SIZE, 0.001);
    for (int i = 1; i <= SIZE; i++) {
      bloomFilter.put(i);
    }

    List<Integer> list = Lists.newArrayList(10000);
    for (int m = SIZE + 10000; m < SIZE + 100000; m++) {
      if (bloomFilter.mightContain(m)) {
        list.add(m);
      }
    }
    System.out.println("误判数量=" + list.size());
  }

}
