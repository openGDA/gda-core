/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
import org.w3c.dom.Element;

/**
 * Spring {@link BeanDefinitionParser} for the {@code "rbac"} element. Translates the element into a definition of a
 * {@link RbacBeanPostProcessor} instance.
 */
public class RbacBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
		AbstractBeanDefinition beanDef = createRbacBeanPostProcessorBeanDefinition();
		beanDef.setResource(parserContext.getReaderContext().getResource());

		String beanName = parserContext.getReaderContext().generateBeanName(beanDef);
		parserContext.getRegistry().registerBeanDefinition(beanName, beanDef);

		return beanDef;
	}

	private AbstractBeanDefinition createRbacBeanPostProcessorBeanDefinition() {
		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(RbacBeanPostProcessor.class);
		return beanDef;
	}

}
