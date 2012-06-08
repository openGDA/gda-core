/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.factory.corba.util;

import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class FactoryImplFactory extends ImplFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(FactoryImplFactory.class);

	private Factory factory;
	
	/**
	 * @param factory the factory delegate
	 * @param netService the NetService
	 */
	public FactoryImplFactory(Factory factory, NetService netService) {
		super(netService);
		this.factory = factory;
	}
	
	@Override
	protected List<Findable> getFindablesToMakeAvailable() {
		return factory.getFindables();
	}
	
	@Override
	protected String getNamespace() {
		return factory.getName();
	}
	
	public void configure() throws FactoryException {
		logger.info("Making objects in factory " + StringUtils.quote(factory.getName()) + " (of type " + factory.getClass().getSimpleName() + ") available through CORBA...");
		makeObjectsAvailable();
	}
	
	@Override
	public String toString() {
		return String.format("FactoryImplFactory[namespace=%s, factory=(%s, name=%s)]",
			getNamespace(), factory.getClass().getSimpleName(), factory.getName());
	}
}
