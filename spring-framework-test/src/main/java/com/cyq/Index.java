package com.cyq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 标题
 *
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-01-19 09:51:27
 */
@Profile("dev")
@Component("index")
public class Index {

//    @Autowired
    Person person;

    @Autowired
    User user;

	public void setPerson(Person person) {
		this.person = person;
	}

	public User getUser() {
		return user;
	}

	public Index() {
        System.out.println("index被创建");
    }

    public void testAop() {
        System.out.println("test aop");
    }
}
