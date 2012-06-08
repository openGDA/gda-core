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

package gda.factory;

/**
 * A server-side object creator that uses a Castor {@link XmlObjectCreator},
 * optionally applying access control.
 * 
 * @deprecated Adding Spring beans directly to the application context is the
 * preferred method for instantiating objects. (See also
 * {@link XmlObjectCreator}.)
 */
@Deprecated
public class ServerXmlObjectCreator extends XmlObjectCreator {

	protected boolean buildProxies;
	
	/**
	 * Controls whether access control proxies are created for objects in the
	 * factory.
	 * 
	 * @param buildProxies whether access control proxies should be created
	 */
	public void setBuildProxies(boolean buildProxies) {
		this.buildProxies = buildProxies;
	}

	@Override
	public ObjectFactory getFactory() throws FactoryException {
		ObjectFactory factory = super.getFactory();
		
		if (buildProxies) {
			factory.buildProxies();
		}
		
		return factory;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[xmlFile=" + xmlFile + ", mappingFile=" + mappingFile + ", buildProxies=" + buildProxies + "]";
	}
	
}
