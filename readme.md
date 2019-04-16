# SpringCloud使用ProtocolBuffers传递数据

  * [前言](#前言)
  * [ProtocolBuffers入门](#protocolbuffers入门)
    * [下载ProtocolBuffer编译器](#下载protocolbuffer编译器)
    * [编写简单的proto文件](#编写简单的proto文件)
    * [编译生成对象](#编译生成对象)
* [命令开头    proto文件夹      生成放的文件夹       源文件](#命令开头----proto文件夹------生成放的文件夹-------源文件)
  * [配置SpringMVC](#配置springmvc)
    * [开发MessageConverter](#开发messageconverter)
    * [配置到MVC中](#配置到mvc中)
    * [开发接口层](#开发接口层)
  * [接口调用测试](#接口调用测试)
  * [Feign整合ProtocolBuffer](#feign整合protocolbuffer)
    * [配置Encoder和Decoder](#配置encoder和decoder)
    * [UserFeign](#userfeign)
    * [测试用例](#测试用例)
  * [项目说明](#项目说明)


## 前言

我们知道，使用 `SpringCloud` 技术栈，上下游传值的方式一般使用 `form` 表单或者使用 `JSON` 格式进行传值。但是我感觉，我们内部服务进行传值的时候，还使用这两种类型的方式，显得有点重。所以在查询了 `Java` 界以及其他语言序列化对象的时候，查询到了几个常用的序列化工具：`kryo` `Hession` `JSON` `XML` `Protocol Buffers`

`JSON` `XML` 就不必多言，使用 `Java` 语言开发的基本都知道。

`kryo` 效率高，使用二进制文件进行传递，但是有个缺点就是不能跨语言。

`Hession` 效率稍稍差一点。

那么剩下的 `Protocol Buffers` 就是能够弥补上面的缺点，并且带来一个新的缺点：需要编写静态的 `.proto` 文件使其项目启动的时候，静态编译映射规格。不过速度高，跨语言，这点缺陷我还是可以接受的。

基本确定方向以后，那么本文就从怎么使用 `.proto` 将其整合到我们常用的 `SpringMVC` 中去，使其序列化和反序列化的过程对我们业务开发不可见。

## ProtocolBuffers入门

`ProtocolBuffers` 规则是这样的：编辑 `.proto` 文件，使用编译器编译不同语言的 `ClassObject` 。有点类似于 `Thift` 。所以一开始我们需要的是 `ProtocolBuffer编译器` 。

### 下载ProtocolBuffer编译器

[下载地址](https://github.com/protocolbuffers/protobuf/releases/tag/v3.7.1)

根据自己所使用的系统版本，下载对应的编译器。

下载完成以后，进入 `protoc-3.7.1-osx-x86_64/bin` 运行 `./protoc --version` 如果能够打印版本号则说明安装成功。

### 编写简单的proto文件

现在开始尝试生成类似于以下 `Java` 类

```java
public class User {
  private String name;
  private Integer id;
  // 省略 getter setter
}
```

根据谷歌官方提供的 [规范文档](https://developers.google.com/protocol-buffers/docs/proto3#scalar) 进行编写：

```protobuf
syntax = "proto2";// 类似于xml声明一样放于第一行，指定编译源protobuf文件的版本号

package tutorial;
option java_package = "com.microorder.test.dto";

message Person {
	// required 表示构造的时候这个值必须传递
	// = 后面的值需要每个属性都是唯一的
  required string name = 1;
  required int32 id = 2;
}

```

> 后面的 id 号是protocolBuffer用来编码的方式，1-15是使用1个字节，16-2047是使用两个字节。
>
> 1-15常用来定义经常发生变化的元素，在使用的时候记得需要保留一下以供后面的属性加入。
>
> 19000-19999 不应该使用，因为这个是框架内部使用的。

### 编译生成对象

```shell
./protoc -I../proto/ --java_out=./ ../proto/user.proto
# 命令开头    proto文件夹      生成放的文件夹       源文件
```

生成的类就不打开了，看不懂...

生成对象先放入项目中，后面使用。

## 配置SpringMVC

### 开发MessageConverter

> SpringMVC 官方自带了 ProtocolBuffer 的消息转换器，不过我写都写了，放上来吧...
>
> 官方的名字和我取的名字是一样的，如果需要使用官方的，直接导入官方包即可。
>
> 调用头使用 x-protobuf 即可接收，不过还是需要在配置中配置，因为默认没有启用。

我们知道开发 `SpringMVC` 的消息转换器需要继承 `AbstractHttpMessageConverter` 传递类型的泛型。

然后使用配置类将此转换器加入 `SpringMVC` 的转换器列表中即可.

```java
import com.google.protobuf.GeneratedMessageV3;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Description：将SpringMVC接收和输出对象转换成protobuf格式的转换器.
 *
 * @author liweidan
 * @version 1.0
 * @date 2019-04-15
 * @email toweidan@126.com
 */
public class ProtobufHttpMessageConverter extends AbstractHttpMessageConverter<GeneratedMessageV3> {

  public ProtobufHttpMessageConverter() {
    //设置该转换器支持的媒体类型
    super(new MediaType("application", "protobuf"));
  }

  /**
   * 支持的对象类型.
   * 如果一个对象的父类是GeneratedMessageV3，就执行该转换器
   *
   * @param assignableFrom
   * @return
   */
  @Override
  protected boolean supports(Class<?> assignableFrom) {
    return GeneratedMessageV3.class.isAssignableFrom(assignableFrom);
  }

  @Override
  protected GeneratedMessageV3 readInternal(
          Class<? extends GeneratedMessageV3> genMsgClazz, HttpInputMessage httpInputMessage)
          throws IOException, HttpMessageNotReadableException {
    Method parseMethod;
    try {
      //利用反射机制获得parseFrom方法
      parseMethod = genMsgClazz.getDeclaredMethod("parseFrom", InputStream.class);
    } catch (SecurityException | NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
    try {
      //调用parseFrom方法从InputStream中反序列化PB对象
      return (GeneratedMessageV3) parseMethod.invoke(genMsgClazz, httpInputMessage.getBody());
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  protected void writeInternal(
          GeneratedMessageV3 generatedMessageV3, HttpOutputMessage httpOutputMessage)
          throws IOException, HttpMessageNotWritableException {
    OutputStream outputStream = httpOutputMessage.getBody();
    generatedMessageV3.writeTo(outputStream);
    outputStream.flush();
    outputStream.close();
  }

}
```

> 代码来源于 CSDN 某个博主，因为记录到笔记，原地址没了...

### 配置到MVC中

```java
@Configuration
@EnableWebMvc
public class Config implements WebMvcConfigurer {
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new ProtobufHttpMessageConverter());
  }
}
```

### 开发接口层

接口层我放了两个接口，目的是为了测试 `ProtocolBuffer` 是否真的比 `JSON` 优秀

```java
@RestController
public class UserEndpoint {

  // 使用 protocolBuffer 传递
  @GetMapping
  public User.Person test() {
    User.Person build = User.Person.newBuilder().setId(222).setName("23232").build();
    System.out.println(Arrays.toString(build.toByteArray()));
    return build;
  }

  // 使用 JSON 传递
  @GetMapping("json")
  public PersonJson test2() {
    return new PersonJson("ssss", 22);
  }

}
```

## 接口调用测试

测试方式是粗略测试，在本机运行。

测试方式是：调用 `1_000_000` 此接口，除去前面 `10_000` 此预热的结果，使用后面 `990_000` 计算均值。

```java
// JSON 方式
List<Long> timeList = Lists.newArrayListWithCapacity(1000000);

for (int i = 0; i < 1000000; i++) {
  long start = System.nanoTime();
  URL target = new URL("http://localhost:8080/json");
  HttpURLConnection conn = (HttpURLConnection) target.openConnection();
  conn.setRequestMethod("GET");
  conn.setRequestProperty("Content-Type", "application/json");
  conn.setRequestProperty("Accept", "application/json");
  InputStream inputStream = conn.getInputStream();
  byte[] b = new byte[1024];
  inputStream.read(b);
  inputStream.close();
  PersonJson personJson = JsonMapper.defaultMapper().fromJson(new String(b), PersonJson.class);
  timeList.add(System.nanoTime() - start);
}


Long sum = 0L;
for (int i = 10000; i < 1000000; i++) {
  Long aLong = timeList.get(i);
  sum += aLong;
}
System.out.println("平均时间：" + (sum / 990000));
// 平均时间：825082 0.825082毫秒(ms)

// ProtocolBuffer方式
List<Long> timeList = Lists.newArrayListWithCapacity(1000000);
for (int i = 0; i < 1000000; i++) {
  long start = System.nanoTime();
  URL target = new URL("http://localhost:8080/");
  HttpURLConnection conn = (HttpURLConnection) target.openConnection();
  conn.setRequestMethod("GET");
  conn.setRequestProperty("Content-Type", "application/protobuf");
  conn.setRequestProperty("Accept", "application/protobuf");
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
```

结果很明显了，`ProtocolBuffer` 方式调用使用的时间更短。

## Feign整合ProtocolBuffer

用过 `SpringCloud` 基本都知道 `OpenFeign` 这个项目，此项目是可以通过编写接口的方式，让框架封装我们需要调用的 `HTTP` 请求，然后经过一系列的序列化反序列化从而取出我们需要的结果的一个开源框架。

那么上面我们已经将 `SpringMVC` 封装 `ProtocolBuffer` 数据输出，那么 `Feign` 也需要做相对应的配置使其支持。

### 配置Encoder和Decoder

`OpenFeign` 其实底层就是 `HttpClient` 去进行封装，那么要解码和编码数据就是通过修改 `Feign` 底层的解码器和编码器即可。

那么在项目中配置：

```java
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
```

### UserFeign

```java
@FeignClient(url = "http://localhost:8080/", name = "userFeign")
public interface UserFeign {

  @GetMapping
  User.Person test();

  @PostMapping
  void add(@RequestBody User.Person param);

}
```

### 测试用例

```java
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
```

## 项目说明

项目没有特别规范，随便写写

调用测试写在了 `com.microorder.test.Config#main` 

`feign` 测试放在 `test` 包下
