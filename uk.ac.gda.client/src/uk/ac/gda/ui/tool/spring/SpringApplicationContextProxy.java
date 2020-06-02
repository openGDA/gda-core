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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
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
	 *
	 * @param listener
	 */
	private static final void addApplicationListener(ApplicationListener<?> listener) {
		// This condition is necessary until the client call Spring "component-scan" by default (DAQ-2645)
		if (SpringApplicationContextProxy.configurableApplicationContext == null) {
			springApplicationDidNotScanClass();
			return;
		}
		SpringApplicationContextProxy.configurableApplicationContext.addApplicationListener(listener);
	}

	private static final void removeApplicationListener(ApplicationListener<?> listener) {
		ApplicationEventMulticaster aem = applicationContext.getBean(
				AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
		aem.removeApplicationListener(listener);
	}

	public static <T> T getBean(Class<T> bean) {
		return SpringApplicationContextProxy.applicationContext.getBean(bean);
	}

	public static <T> Optional<T> getOptionalBean(Class<T> bean) {
		try {
			return Optional.ofNullable(SpringApplicationContextProxy.applicationContext.getBean(bean));
		} catch (BeansException e) {
			return Optional.empty();
		}
	}

	/**
	 * Registers a {@code listener} and removes it when the related {@code composite} is disposed.
	 *
	 * @param widget the element publishing the dispose event which causes {@code listener} to be removed
	 * @param listener the application lister to
	 * @throws GDAClientException if the widget is {@code null} or {@link Widget#isDisposed()} returns {@code true}
	 */
	public static final void addDisposableApplicationListener(Widget widget, ApplicationListener<?> listener) throws GDAClientException {
		validateWidget(widget);
		SpringApplicationContextProxy.addApplicationListener(listener);
		widget.addDisposeListener(disposedEvent -> {
			if (Objects.equals(disposedEvent.getSource(), widget)) {
				SpringApplicationContextProxy.removeApplicationListener(listener);
			}
		});
	}

	private static void validateWidget(Widget widget) throws GDAClientException {
		if (Objects.isNull(widget)) {
			throw new GDAClientException("Cannot listen to a null widget");
		}
		if (widget.isDisposed()) {
			throw new GDAClientException("Cannot listen to a disposed widget");
		}
	}

	private static void springApplicationDidNotScanClass() {
		logger.warn(
				"The Spring Application may have not initialized uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy class");
	}

}
