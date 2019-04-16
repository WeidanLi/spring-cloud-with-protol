package com.microorder.test;

import com.microorder.test.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Description：{DESC}
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-16
 * @email toweidan@126.com
 */
@FeignClient(url = "http://localhost:8080/", name = "userFeign")
public interface UserFeign {

  @GetMapping
  User.Person test();

  @PostMapping
  void add(@RequestBody User.Person param);

}
