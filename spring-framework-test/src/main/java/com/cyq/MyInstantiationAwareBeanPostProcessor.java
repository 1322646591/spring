package com.cyq;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.PriorityOrdered;

/**
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-04-26 17:19:20
 */
//@Component
public class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor, PriorityOrdered {
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		System.out.println("postProcessBeforeInstantiation");
        System.out.println(beanName);
        if (beanName.equals("user")) {
            User user = new User();
            user.setName("MyInstantiationAwareBeanPostProcessor");
            return user;
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
