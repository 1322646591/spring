package com.cyq;

import javax.servlet.ServletContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

/**
 * @author chenyuqing
 * @email chenyq06@kaisagroup.com
 * @date 2021-05-17 17:45:41
 */
@Component
public class MySchedulerContextAware implements ServletContextAware {
	@Override
	public void setServletContext(ServletContext servletContext) {
		System.out.println("ServletContextAware");
	}
}
