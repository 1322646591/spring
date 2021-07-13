package com.cyq;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-04-25 14:32:25
 */
@Component
public class MyBeanFactoryRegistryProcessor01 implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinitionBuilder builder02 = BeanDefinitionBuilder.rootBeanDefinition(MyBeanFactoryRegistryProcessor02.class);
        registry.registerBeanDefinition("2", builder02.getBeanDefinition());

        BeanDefinitionBuilder builder03 = BeanDefinitionBuilder.rootBeanDefinition(MyBeanFactoryRegistryProcessor03.class);
        registry.registerBeanDefinition("3", builder03.getBeanDefinition());
        System.out.println("MyBeanFactoryRegistryProcessor01*************postProcessBeanDefinitionRegistry******************");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
//        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(MyBeanFactoryRegistryProcessor02.class);
//        registry.registerBeanDefinition("2", builder.getBeanDefinition());
//        System.out.println("MyBeanFactoryRegistryProcessor01*************postProcessBeanFactory******************");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("------------------------------------");
    }
}
