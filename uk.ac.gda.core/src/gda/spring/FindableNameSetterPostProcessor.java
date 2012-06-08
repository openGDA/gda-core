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

package gda.spring;

import gda.factory.Findable;
import gda.util.converters.JEPConverterHolder;
import gda.util.converters.LookupTableConverterHolder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.util.StringUtils;

/**
 * A Spring {@link BeanFactoryPostProcessor} that sets the name of all
 * {@link Findable}s to be the same as the Spring bean ID.
 */
public class FindableNameSetterPostProcessor extends BeanPostProcessorAdapter {

	// Must be *after* initialization, or an explicit 'name' property/value could revert the processor's changes
	// See http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/beans/factory/BeanFactory.html
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Findable) {
			Findable f = (Findable) bean;
			checkFindableName(beanName, f);
		}
		return bean;
	}
	
	protected static void checkFindableName(String beanName, Findable findable) {
		if (!beanName.equals(findable.getName())) {
		
			if (cannotSetObjectName(findable)) {
				throw new RuntimeException("Bean " + StringUtils.quote(beanName) + " has name " + StringUtils.quote(findable.getName()) + "; you need to set the name manually");
			}
			
			findable.setName(beanName);
			if (!beanName.equals(findable.getName())) {
				throw new RuntimeException("Name of bean " + StringUtils.quote(beanName) + " could not be set");
			}
		}
	}
	
	protected static boolean cannotSetObjectName(Findable findable) {
		return (findable instanceof JEPConverterHolder) || (findable instanceof LookupTableConverterHolder);
	}

}
