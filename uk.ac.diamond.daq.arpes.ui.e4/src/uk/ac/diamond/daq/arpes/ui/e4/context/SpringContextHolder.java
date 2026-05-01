package uk.ac.diamond.daq.arpes.ui.e4.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHolder implements ApplicationContextAware{
	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext ctx) {
		context = ctx;
	}

	public static ApplicationContext getContext() {
		return context;
	}
}
