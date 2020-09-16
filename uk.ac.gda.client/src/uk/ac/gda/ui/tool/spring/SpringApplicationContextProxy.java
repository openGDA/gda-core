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

import java.lang.ref.PhantomReference;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.swt.widgets.Widget;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * This class uses the {@link SpringApplicationContextFacade} to implements client specific methods.
 * These methods are not implementable by the {@code SpringApplicationContextFacade} as its package is supposed to not contain client libraries.
 *
 * @author Maurizio Nagni
 */
public class SpringApplicationContextProxy {

	private SpringApplicationContextProxy() {}

	/**
	 * @param event
	 * @deprecated use {@link SpringApplicationContextFacade#publishEvent(ApplicationEvent)}
	 */
	@Deprecated
	public static final void publishEvent(ApplicationEvent event) {
		SpringApplicationContextFacade.publishEvent(event);
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
	 * @deprecated use {@link SpringApplicationContextFacade#getBean(Class)}
	 */
	@Deprecated
	public static <T> T getBean(Class<T> requiredType) {
		return SpringApplicationContextFacade.getBean(requiredType);
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
	 * @deprecated Use {@link SpringApplicationContextFacade#getBean(String, Object...)}
	 */
	@Deprecated
	public static Object getBean(String name, Object... args) {
		return SpringApplicationContextFacade.getBean(name, args);
	}


	/**
	 * @param bean
	 * @return an optional instance
	 * @deprecated Use {@link SpringApplicationContextFacade#getOptionalBean(Class)}
	 */
	@Deprecated
	public static <T> Optional<T> getOptionalBean(Class<T> bean) {
		return SpringApplicationContextFacade.getOptionalBean(bean);
	}

	/**
	 * Registers a {@code listener} and removes it when the related {@code composite} is disposed.
	 *
	 * @param widget
	 *            the element publishing the dispose event which causes {@code listener} to be removed
	 * @param listener
	 *            the application lister to
	 * @throws GDAClientException
	 *             if the widget is {@code null} or {@link Widget#isDisposed()} returns {@code true}
	 */
	public static final void addDisposableApplicationListener(Widget widget, ApplicationListener<?> listener)
			throws GDAClientException {
		validateWidget(widget);
		SpringApplicationContextFacade.addApplicationListener(listener);
		widget.addDisposeListener(disposedEvent -> {
			if (Objects.equals(disposedEvent.getSource(), widget)) {
				SpringApplicationContextFacade.removeApplicationListener(listener);
			}
		});
	}

	/**
	 * Registers a {@code listener} and removes it when the related {@code object} became a {@link PhantomReference}.
	 *
	 * @param object
	 *            the element publishing the dispose event which causes {@code listener} to be removed
	 * @param listener
	 *            the application lister to
	 * @deprecated use {@link SpringApplicationContextFacade#addDisposableApplicationListener(Object, ApplicationListener)}
	 */
	@Deprecated
	public static final void addDisposableApplicationListener(Object object, ApplicationListener<?> listener) {
		SpringApplicationContextFacade.addDisposableApplicationListener(object, listener);
	}

	private static void validateWidget(Widget widget) throws GDAClientException {
		if (Objects.isNull(widget)) {
			throw new GDAClientException("Cannot listen to a null widget");
		}
		if (widget.isDisposed()) {
			throw new GDAClientException("Cannot listen to a disposed widget");
		}
	}
}