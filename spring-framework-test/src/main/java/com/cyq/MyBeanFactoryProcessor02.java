package com.cyq;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-04-25 14:32:25
 */
public class MyBeanFactoryProcessor02 implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition user = beanFactory.getBeanDefinition("user");
        System.out.println("MyBeanFactoryProcessor02===================================" + user);
    }
}
