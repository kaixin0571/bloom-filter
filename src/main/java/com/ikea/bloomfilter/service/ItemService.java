package com.ikea.bloomfilter.service;

import com.ikea.bloomfilter.constant.Constants;
import com.ikea.bloomfilter.dto.ItemDto;
import com.ikea.bloomfilter.dto.NullValueDto;
import com.ikea.bloomfilter.dto.R;
import com.ikea.bloomfilter.repository.ItemRepository;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

/**
 * @author yonlv2
 */
@Repository
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final RedisTemplate redisTemplate;
  private final RedisBloomFilter redisBloomFilter;

  public R queryByItemNo(String itemNo) {

    if(!redisBloomFilter.mightContain(itemNo)){
      return new R<>().fail();
    }


    ValueOperations opsForValue = redisTemplate.opsForValue();

    Object object = opsForValue.get(itemNo);
    if (Objects.nonNull(object)) {
      return new R<>().success(object);
    }
    ItemDto itemDto = itemRepository.queryByItemNo(itemNo);
    if (Objects.nonNull(itemDto)) {
      opsForValue.set(itemNo, itemDto, 10, TimeUnit.MINUTES);
      return new R<>().success(itemDto);
    }
    return new R<>().fail();
  }

  @PostConstruct
  public void initBloomFilter() {

    List<String> allItemNo = itemRepository.getAllItemNo();
    allItemNo.stream().forEach(redisBloomFilter::put);
  }

}
