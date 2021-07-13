package com.cyq;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-04-26 17:41:19
 */
@Component
@Scope("singleton")
public class Person {

    public Person() {
        System.out.println("Person被创建");
    }

}
