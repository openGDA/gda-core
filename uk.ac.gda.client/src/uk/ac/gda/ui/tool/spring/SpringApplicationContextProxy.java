/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.ui.tool.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import uk.ac.gda.client.exception.GDAClientException;

/**
 * This class exposes Spring {@link ApplicationEventPublisher} to components as Views or Perspectives which are directly
 * managed by Eclipse and consequently cannot be initialised by Spring
 *
 * @author Maurizio Nagni
 */
@Component
public class SpringApplicationContextProxy implements ApplicationEventPublisherAware, ApplicationContextAware {

	private static ApplicationEventPublisher applicationPublisher;

	private static final Logger logger = LoggerFactory.getLogger(SpringApplicationContextProxy.class);

	@SuppressWarnings("unused")
	private static ApplicationContext applicationContext;

	private static ConfigurableApplicationContext configurableApplicationContext;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationPublisher) {
		SpringApplicationContextProxy.applicationPublisher = applicationPublisher;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		SpringApplicationContextProxy.applicationContext = applicationContext;
		if (ConfigurableApplicationContext.class.isAssignableFrom(applicationContext.getClass())) {
			configurableApplicationContext = ConfigurableApplicationContext.class.cast(applicationContext);
		}
	}

	public static final void publishEvent(ApplicationEvent event) {
		// This condition is necessary until the client call Spring "component-scan" by default (DAQ-2645)
		if (SpringApplicationContextProxy.applicationPublisher == null) {
			springApplicationDidNotScanClass();
			return;
		}
		SpringApplicationContextProxy.applicationPublisher.publishEvent(event);
	}

	/**
	 * Attached a listener to the application context in order to be notified when new events occur
	 * @param listener
	 * @throws GDAClientException
	 */
	public static final void addApplicationListener(ApplicationListener<?> listener) throws GDAClientException {
		// This condition is necessary until the client call Spring "component-scan" by default (DAQ-2645)
		if (SpringApplicationContextProxy.configurableApplicationContext == null) {
			springApplicationDidNotScanClass();
			return;
		}

//		if (configurableApplicationContext == null) {
//			throw new GDAClientException("This class has no valid ConfigurableApplicationContext instance");
//		}
		SpringApplicationContextProxy.configurableApplicationContext.addApplicationListener(listener);
	}

	public static <T> T getBean(Class<T> bean) {
		return SpringApplicationContextProxy.applicationContext.getBean(bean);
	}

	private static void springApplicationDidNotScanClass() {
		logger.warn("The Spring Application may have not initialized uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy class");
	}
}
