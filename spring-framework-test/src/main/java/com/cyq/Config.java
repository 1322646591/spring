package com.cyq;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 标题
 *
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-01-18 19:56:12
 */
@ComponentScan
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Configuration
public class Config {

	public Config() {
		System.out.println("config被创建");
	}

//	@Bean
//	public Index index() {
//		return new Index();
//	}
//
//	@Bean
//	public User user() {
//		return new User();
//	}
}
