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

package gda.data.metadata;

import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.MetadataEntry;
import gda.data.metadata.StoredMetadataEntry;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SubdirectoryMetadataEntry extends StoredMetadataEntry implements IObserver, Configurable {

	Map<String, String> visit2subdir = new HashMap<String, String>();
	String currentvisit = "";

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			IMetadataEntry visit = null;
			ArrayList<IMetadataEntry> metadataEntries = GDAMetadataProvider.getInstance().getMetadataEntries();
			for (Iterator<IMetadataEntry> iterator = metadataEntries.iterator(); iterator.hasNext();) {
				IMetadataEntry metadataEntry = iterator.next();
				if (metadataEntry.getName().equals("visit")) {
					visit = metadataEntry;
					break;
				}
			}
			if (visit != null)
				visit.addIObserver(this);
		} catch (DeviceException e) {
			throw new FactoryException("error locating visit", e);
		}
	}

	@Override
	public String getName() {
		return "subdirectory";
	}

	@Override
	public void setValue(String value) {
		String oldValue = getMetadataValue();
		value = sanitze(value);
		super.setValue(value);
		String path = PathConstructor.createFromDefaultProperty();
		File file = new File(path);
		if (file.isDirectory())
			return;
		if (file.exists() || !file.mkdirs()) {
			super.setValue(oldValue);
			return;
		}
	}

	public static String sanitze(String in) {
		return in.replaceAll("[^0-9a-zA-Z,./_-]", "");
	}

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof MetadataEntry) {
			if (currentvisit.equals(arg.toString())) {
				return;
			}
			visit2subdir.put(currentvisit, getMetadataValue());
			currentvisit = arg.toString();
			super.setValue("");
			if (visit2subdir.containsKey(currentvisit))
				setValue(visit2subdir.get(currentvisit));
		}
	}
}