package com.ikea.bloomfilter.repository;

import com.ikea.bloomfilter.constant.Constants;
import com.ikea.bloomfilter.dto.ItemDto;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * @author yonlv2
 */
@Repository
@RequiredArgsConstructor
public class ItemRepository {

  private final JdbcTemplate jdbcTemplate;

  public ItemDto queryByItemNo(String itemNo) {
    String sql = String.format(Constants.QUERY_ITEM, itemNo);

    List<ItemDto> ret = jdbcTemplate.query(sql,
        (rs, rowNum) -> ItemDto.builder()
            .itemNo(rs.getString("item_no"))
            .itemName(rs.getString("item_name"))
            .build());
    if (CollectionUtils.isEmpty(ret)) {
      return null;
    }
    return ret.get(0);
  }

  public List<String> getAllItemNo() {
    return jdbcTemplate.queryForList(Constants.QUERY_ALL_ITEM_NO, String.class);
  }


}
