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

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * A {@link ContextLoader} that creates an {@link WebApplicationContext} that
 * has the specified {@link BeanFactory} as its parent and simply delegates to
 * that factory.
 */
public class DelegatingContextLoader extends ContextLoader {

	protected BeanFactory beanFactory;
	
	/**
	 * Creates a {@link DelegatingContextLoader} that will create an empty
	 * {@link WebApplicationContext} with the specified bean factory as its
	 * parent.
	 * 
	 * @param beanFactory the parent bean factory
	 */
	public DelegatingContextLoader(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent) throws BeansException {
		return new GenericWebApplicationContext((DefaultListableBeanFactory) beanFactory);
	}

}
