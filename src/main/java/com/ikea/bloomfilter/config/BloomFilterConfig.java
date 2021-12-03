package com.ikea.bloomfilter.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.ikea.bloomfilter.service.RedisBloomFilter;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author yonlv2
 */
@Configuration
public class BloomFilterConfig {

  private static final int MAX_SIZE = 10000000;

  @Bean(name = "guavaBloomFilter")
  public BloomFilter<String> guavaBloomFilter() {
    return BloomFilter
        .create(Funnels.stringFunnel(StandardCharsets.UTF_8), MAX_SIZE, 0.01);
  }

  @Bean(name = "redisBloomFilter")
  public RedisBloomFilter redisBloomFilter(RedisTemplate redisTemplate) {
    return new RedisBloomFilter(100000, 0.001, redisTemplate);
  }

}
