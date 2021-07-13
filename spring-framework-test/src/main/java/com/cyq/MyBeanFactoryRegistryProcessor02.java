package com.cyq;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-04-25 14:32:25
 */
public class MyBeanFactoryRegistryProcessor02 implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.out.println("MyBeanFactoryRegistryProcessor02***************postProcessBeanDefinitionRegistry****************");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        System.out.println("MyBeanFactoryRegistryProcessor02*************postProcessBeanFactory******************");
    }

}
