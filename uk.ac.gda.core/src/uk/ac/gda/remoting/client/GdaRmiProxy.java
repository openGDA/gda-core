/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.remoting.client;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import gda.factory.FactoryException;

/**
 * This is a convenience class for importing remote objects over RMI into the client side Spring context. It works using
 * the {@link RmiProxyFactory} to obtain the objects.
 * <p>
 * An example usage in Spring to import an object called 'commandQueueProcessor'
 *
 * <pre>
 *  {@code <bean id="commandQueueProcessor" class="uk.ac.gda.remoting.client.GdaRmiProxy" />}
 * </pre>
 *
 * This object can then be set on other beans e.g.
 *
 * <pre>
 *  {@code <bean class="gda.rcp.util.OSGIServiceRegister">
 * 	<property name="class" value="gda.commandqueue.Processor" />
 * 	<property name="service" ref="commandQueueProcessor" />
 * </bean>}
 * </pre>
 *
 * @since GDA 9.8
 * @author James Mudd
 */
public class GdaRmiProxy implements ApplicationContextAware, BeanNameAware, FactoryBean<Object>, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(GdaRmiProxy.class);

	private ApplicationContext applicationContext;
	private String name;
	private Object object;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws FactoryException {
		// Check state, should be ensured by Spring
		Objects.requireNonNull(applicationContext, "applicationContext not set");
		Objects.requireNonNull(name, "name not set");

		// Get the actual proxy object
		object = getRmiProxyFactory().getFindable(name);
		// If it can't be imported throw
		Objects.requireNonNull(object,
				String.format("Could not import '%s', are you sure its exported from the server?", name));

		logger.debug("Imported '{}' (Proxy={})", name, object);
	}

	/**
	 * Gets the rmiProxyFactory from the context, and ensures only one is present. It also ensures the rmiProxyFactory
	 * is configured before returning it.
	 *
	 * @return the rmiProxyFactory
	 * @throws FactoryException
	 *             If configuring the rmiProxyFactory fails
	 */
	private RmiProxyFactory getRmiProxyFactory() throws FactoryException {
		final Map<String, RmiProxyFactory> rmiProxyFactories = applicationContext.getBeansOfType(RmiProxyFactory.class);
		if (rmiProxyFactories.size() != 1) {
			throw new IllegalStateException("No RmiProxyFactory is avaliable. Is one created in your config?");
		}
		// Only one thing in the map and we don't care about the id.
		final RmiProxyFactory rmiProxyFactory = rmiProxyFactories.values().iterator().next();
		// Ensure it's configured
		rmiProxyFactory.configure();
		logger.trace("Got rmiProxyFactory '{}'", rmiProxyFactory);
		return rmiProxyFactory;
	}

	@Override
	public Object getObject() throws Exception {
		if (object == null) {
			// Throw as specified by the doc
			throw new FactoryBeanNotInitializedException();
		}
		return object;
	}

	@Override
	public Class<?> getObjectType() {
		if (object == null) {
			// Return null when unknown as specified by the doc
			return null; // This will happen when trying to get the rmiProxyFactory
		}
		return object.getClass();
	}

	@Override
	public boolean isSingleton() {
		return false; // May have lots of instances of this class for importing multiple objects
	}

	@Override
	public String toString() {
		return "GdaRmiProxy [name=" + name + ", object=" + object + "]";
	}

}
