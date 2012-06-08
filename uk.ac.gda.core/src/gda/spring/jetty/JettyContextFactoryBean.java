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

package gda.spring.jetty;

import java.util.Map;
import java.util.Map.Entry;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A {@link FactoryBean} that can be used to create a fully-configured Jetty
 * context, by taking the Jetty context itself and a group of servlets and
 * adding the servlets to the context. This allows the context to be created
 * using dependency injection.
 * 
 * <p>Beans created using this factory can typically be anonymous: the context
 * which has to be supplied as a property is attached to the Jetty server
 * when it is created, so does not need to be referenced elsewhere in the Spring
 * beans file.
 */
public class JettyContextFactoryBean implements FactoryBean<Context>, InitializingBean {

	private Context context;
	
	private Map<String, ServletHolder> servlets;
	
	/**
	 * Sets the Jetty context, to which servlets will be added.
	 * 
	 * @param context the Jetty context
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Sets the servlets that will be added to the context.
	 * 
	 * @param servlets the servlets
	 */
	public void setServlets(Map<String, ServletHolder> servlets) {
		this.servlets = servlets;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// add servlets to the context
		for (Entry<String, ServletHolder> entry : servlets.entrySet()) {
			context.addServlet(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public Context getObject() throws Exception {
		return context;
	}

	@Override
	public Class<?> getObjectType() {
		return Context.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
