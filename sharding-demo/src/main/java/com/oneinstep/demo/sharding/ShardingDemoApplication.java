package com.oneinstep.demo.sharding;

import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import com.oneinstep.demo.sharding.generator.SnowflakeIdGenerator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 扫描 Mapper 接口
@MapperScan("com.oneinstep.demo.sharding.mapper")
public class ShardingDemoApplication {

    public static void main(String[] args) {
        KeyGeneratorFactory.register("snowflakeIdGenerator", new SnowflakeIdGenerator());
        SpringApplication.run(ShardingDemoApplication.class, args);
    }

}
