package com.ikea.bloomfilter.service;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author yonlv2
 */
public class RedisBloomFilter {

  private int numOfHashFunctions;
  private long numOfBits;
  private RedisTemplate redisTemplate;
  private static final String bloomFilterKey = "bf:redis";

  public RedisBloomFilter(long expectedInsertions, double fpp,
      RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.numOfBits = optimalNumOfBits(expectedInsertions, fpp);
    this.numOfHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numOfBits);
  }


  static int optimalNumOfHashFunctions(long n, long m) {
    // (m / n) * log(2), but avoid truncation due to division!
    return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
  }


  static long optimalNumOfBits(long n, double p) {
    if (p == 0) {
      p = Double.MIN_VALUE;
    }
    return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
  }

  public void put(String key) {
    long[] hashIndex = getHashIndex(key);
    redisTemplate.executePipelined((RedisConnection connection) -> {
      connection.openPipeline();
      for (long index : hashIndex) {
        connection.setBit(bloomFilterKey.getBytes(StandardCharsets.UTF_8), index, true);
      }
      return null;
    });

  }

  public boolean mightContain(String key) {
    long[] hashIndex = getHashIndex(key);
    List list = redisTemplate.executePipelined((RedisConnection connection) -> {
      connection.openPipeline();
      for (long index : hashIndex) {
        connection.getBit(bloomFilterKey.getBytes(StandardCharsets.UTF_8), index);
      }
      return null;
    });

    return !list.contains(false);

  }

  private long[] getHashIndex(String key) {
    long hash64 = Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asLong();
    int hash1 = (int) hash64;
    int hash2 = (int) (hash64 >>> 32);
    long[] index = new long[numOfHashFunctions];
    for (int i = 0; i < numOfHashFunctions; i++) {
      int combinedHash = hash1 + i * hash2;
      if (combinedHash < 0) {
        combinedHash = ~combinedHash;
      }

      index[i] = combinedHash % numOfBits;
    }

    return index;
  }

}
