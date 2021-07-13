package com.cyq;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 标题
 *
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-01-18 19:57:05
 */
@Profile("dev")
@Service("user")
public class User {

    @Autowired
    private Person person;

    @Autowired
    Index index;

    @Autowired
    Post post01;

    String name;

    public void setName(String name) {
        this.name = name;
    }

	public Person getPerson() {
		System.out.println("取person: " + this.person);
		return this.person;
	}

	public User() {
        System.out.println("user被创建");
    }

    @PostConstruct
    public void born() {
        System.out.println("@PostConstruct");
    }

    public void testAop() {
        System.out.println("test aop");
    }
}
