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

package uk.ac.gda.core.tool.spring;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
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

/**
 * This class exposes Spring {@link ApplicationEventPublisher} to components as Views or Perspectives which are directly
 * managed by Eclipse and consequently cannot be initialised by Spring
 *
 * @author Maurizio Nagni
 */
@Component
public class SpringApplicationContextFacade implements ApplicationEventPublisherAware, ApplicationContextAware {

	private static final ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
	private static final WeakHashMap<Reference<?>, List<ApplicationListener<?>>> weakMap = new WeakHashMap<>();

	private static ApplicationEventPublisher applicationPublisher;

	private static final Logger logger = LoggerFactory.getLogger(SpringApplicationContextFacade.class);

	private static ApplicationContext applicationContext;

	private static ConfigurableApplicationContext configurableApplicationContext;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationPublisher) {
		SpringApplicationContextFacade.applicationPublisher = applicationPublisher;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		SpringApplicationContextFacade.applicationContext = applicationContext;
		if (ConfigurableApplicationContext.class.isAssignableFrom(applicationContext.getClass())) {
			configurableApplicationContext = ConfigurableApplicationContext.class.cast(applicationContext);
		}
	}

	public static final void publishEvent(ApplicationEvent event) {
		// This condition is necessary until the client call Spring "component-scan" by default (DAQ-2645)
		if (SpringApplicationContextFacade.applicationPublisher == null) {
			springApplicationDidNotScanClass();
			return;
		}
		// Cleanup phantom references
		cleanUpListeners();
		applicationPublisher.publishEvent(event);
	}

	/**
	 * Attached a listener to the application context in order to be notified when new events occur.
	 *
	 * @param listener
	 */
	public static final void addApplicationListener(ApplicationListener<?> listener) {
		configurableApplicationContext.addApplicationListener(listener);
	}


	/**
	 * @param listener
	 */
	public static final void removeApplicationListener(ApplicationListener<?> listener) {
		ApplicationEventMulticaster aem = applicationContext.getBean(
				AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
		aem.removeApplicationListener(listener);
	}

	/**
	 * Return the bean instance that uniquely matches the given object type, if any.
	 *
	 * @param requiredType
	 *            type the bean must match; can be an interface or superclass. {@code null} is disallowed.
	 *            <p>
	 *            This method goes into {@link ListableBeanFactory} by-type lookup territory but may also be translated
	 *            into a conventional by-name lookup based on the name of the given type. For more extensive retrieval
	 *            operations across sets of beans, use {@link ListableBeanFactory} and/or {@link BeanFactoryUtils}.
	 * @return an instance of the single bean matching the required type
	 * @throws NoSuchBeanDefinitionException
	 *             if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException
	 *             if more than one bean of the given type was found
	 */
	public static <T> T getBean(Class<T> requiredType) {
		return applicationContext.getBean(requiredType);
	}

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>
	 * Allows for specifying explicit constructor arguments / factory method arguments, overriding the specified default
	 * arguments (if any) in the bean definition.
	 *
	 * @param name
	 *            the name of the bean to retrieve
	 * @param args
	 *            arguments to use if creating a prototype using explicit arguments to a static factory method. It is
	 *            invalid to use a non-null args value in any other case.
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException
	 *             if there is no such bean definition
	 * @throws BeanDefinitionStoreException
	 *             if arguments have been given but the affected bean isn't a prototype
	 */
	public static Object getBean(String name, Object... args) {
		return applicationContext.getBean(name, args);
	}

	public static <T> Optional<T> getOptionalBean(Class<T> bean) {
		try {
			return Optional.ofNullable(applicationContext.getBean(bean));
		} catch (BeansException e) {
			return Optional.empty();
		}
	}

	/**
	 * Registers a {@code listener} and removes it when the related {@code object} became a {@link PhantomReference}.
	 *
	 * @param object
	 *            the element publishing the dispose event which causes {@code listener} to be removed
	 * @param listener
	 *            the application lister to
	 */
	public static final void addDisposableApplicationListener(Object object, ApplicationListener<?> listener) {
		SpringApplicationContextFacade.addApplicationListener(listener);
		createPhantomReference(object, listener);
	}

	private static void springApplicationDidNotScanClass() {
		logger.warn(
				"The Spring Application may have not initialized uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy class");
	}

	/**
	 * Creates a {@link PhantomReference} with the given {@code object}. The {@code reference} and the {@code listener}
	 * are stored in {@link #weakMap} for later cleanup
	 *
	 * @param object
	 * @param listener
	 *
	 * @see SpringApplicationContextFacade#cleanUpListeners()
	 */
	private static void createPhantomReference(Object object, ApplicationListener<?> listener) {
		Reference<?> pr = new PhantomReference<>(object, refQueue);
		weakMap.putIfAbsent(pr, new ArrayList<ApplicationListener<?>>());
		weakMap.get(pr).add(listener);
	}

	/**
	 * Removes the {@link ApplicationListener}s associated with the eligible {@link PhantomReference}.
	 *
	 */
	private static void cleanUpListeners() {
		// Is there any reference in the queue?
		Optional.ofNullable(refQueue.poll()).ifPresent(r -> {
			// Has the reference any assocated list of listeners?
			Optional.ofNullable(weakMap.remove(r))
					.ifPresent(l -> l.forEach(SpringApplicationContextFacade::removeApplicationListener));
			// Recurse until queue is empty
			cleanUpListeners();
		});
	}
}