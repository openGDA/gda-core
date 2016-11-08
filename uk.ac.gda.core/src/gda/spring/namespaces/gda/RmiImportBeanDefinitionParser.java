/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.spring.namespaces.gda;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.w3c.dom.Element;

import uk.ac.gda.remoting.client.GdaRmiProxyFactoryBean;

/**
 * Spring {@link BeanDefinitionParser} for the {@code rmi-import} element.
 */
public class RmiImportBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {

		final String id = element.getAttribute("id");
		final String serviceUrl = element.getAttribute("serviceUrl");
		final String serviceInterface = element.getAttribute("serviceInterface");

		boolean events = true;
		if (element.hasAttribute("events")) {
			final String eventsAttr = element.getAttribute("events");
			events = Boolean.valueOf(eventsAttr);
		}

		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setResource(parserContext.getReaderContext().getResource());
		beanDef.setBeanClass(events ? GdaRmiProxyFactoryBean.class : RmiProxyFactoryBean.class);
		beanDef.getPropertyValues().addPropertyValue("serviceUrl", serviceUrl);
		beanDef.getPropertyValues().addPropertyValue("serviceInterface", serviceInterface);
		beanDef.getPropertyValues().addPropertyValue("refreshStubOnConnectFailure", true);

		parserContext.getRegistry().registerBeanDefinition(id, beanDef);

		return null;
	}

}
