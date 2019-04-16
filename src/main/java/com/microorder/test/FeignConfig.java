package com.microorder.test;

import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：{DESC}
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-16
 * @email toweidan@126.com
 */
@Configuration
public class FeignConfig {

  /**
   * 如果使用了RestTemplate，进行以下配置.
   * @return
   */
  @Bean
  RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    List<HttpMessageConverter<?>> of = new ArrayList<HttpMessageConverter<?>>();
    of.add(new ProtobufHttpMessageConverter());
    restTemplate.setMessageConverters(of);
    return restTemplate;
  }

  @Bean
  public Decoder protolDecoder() {
    return new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(new ProtobufHttpMessageConverter())));
  }

  @Bean
  public Encoder protolEncoder() {
    return new SpringEncoder(() -> new HttpMessageConverters(new ProtobufHttpMessageConverter()));
  }

}
