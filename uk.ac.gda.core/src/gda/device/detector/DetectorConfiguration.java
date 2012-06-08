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

package gda.device.detector;

import gda.factory.Configurable;
import gda.factory.Findable;

import java.io.File;
import java.net.URL;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Class for extending to pass beans to detectors.
 */
public abstract class DetectorConfiguration implements Configurable, Findable {

	protected String name;

	protected abstract Class<? extends Object> getBeanClass();

	protected abstract URL getMappingURL();

	protected abstract URL getSchemaURL();

	protected Object getBean(final String dir, final Object var) throws Exception {

		if (var != null && getBeanClass().isInstance(var)){
			return var;
		}
		
		String path;
		if (var != null){
			 path = dir + var;
		} else {
			path = dir;
		}
		
		if (!path.endsWith(".xml")){
			path = path + ".xml";
		}

		return XMLHelpers.createFromXML(getMappingURL(), getBeanClass(), getSchemaURL(), new File(path));

	}

	/**
	 * Writes the bean to file and deals with any synchronisation if needed.
	 * 
	 * @param bean
	 * @param templateFile
	 * @throws Exception
	 */
	protected void saveBeanToTemplate(final Object bean, final File templateFile) throws Exception {
		XMLHelpers.writeToXML(getMappingURL(), bean, templateFile);
	}

	/**
	 * @return Returns the name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

}
