package org.rt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * rt-kv 服务端启动入口。
 */
@SpringBootApplication
public class KvServerApplication {

    /**
     * 启动 Spring 容器并触发 Raft 服务引导。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(KvServerApplication.class, args);
    }
}