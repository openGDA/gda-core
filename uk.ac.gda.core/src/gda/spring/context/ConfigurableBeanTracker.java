/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.spring.context;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import gda.factory.Configurable;
import gda.factory.ConfigurableAware;
import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.spring.BeanPostProcessorAdapter;
import uk.ac.gda.remoting.client.RmiProxyMarker;

/**
 * BeanPostProcessor to track instantiation order of beans.
 * <p>
 * The order in which beans are defined is not always the order in which they need to be configured.
 * This allows Spring's 'depends-on' attributes to control the instantiation of beans and the beans
 * to be configured in the same order.
 */
public class ConfigurableBeanTracker extends BeanPostProcessorAdapter implements Iterable<Entry<String, Configurable>> {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurableBeanTracker.class);

	/** Ordered map of name to configurable - ordered by instantiation time */
	private Map<String, Configurable> order = new LinkedHashMap<>();
	/** Collection of all beans that should be aware of the Configuration process */
	private List<ConfigurableAware> aware = new ArrayList<>();
	/** Track when each bean is initialised, and if it should be aware of Configuration */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean instanceof Configurable) {
			order.put(beanName, (Configurable)bean);
		}
		if (bean instanceof ConfigurableAware) {
			aware.add((ConfigurableAware) bean);
		}
		return bean;
	}

	/**
	 * Configure all {@link Configurable} beans in the application context.
	 * Any errors are caught and, depending on configuration, possibly ignored
	 * @throws FactoryException if any configure method throws an exception and this server
	 *         is configured to rethrow configuration errors.
	 * @see FactoryBase#GDA_FACTORY_ALLOW_EXCEPTION_IN_CONFIGURE
	 */
	public void configureAll(boolean allowExceptions) throws FactoryException {

		// Stats about configuring
		int configuredCounter = 0;
		Collection<String> failures = new ArrayList<>();
		final Stopwatch configureStopwatch = Stopwatch.createStarted();

		preConfigure();

		for (Map.Entry<String, Configurable> entry : this) {
			String name = entry.getKey();
			Configurable obj = entry.getValue();

			if (!(obj instanceof RmiProxyMarker) && obj.isConfigureAtStartup()) {
				logger.info("Configuring {}", name);
				try {
					preConfigureBean(obj);
					obj.configure();
					postConfigureBean(obj);
					configuredCounter++;
				} catch (Exception e) {
					postConfigureBean(obj, e);
					if (!allowExceptions) {
						throw new FactoryException("Error in configure for " + name, e);
					}
					failures.add(name);
					logger.error("Error in configure for " + name, e);
				}
			} else {
				logger.info("Not configuring {}", name);
			}
		}

		postConfigure();

		// Analyse and log stats
		configureStopwatch.stop();
		logger.info("Finished configuring objects. Configured {} objects in {} seconds", configuredCounter,
				configureStopwatch.elapsed(SECONDS));
		logger.info("Failed to configure {} objects: {}", failures.size(), failures);
	}

	@Override
	public Iterator<Entry<String, Configurable>> iterator() {
		return order.entrySet().iterator();
	}
	/** Call preConfigure on all ConfigurableAware beans */
	private void preConfigure() {
		aware.forEach(safeExec(ConfigurableAware::preConfigure));
	}
	/** Call preConfigureBean on all ConfigurableAware beans */
	private void preConfigureBean(Configurable bean) {
		aware.forEach(safeExec(a -> a.preConfigureBean(bean)));
	}
	/** Call postConfigureBean on all ConfigurableAware beans */
	private void postConfigureBean(Configurable bean) {
		aware.forEach(safeExec(a -> a.postConfigureBean(bean, null)));
	}
	/** Call postConfigureBean on all ConfigurableAware beans after error */
	private void postConfigureBean(Configurable bean, Exception error) {
		aware.forEach(safeExec(a -> a.postConfigureBean(bean, error)));
	}
	/** Call postConfigure on all ConfigurableAware beans */
	private void postConfigure() {
		aware.forEach(safeExec(ConfigurableAware::postConfigure));
	}
	/** Run a ConfigurableAware method safely, preventing any exceptions interrupting configure process */
	private Consumer<ConfigurableAware> safeExec(Consumer<ConfigurableAware> method) {
		return listener -> {
			try {
				method.accept(listener);
			} catch (Exception e) {
				logger.error("Failed to run configurable aware task", e);
			}
		};
	}
}