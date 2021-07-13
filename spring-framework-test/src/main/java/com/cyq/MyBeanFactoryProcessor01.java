package com.cyq;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-04-25 14:32:25
 */
//@Component
public class MyBeanFactoryProcessor01 implements BeanFactoryPostProcessor, BeanFactoryAware {
    @Autowired
    private DefaultListableBeanFactory defaultListableBeanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(MyBeanFactoryProcessor02.class);
        defaultListableBeanFactory.registerBeanDefinition("1",builder.getBeanDefinition());
        BeanDefinition user = beanFactory.getBeanDefinition("user");
        System.out.println("MyBeanFactoryProcessor01===================================" + user);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}
