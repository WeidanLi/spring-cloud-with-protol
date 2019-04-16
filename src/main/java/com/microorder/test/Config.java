package com.microorder.test;

import com.google.common.collect.Lists;
import com.microorder.test.dto.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Description：{DESC}
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-15
 * @email toweidan@126.com
 */
@Configuration
@EnableWebMvc
public class Config implements WebMvcConfigurer {
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new ProtobufHttpMessageConverter());
  }

  public static void main(String[] args) throws IOException {
    List<Long> timeList = Lists.newArrayListWithCapacity(1000000);
    for (int i = 0; i < 1000000; i++) {
      long start = System.nanoTime();
      URL target = new URL("http://localhost:8080/");
      HttpURLConnection conn = (HttpURLConnection) target.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Type", "application/x-protobuf");
      conn.setRequestProperty("Accept", "application/x-protobuf");
      InputStream inputStream = conn.getInputStream();
      User.Person person = User.Person.parseFrom(inputStream);
      timeList.add(System.nanoTime() - start);
    }

    Long sum = 0L;
    for (int i = 10000; i < 1000000; i++) {
      Long aLong = timeList.get(i);
      sum += aLong;
    }
    System.out.println("平均时间：" + (sum / 990000));
    // 平均时间：316688 0.316688毫秒(ms)

//    List<Long> timeList = Lists.newArrayListWithCapacity(1000000);
//
//    for (int i = 0; i < 1000000; i++) {
//      long start = System.nanoTime();
//      URL target = new URL("http://localhost:8080/json");
//      HttpURLConnection conn = (HttpURLConnection) target.openConnection();
//      conn.setRequestMethod("GET");
//      conn.setRequestProperty("Content-Type", "application/json");
//      conn.setRequestProperty("Accept", "application/json");
//      InputStream inputStream = conn.getInputStream();
//      byte[] b = new byte[1024];
//      inputStream.read(b);
//      inputStream.close();
//      PersonJson personJson = JsonMapper.defaultMapper().fromJson(new String(b), PersonJson.class);
//      timeList.add(System.nanoTime() - start);
//    }
//
//
//    Long sum = 0L;
//    for (int i = 10000; i < 1000000; i++) {
//      Long aLong = timeList.get(i);
//      sum += aLong;
//    }
//    System.out.println("平均时间：" + (sum / 990000));
    // 平均时间：825082 0.825082毫秒(ms)
  }
}
