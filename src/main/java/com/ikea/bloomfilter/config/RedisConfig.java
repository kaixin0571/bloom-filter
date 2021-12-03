package com.ikea.bloomfilter.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  private final RedisProperties redisProperties;

  @Bean
  public RedisTemplate<Object, Object> redisTemplate(
      LettuceConnectionFactory lettuceConnectionFactory) {
    RedisTemplate<Object, Object> template = new RedisTemplate<>();

    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
        Object.class);
    ObjectMapper om = new ObjectMapper();
    om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    jackson2JsonRedisSerializer.setObjectMapper(om);

    template.setKeySerializer(jackson2JsonRedisSerializer);
    template.setValueSerializer(jackson2JsonRedisSerializer);
    template.setHashKeySerializer(jackson2JsonRedisSerializer);
    template.setHashValueSerializer(jackson2JsonRedisSerializer);

    template.setConnectionFactory(lettuceConnectionFactory);
    return template;
  }

  @Bean
  @ConditionalOnMissingBean(StringRedisTemplate.class)
  public StringRedisTemplate stringRedisTemplate(
      LettuceConnectionFactory lettuceConnectionFactory) {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(lettuceConnectionFactory);
    return template;
  }

  @Bean
  LettuceConnectionFactory lettuceConnectionFactory(
      GenericObjectPoolConfig genericObjectPoolConfig) {
    //stand-alone config
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());
    redisStandaloneConfiguration.setHostName(redisProperties.getHost());
    redisStandaloneConfiguration.setPort(redisProperties.getPort());
    redisStandaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));

    LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
        .commandTimeout(Duration.ofMillis(redisProperties.getTimeout()))
        .poolConfig(genericObjectPoolConfig)
        .build();

    return new LettuceConnectionFactory(redisStandaloneConfiguration,
        clientConfig);
  }

  @Bean
  public GenericObjectPoolConfig genericObjectPoolConfig() {
    GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
    genericObjectPoolConfig.setMaxIdle(redisProperties.getLettuce().getMaxIdle());
    genericObjectPoolConfig.setMinIdle(redisProperties.getLettuce().getMinIdle());
    genericObjectPoolConfig.setMaxTotal(redisProperties.getLettuce().getMaxActive());
    genericObjectPoolConfig.setMaxWaitMillis(redisProperties.getLettuce().getMaxWait());
    return genericObjectPoolConfig;
  }
}
