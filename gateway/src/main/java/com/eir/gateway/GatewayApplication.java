package com.eir.gateway;

import com.eir.gateway.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.eir.gateway", "com.eir.common"})
@EnableDiscoveryClient
@EnableConfigurationProperties(SecurityProperties.class)
public class GatewayApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(GatewayApplication.class, args);


    }

}