package com.ikea.bloomfilter.dto;

import lombok.Data;

@Data
public class R<T> {

  private String code;
  private String message;
  private T data;

  public R<T> success(T data) {
    this.setCode("Success");
    this.setMessage("查找成功");
    this.setData(data);
    return this;
  }

  public R<T> fail() {
    this.setCode("Fail");
    this.setMessage("查找失败");
    return this;
  }

}
