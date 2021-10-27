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

package gda.spring.device;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SpringMonitorDefinitionParser implements BeanDefinitionParser {
	private static final Logger logger = LoggerFactory.getLogger(SpringMonitorDefinitionParser.class);
	/** Base of property used to determine default monitor implementation - mode is added */
	public static final String DEFAULT_MONITOR_CLASS_PROPERTY_BASE = "gda.spring.device.default.monitor.";

	/** Attribute name for the name of created beans */
	private static final String ID_ATTRIBUTE = "id";
	/** Attribute name for the class of created beans */
	private static final String CLASS_ATTRIBUTE = "class";

	/**
	 * Keys which are expected to be defined in the element but should not be set
	 * as properties on the created bean.
	 */
	private static final Set<String> NON_MODE_KEYS = Set.of(ID_ATTRIBUTE, CLASS_ATTRIBUTE);

	/** For the common modes provide fallback classes for when properties aren't set */
	private static final Map<String, String> FALLBACK_MONITOR_CLASSES = Map.of(
			"live", "gda.device.monitor.EpicsMonitor",
			"dummy", "gda.device.monitor.DummyMonitor"
	);

	/** Class provider that uses the current mode to determine the class for a bean */
	private static final BeanClassProvider CLASS_PROVIDER = new BeanClassProvider(DEFAULT_MONITOR_CLASS_PROPERTY_BASE, FALLBACK_MONITOR_CLASSES);

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		var monitor = new GenericBeanDefinition();

		var attrs = BeanAttributes.from(element);
		monitor.setBeanClassName(CLASS_PROVIDER.forCurrentMode(attrs));

		monitor.setPropertyValues(attrs.modeProperties()
				.filter(e -> !NON_MODE_KEYS.contains(e.getKey()))
				.map(e -> new PropertyValue(e.getKey(), e.getValue()))
				.collect(MutablePropertyValues::new,
						MutablePropertyValues::addPropertyValue,
						MutablePropertyValues::addPropertyValues));

		var resource = parserContext.getReaderContext().getResource();
		if (resource != null) monitor.setResource(resource);

		String id = attrs.get(ID_ATTRIBUTE)
				.orElseThrow(() -> new IllegalStateException("No id found for monitor bean"));
		logger.debug("Creating monitor instance for {}", id);
		parserContext.getRegistry().registerBeanDefinition(id, monitor);
		return null;
	}
}
