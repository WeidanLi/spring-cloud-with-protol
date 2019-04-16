package com.microorder.test.dto;

/**
 * Description：{DESC}
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-16
 * @email toweidan@126.com
 */
public class PersonJson {

  private String name;
  private Integer id;

  public PersonJson(String name, Integer id) {
    this.name = name;
    this.id = id;
  }

  public PersonJson() {
  }

  public String getName() {
    return name;
  }

  public Integer getId() {
    return id;
  }
}
