package com.ikea.bloomfilter.controller;

import com.ikea.bloomfilter.dto.ItemDto;
import com.ikea.bloomfilter.dto.R;
import com.ikea.bloomfilter.service.ItemService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @GetMapping("/query/item")
  public R queryItems(@RequestParam(value = "itemNo") String itemNo) {

    return itemService.queryByItemNo(itemNo);

  }

}
