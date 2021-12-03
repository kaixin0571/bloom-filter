# 缓存三大问题



## 关于缓存

![点击查看源网页](https://gimg2.baidu.com/image_search/src=http%3A%2F%2Finews.gtimg.com%2Fnewsapp_bt%2F0%2F12585436283%2F1000.jpg&refer=http%3A%2F%2Finews.gtimg.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1641042696&t=516c1a77debc02d163fee092b5bf8783)



## 1.缓存穿透

缓存穿透是指查询一个一定不存在的数据，因为缓存中也无该数据的信息，则会直接去数据库层进行查询，从系统层面来看像是穿透了缓存层直接达到db，从而称为缓存穿透，没有了缓存层的保护，如果有人恶意用这种一定不存在的数据来频繁请求系统，请求都会到达数据库层导致db瘫痪从而引起系统故障



## 2.缓存雪崩

在普通的缓存系统中一般例如redis、memcache等中，我们会给缓存设置一个失效时间，但是如果所有的缓存的失效时间相同，那么在同一时间失效时，所有系统的请求都会发送到数据库层，db可能无法承受如此大的压力导致系统崩溃



## 3.缓存击穿

缓存击穿实际上是缓存雪崩的一个特例，由于系统中对这些热点数据缓存也存在失效时间，在热点的缓存到达失效时间时，此时可能依然会有大量的请求到达系统，没有了缓存层的保护，这些请求同样的会到达db从而可能引起故障。击穿与雪崩的区别即在于击穿是对于特定的热点数据来说，而雪崩是全部数据。


## 4.布隆过滤器演示
https://www.jasondavies.com/bloomfilter/

## 5.Redis实现布隆过滤器

```java
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

```



## 5.Redis实现原理

