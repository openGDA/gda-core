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

import gda.factory.Findable;
import gda.factory.corba.util.CorbaUtils;
import gda.jython.accesscontrol.RbacUtils;
import gda.spring.BeanPostProcessorAdapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A {@link BeanPostProcessor} instance that proxies devices.
 */
public class RbacBeanPostProcessor extends BeanPostProcessorAdapter {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Findable) {
			Findable f = (Findable) bean;
			
			// Don't wrap the Findable if it is a cglib proxy
			if (RbacUtils.objectIsCglibProxy(f)) {
				return bean;
			}
			
			// Handle adapters, as done by AdapterFactory
			else if (CorbaUtils.isCorbaAdapter(f)) {
				bean = RbacUtils.buildProxy(f);
			}
			
			// Handle 'standard' objects, as done by ObjectFactory.buildProxies
			else {
				bean = RbacUtils.wrapFindableWithInterceptor(f);
			}
		}
		
		return bean;
	}
	
}
