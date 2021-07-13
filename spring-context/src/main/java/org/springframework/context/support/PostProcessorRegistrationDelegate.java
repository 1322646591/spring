/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// WARNING: Although it may appear that the body of this method can be easily
		// refactored to avoid the use of multiple loops and multiple lists, the use
		// of multiple lists and multiple passes over the names of processors is
		// intentional. We must ensure that we honor the contracts for PriorityOrdered
		// and Ordered processors. Specifically, we must NOT cause processors to be
		// instantiated (via getBean() invocations) or registered in the ApplicationContext
		// in the wrong order.
		//
		// Before submitting a pull request (PR) to change this method, please review the
		// list of all declined PRs involving changes to PostProcessorRegistrationDelegate
		// to ensure that your proposal does not result in a breaking change:
		// https://github.com/spring-projects/spring-framework/issues?q=PostProcessorRegistrationDelegate+is%3Aclosed+label%3A%22status%3A+declined%22

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		//只针对BeanDefinitionRegistry类型的beanfactory才进行下面一系列的针对BeanFactoryPostProcessor的处理
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			/*1.首先对通过硬编码方式加入的BeanFactoryPostProcessor进行处理,首先对BeanDefinitionRegistryPostProcessor
			 * 类型的后置处理器优先处理,调用BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry方法,
			 * 并将其保存在registryProcessor集合当中,然后将普通的BeanFactoryPostProcessor保存在regularPostProcessors集合当中*/
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();


			/* 2.通过从传入的beanFactory中获取BeanDefinitionRegistryPostProcessor类型的bean的名称,对实现了PriorityOrdered
			 * 接口的,通过bean名称调用beanfactory的getBean方法获取到实例bean,将其加入到一个临时集合currentRegistryProcessors中,
			 * 并对其进行排序,然后再将临时集合当中的bean加入到registryProcessors当中,接下来,遍历调用临时集合currentRegistryProcessors
			 * 当中存储的BeanDefinitionRegistryPostProcessor类型的bean的postProcessBeanDefinitionRegistry方法,最后将临时集合
			 * currentRegistryProcessors清空*/
			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			currentRegistryProcessors.clear();

			// 3.第3步与第2步实现逻辑完全相同,只是由对实现了PriorityOrdered接口的bean进行处理换成了对实现Ordered接口的bean进行处理
			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
			currentRegistryProcessors.clear();

			/* 4.第4步中,spring首先也是从beanfactory容器中获取BeanDefinitionRegistryPostProcessor类型的bean的名称,(注意:在第2步第3
			 * 步中,spring将获取到的BeanDefinitionRegistryPostProcessor类型的bean的名称都存储在一个processedBeans集合中),通过遍历
			 * 获取到的bean名称,校验其是否存在于processedBeans集合中,如果不存在,则说明还没处理过,就还是通过beanfactory的getBean方法获取
			 * 到对应的bean实例,并将其存放在临时集合currentRegistryProcessors中,然后又是先进行排序,然后将currentRegistryProcessors集合
			 * 中的bean存放在registryProcessors集合当中,然后遍历currentRegistryProcessors调用其中BeanDefinitionRegistryPostProcessor
			 * 类型的bean的postProcessBeanDefinitionRegistry方法,最后将currentRegistryProcessors集合清空(注意:在第4步中,要注意一个点,
			 * 那就是每次只要有发现还有没有处理过的BeanDefinitionRegistryPostProcessor,就得重新循环刚才的过程,即又从beanfactory容器中再次获取
			 * BeanDefinitionRegistryPostProcessor类型的bean名称,再次判断还有有没有新产生的还没有处理过的BeanDefinitionRegistryPostProcessor,
			 * 之所以这样做的原因是,在调用postProcessBeanDefinitionRegistry方法的时候,有可能在方法里面又重新往beanfactory容器中注册了新的
			 * BeanDefinitionRegistryPostProcessor,所以要不断循环检查,直到没有出现未处理的BeanDefinitionRegistryPostProcessor为止)*/
			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				/**
				 * (注意:这里是对没有实现PriorityOrdered跟Ordered接口的BeanDefinitionRegistryPostProcessor进行方法调用,按道理来说,应该无需再进行
				 * 排序,其实,这里之所以还要再次进行排序,主要是考虑到在调用BeanDefinitionRegistryPostProcessor类型的bean的postProcessBeanDefinitionRegistry
				 * 方法的时候,可能会产生新的BeanDefinitionRegistryPostProcessor,新产生的BeanDefinitionRegistryPostProcessor可能有的实现了PriorityOrdered
				 * 或者Ordered接口,所以需要再次进行排序,然后再进行方法调用)
				 */
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
				currentRegistryProcessors.clear();
			}

			/**
			 * 5.将上面调用过postProcessBeanDefinitionRegistry方法的BeanDefinitionRegistryPostProcessor,进行遍历
			 * 并调用它们的postProcessBeanFactory方法
			 */
			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			/*6.将最开始获取到的通过硬编码方式添加到application容器内部的普通的BeanFactoryPostProcessor进行遍历,并调用它们的
		    * postProcessBeanFactory方法*/
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			/**
			 * 如果所传入的beanfactory不是BeanDefinitionRegistry类型的,则直接先调用通过硬编码加入容器的BeanFactoryPostProcessor的
			 * postProcessBeanFactory方法即可
			 */
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		/** 注意:在对于BeanDefinitionRegistryPostProcessor的处理中,最后对于没有实现PriorityOrdered跟Ordered接口的
		 * BeanDefinitionRegistryPostProcessor做处理的时候,只要发现还有未处理的BeanDefinitionRegistryPostProcessor,
		 * 又会重新去beanfactory中获取BeanDefinitionRegistryPostProcessor类型的bean,以防止在BeanDefinitionRegistryPostProcessor
		 * 调用postProcessBeanDefinitionRegistry中创建了新的BeanDefinitionRegistryPostProcessor,所以只要发现还有未处理的,就
		 * 得重新获取判断还有没有新的未处理的BeanDefinitionRegistryPostProcessor,所以在调用postProcessBeanDefinitionRegistry中
		 * 创建的新的BeanDefinitionRegistryPostProcessor是肯定会被做处理的;
		 * 但是,在对于普通的BeanFactoryPostProcessor的处理,只是简单的把实现了PriorityOrdered跟Ordered接口的和没有实现的分别放在不同集合
		 * 中,然后遍历调用它们的postProcessBeanFactory方法,所以,如果在调用postProcessBeanFactory方法的过程中有产生新的BeanFactoryPostProcessor,
		 * 不管新产生的是什么类型的BeanFactoryPostProcessor,都是不会被处理的
		 */

		//从传入的beanFactory中获取BeanFactoryPostProcessor类型的bean的名称
		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();//存储实现了PriorityOrdered接口的BeanFactoryPostProcessor
		List<String> orderedPostProcessorNames = new ArrayList<>();//存储实现了Ordered接口的BeanFactoryPostProcessor的beanName
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();//存储没有实现PriorityOrdered和Ordered接口的BeanFactoryPostProcessor的beanName
		for (String ppName : postProcessorNames) {
			/**
			 * processedBeans存放的是已经处理过的BeanDefinitionRegistryPostProcessor类型的后置处理器的beanName,如果所传入的beanfactory
			 * 是BeanDefinitionRegistry类型的,则会先针对BeanDefinitionRegistryPostProcessor类型的后置处理器进行方法调用处理,所以,如果
			 * processedBeans不为空且包含了最新从容器里面获取的BeanFactoryPostProcessor类型的beanName,说明这个beanName所对应的bean的类型是
			 * BeanDefinitionRegistryPostProcessor,而且在前面已经进行过方法调用了,所以就没必要再次进行处理,直接跳过
			 */
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}

			/**
			 * 先对实现了PriorityOrdered接口的BeanFactoryPostProcessor进行处理,如果beanfactory的getBean方法获取到bean,并
			 * 将其存放在priorityOrderedPostProcessors集合当中
			 */
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}

		    //对实现了Ordered接口的BeanFactoryPostProcessor进行处理,将其beanName存放在orderedPostProcessorNames集合中
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			//最后对没有实现PriorityOrdered和Ordered接口的BeanFactoryPostProcessor进行处理,将其beanName存放在nonOrderedPostProcessorNames集合中
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		/**
		 * 先对实现了PriorityOrdered接口的BeanFactoryPostProcessor进行处理,先是进行排序,然后遍历priorityOrderedPostProcessors调用
		 * BeanFactoryPostProcessor的postProcessBeanFactory方法
		 */
		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		/**
		 * 这里是遍历循环orderedPostProcessorNames集合,取出里面的beanName,然后跳过beanfactory的getBean方法获取到对应的bean,并加入
		 * 到orderedPostProcessors集合当中,先是进行排序,最后遍历orderedPostProcessors调用BeanFactoryPostProcessor的
		 * postProcessBeanFactory方法
		 */
		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		/**
		 * 这里是遍历循环nonOrderedPostProcessorNames集合,取出里面的beanName,然后跳过beanfactory的getBean方法获取到对应的bean,并加入到
		 * nonOrderedPostProcessors集合当中,最后遍历nonOrderedPostProcessors调用BeanFactoryPostProcessor的postProcessBeanFactory方法
		 * (注意:这里因为没有实现排序接口所以无需进行排序)
		 */
		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// WARNING: Although it may appear that the body of this method can be easily
		// refactored to avoid the use of multiple loops and multiple lists, the use
		// of multiple lists and multiple passes over the names of processors is
		// intentional. We must ensure that we honor the contracts for PriorityOrdered
		// and Ordered processors. Specifically, we must NOT cause processors to be
		// instantiated (via getBean() invocations) or registered in the ApplicationContext
		// in the wrong order.
		//
		// Before submitting a pull request (PR) to change this method, please review the
		// list of all declined PRs involving changes to PostProcessorRegistrationDelegate
		// to ensure that your proposal does not result in a breaking change:
		// https://github.com/spring-projects/spring-framework/issues?q=PostProcessorRegistrationDelegate+is%3Aclosed+label%3A%22status%3A+declined%22

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry, ApplicationStartup applicationStartup) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanDefRegistry = applicationStartup.start("spring.context.beandef-registry.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanDefinitionRegistry(registry);
			postProcessBeanDefRegistry.end();
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanFactory = beanFactory.getApplicationStartup().start("spring.context.bean-factory.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanFactory(beanFactory);
			postProcessBeanFactory.end();
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		if (beanFactory instanceof AbstractBeanFactory) {
			// Bulk addition is more efficient against our CopyOnWriteArrayList there
			((AbstractBeanFactory) beanFactory).addBeanPostProcessors(postProcessors);
		}
		else {
			for (BeanPostProcessor postProcessor : postProcessors) {
				beanFactory.addBeanPostProcessor(postProcessor);
			}
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
