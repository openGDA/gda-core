/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import gda.spring.BeansOfType;

public class BeansOfTypeParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String type = element.getAttribute("type");
		GenericBeanDefinition beansOfType = new GenericBeanDefinition();
		beansOfType.setBeanClass(BeansOfType.class);
		var props = new MutablePropertyValues();
		props.add("type", type);
		beansOfType.setPropertyValues(props);
		return beansOfType;
	}

}
