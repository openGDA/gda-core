/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.data.metadata;

import gda.data.ObservablePathProvider;
import gda.data.PathChanged;
import gda.data.PathConstructor;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.jython.InterfaceProvider;

@CorbaAdapterClass(DeviceAdapter.class)
@CorbaImplClass(DeviceImpl.class)
public class IntransparentLogFilePathProvider extends MetadataBlaster implements ObservablePathProvider {

	private String pathTemplate;

	public String getPathTemplate() {
		return pathTemplate;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		InterfaceProvider.getBatonStateProvider().addBatonChangedObserver(this);
	}
	
	public void setPathTemplate(String pathTemplate) {
		this.pathTemplate = pathTemplate;
	}

	@Override
	public String getPath() {
		return PathConstructor.createFromTemplate(pathTemplate);
	}
	
	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(this, new PathChanged(getPath()));
	}
}