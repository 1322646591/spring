package com.cyq;

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
@Service("view")
public class View {

    @Autowired
    Person person;

    public View() {
        System.out.println("View被创建");
    }

}
