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

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;

import gda.factory.Finder;

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
 *  {@code <bean class="gda.util.osgi.OSGiServiceRegister">
 * 	<property name="class" value="gda.commandqueue.Processor" />
 * 	<property name="service" ref="commandQueueProcessor" />
 * </bean>}
 * </pre>
 *
 * @since GDA 9.8
 * @author James Mudd
 */
public class GdaRmiProxy implements BeanNameAware, FactoryBean<Object>, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(GdaRmiProxy.class);

	private String name;
	private Object object;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Check state, should be ensured by Spring
		Objects.requireNonNull(name, "name not set");

		// Get the actual proxy object
		object = Finder.getInstance().find(name);
		// If it can't be imported throw
		Objects.requireNonNull(object,
				String.format("Could not import '%s' - are you sure it is exported from the server?", name));

		logger.debug("Imported '{}' (Proxy={})", name, object);
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
