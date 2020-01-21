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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;

public class SubdirectoryMetadataEntry extends PersistantMetadataEntry {

	private Map<String, String> visit2subdir = new HashMap<>();
	private String currentvisit = "";
	private String defaultSubdirectory = "";
	private static final Logger logger = LoggerFactory.getLogger(SubdirectoryMetadataEntry.class);

	public SubdirectoryMetadataEntry() {
		super("subdirectory");
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		super.configure();
		try {
			GDAMetadataProvider.getInstance().getMetadataEntries().stream()
					.filter(entry -> entry.getName().equals("visit"))
					.findFirst()
					.ifPresent(entry -> entry.addIObserver(this::update));
		} catch (DeviceException e) {
			setConfigured(false);
			throw new FactoryException("error locating visit", e);
		}
		setConfigured(true);
	}

	@Override
	public void setValue(String value) throws Exception {
		final String oldValue = getMetadataValue();
		super.setValue(sanitize(value));
		final String path = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		final File file = new File(path);
		if (file.isDirectory()) {
			return;
		}
		if (file.exists() || !file.mkdirs()) {
			super.setValue(oldValue);
		}
	}

	private static String sanitize(String in) {
		return in.replaceAll("[^0-9a-zA-Z,./_-]", "");
	}

	private void update(Object source, Object arg) {
		if (source instanceof MetadataEntry) {
			if (currentvisit.equals(arg.toString())) {
				return;
			}
			visit2subdir.put(currentvisit, getMetadataValue());
			currentvisit = arg.toString();
			try {
				super.setValue("");
				if (visit2subdir.containsKey(currentvisit)) {
					setValue(visit2subdir.get(currentvisit));
				} else {
					setValue(getDefaultSubdirectory());
				}
			} catch (Exception e) {
				logger.error("cannot set or persist directory setting", e);
			}
		}
	}

	public String getDefaultSubdirectory() {
		return defaultSubdirectory;
	}

	public void setDefaultSubdirectory(String defaultSubdirectory) {
		this.defaultSubdirectory = defaultSubdirectory;
	}
}
