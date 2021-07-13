package com.cyq;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

/**
 * 标题
 *
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-01-18 19:53:51
 */
@Component
public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment environment = ac.getEnvironment();
        environment.setActiveProfiles("dev");
        ac.register(Config.class);
        ac.refresh();
        User user = ac.getBean(User.class);
        Index index = ac.getBean(Index.class);
        View view = ac.getBean(View.class);

        user.testAop();
        index.testAop();

        if (user.index != null) {
            user.index.testAop();
        }

        if (user.name != null) {
            System.out.println("---------------" + user.name);
        }

        System.out.println("user-------->" + user.getPerson());
        System.out.println("index-------->" + index.person);
        System.out.println("index-------->" + index.getUser());
        System.out.println("view-------->" + view.person);

//        MyBeanFactoryProcessor01 myBeanFactoryProcessor01 = ac.getBean(MyBeanFactoryProcessor01.class);
//        System.out.println(myBeanFactoryProcessor01);
//        MyBeanFactoryProcessor02 myBeanFactoryProcessor02 = (MyBeanFactoryProcessor02)ac.getBean("1");
//        System.out.println(myBeanFactoryProcessor02);
    }

}
