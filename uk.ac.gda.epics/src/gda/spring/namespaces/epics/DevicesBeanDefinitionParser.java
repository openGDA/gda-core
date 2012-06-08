/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.spring.namespaces.epics;

import gda.device.epicsdevice.FindableEpicsDevice;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;

import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring {@link BeanDefinitionParser} for the {@code devices} element.
 */
public class DevicesBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
		boolean simulated = false;
		
		try {
			List<String> deviceNames = GDAEpicsInterfaceReader.getAllDeviceNames();
			for (String devName : deviceNames) {
				AbstractBeanDefinition beanDef = new GenericBeanDefinition();
				beanDef.setBeanClass(FindableEpicsDevice.class);
				String findableName = devName.replace(".", "_");
				beanDef.getPropertyValues().addPropertyValue("name", findableName);
				beanDef.getPropertyValues().addPropertyValue("deviceName", devName);
				if (simulated) {
					beanDef.getPropertyValues().addPropertyValue("dummy", true);
				}
				parserContext.getRegistry().registerBeanDefinition(findableName, beanDef);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return null;
	}

}
