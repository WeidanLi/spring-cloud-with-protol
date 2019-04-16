package com.microorder.test.endpoint;

import com.microorder.test.dto.PersonJson;
import com.microorder.test.dto.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Description：{DESC}
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-15
 * @email toweidan@126.com
 */
@RestController
public class UserEndpoint {

  @GetMapping
  public User.Person test() {
    User.Person build = User.Person.newBuilder().setId(222).setName("23232").build();
    System.out.println(Arrays.toString(build.toByteArray()));
    return build;
  }

  @GetMapping("json")
  public PersonJson test2() {
    return new PersonJson("ssss", 22);
  }

  @PostMapping
  public void add(@RequestBody User.Person param) {
    System.out.println(param);
  }

}
