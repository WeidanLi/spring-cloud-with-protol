package com.microorder.test;

import com.microorder.test.dto.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;

/**
 * Description：{DESC}
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-16
 * @email toweidan@126.com
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserFeignTest {

  @Autowired
  private UserFeign userFeign;
  @Autowired
  private RestTemplate restTemplate;

  @Test
  public void testRestTemplate() {
    User.Person test = restTemplate.getForObject("http://localhost:8080/", User.Person.class);
    assertNotNull("传递对象不能为空", test);
  }

  @Test
  public void testFeign() {
    User.Person test = userFeign.test();
    assertNotNull("传递对象不能为空", test);
  }

  @Test
  public void testAdd() {
    User.Person weoda = User.Person.newBuilder().setName("Weoda").setId(2000).build();
    userFeign.add(weoda);
  }

}