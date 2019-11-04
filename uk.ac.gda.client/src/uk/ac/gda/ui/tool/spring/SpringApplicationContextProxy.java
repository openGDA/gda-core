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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * This class exposes Spring {@link ApplicationEventPublisher} to components as Views or Perspectives which are directly
 * managed by Eclipse and consequently cannot be initialized by Spring
 *
 * @author Maurizio Nagni
 */
@Component
public class SpringApplicationContextProxy implements ApplicationEventPublisherAware, ApplicationContextAware {

	private static ApplicationEventPublisher applicationPublisher;
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationPublisher) {
		SpringApplicationContextProxy.applicationPublisher = applicationPublisher;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		SpringApplicationContextProxy.applicationContext = applicationContext;
	}

	public static final void publishEvent(ApplicationEvent event) {
		SpringApplicationContextProxy.applicationPublisher.publishEvent(event);
	}

	public static final <T> T getBean(String name, Class<T> clazz) {
		return SpringApplicationContextProxy.applicationContext.getBean(name, clazz);
	}

	public static final <T> T getBean(Class<T> clazz) {
		return SpringApplicationContextProxy.applicationContext.getBean(clazz);
	}
}
