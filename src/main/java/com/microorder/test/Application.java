package com.microorder.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Description：{DESC}
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-15
 * @email toweidan@126.com
 */
@SpringBootApplication
@EnableFeignClients
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }



}
