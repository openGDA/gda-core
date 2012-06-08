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

import gda.factory.corba.util.NetService;

import org.springframework.beans.factory.FactoryBean;

/**
 * A Spring {@link FactoryBean} that returns the singleton {@link NetService}.
 */
public class NetServiceFactoryBean implements FactoryBean<NetService> {

	@Override
	public Class<?> getObjectType() {
		return NetService.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public NetService getObject() throws Exception {
		return NetService.getInstance();
	}

}
