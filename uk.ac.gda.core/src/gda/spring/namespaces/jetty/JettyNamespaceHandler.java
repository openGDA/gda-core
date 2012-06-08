/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring.namespaces.jetty;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * A {@link NamespaceHandler} that handles the GDA {@code jetty} namespace.
 */
public class JettyNamespaceHandler extends NamespaceHandlerSupport {
	
	@Override
	public void init() {
		registerBeanDefinitionParser("server", new JettyServerBeanDefinitionParser());
		registerBeanDefinitionParser("context", new JettyContextBeanDefinitionParser());
		registerBeanDefinitionParser("exporter", new JettyExporterBeanDefinitionParser());
	}
	
}
