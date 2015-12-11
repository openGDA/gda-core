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

package gda.data.scan.datawriter;

import gda.configuration.properties.LocalProperties;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class DefaultDataWriterFactory implements DataWriterFactory, Findable, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(DefaultDataWriterFactory.class);


	private String name;
	private Map<String, IDataWriterExtender> dataWriterExtenders = new LinkedHashMap<String, IDataWriterExtender>();
	private boolean configured;

	@Override
	public DataWriter createDataWriter() throws FactoryException {
		DataWriter dw;

		// determine the format of data being written
		final String dataFormat = LocalProperties.get(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
		if (dataFormat == null) {
			throw new FactoryException("Data writer format not specified ("  + StringUtils.quote(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT) + " property not found)");
		}

		final String dataHandlerClassName = "gda.data.scan.datawriter." + dataFormat;

		// find data handler class
		Class<?> dataHandlerClass;
		try {
			dataHandlerClass = Class.forName(dataHandlerClassName);
		} catch (ClassNotFoundException e) {
			throw new FactoryException("Could not find data handler class " + StringUtils.quote(dataHandlerClassName) + " (for data format " + StringUtils.quote(dataFormat) + ")", e);
		} catch (Exception e) {
			throw new FactoryException("Unable to load data handler class " + StringUtils.quote(dataHandlerClassName) + " (for data format " + StringUtils.quote(dataFormat) + ")", e);
		}

		// create a data handler object
		try {
			dw = (DataWriter) dataHandlerClass.newInstance();
		} catch (Exception e) {
			throw new FactoryException("Unable to create data handler of type " + StringUtils.quote(dataHandlerClassName) + " (for data format " + StringUtils.quote(dataFormat) + ")", e);
		}

		for (IDataWriterExtender dwe : dataWriterExtenders.values()) {
			if (dwe != null) {
				dw.addDataWriterExtender(dwe);
			}
		}
		return dw;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * This method added in to make the system work with Spring properly
	 * @param dataWriterExtenders
	 */
	public void setDataWriterExtenders(Collection<IDataWriterExtender> dataWriterExtenders) {
		this.dataWriterExtenders.clear();
		for(IDataWriterExtender extender: dataWriterExtenders) {
			addDataWriterExtender(extender);
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**GDA_DATA_SCAN_DATAWRITER_DATAFORMAT
	 * Add extender to be attached to newly created DataWriters
	 *
	 * @param dataWriterExtender
	 */
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		if (dataWriterExtender != null) {
			this.dataWriterExtenders.put(dataWriterExtender.getClass().toString() + dataWriterExtender.hashCode(),
					dataWriterExtender);
		}
	}

	/**
	 * Remove the given DataWriterExtender from the list of extenders added to fresh DataWriters created
	 *
	 * @param dataWriterExtender
	 */
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		this.dataWriterExtenders.remove(dataWriterExtender);
		String itsname = null;
		for (String dweName : dataWriterExtenders.keySet()) {
			IDataWriterExtender dwe = dataWriterExtenders.get(dweName);

			if (dwe == dataWriterExtender) {
				// TODO find out if we are allowed to remove in that loop
				itsname = dweName;
				break;
			}
		}
		if (itsname != null) {
			dataWriterExtenders.remove(itsname);
		}
	}

	/**
	 * Add extender to be attached to newly created DataWriters
	 *
	 * @param dataWriterExtenderName
	 */
	public void addDataWriterExtender(String dataWriterExtenderName) {

		if (!configured) {
			// might not exist then, put a placeholder
			this.dataWriterExtenders.put(dataWriterExtenderName, null);
		} else {
			IDataWriterExtender dataWriterExtender = findByName(dataWriterExtenderName);
			if (dataWriterExtender != null) {
				this.dataWriterExtenders.put(dataWriterExtenderName, dataWriterExtender);
			}
		}
	}


	/**
	 * Allow to retrieve the complete list of DataWriterExtenders configured
	 *
	 * @return list of DataWriterExtenders
	 */
	public Collection<IDataWriterExtender> getDataWriterExtenders() {
		return dataWriterExtenders.values();
	}

	/**
	 * Allow to retrieve the complete list of DataWriterExtenders configured
	 *
	 * @return list of DataWriterExtenders
	 */
	public Collection<String> getDataWriterExtenderNames() {
		return dataWriterExtenders.keySet();
	}

	@Override
	public void configure() throws FactoryException {
		for (String dweName : dataWriterExtenders.keySet()) {
			if (dataWriterExtenders.get(dweName) == null) {

				dataWriterExtenders.put(dweName, findByName(dweName));
			}
		}
		configured = true;
	}

	private IDataWriterExtender findByName(String dweName) {
		Object tmp = Finder.getInstance().find(dweName);
		if (tmp instanceof IDataWriterExtender) {
			return (IDataWriterExtender) tmp;
		}
		return null;
	}

	/**
	 * Create a DataWriter object from the first findable DataWriterFactory
	 *
	 * @throws Exception
	 */
	static public DataWriter createDataWriterFromFactory() throws Exception {

		Map<String, DataWriterFactory> factoryMap = Finder.getInstance().getFindablesOfType(DataWriterFactory.class);

		if (factoryMap.size() == 0) {
			// no factory configured - default for now
			return new DefaultDataWriterFactory().createDataWriter();
		}

		String factoryName = factoryMap.keySet().iterator().next();
		if (factoryMap.size() > 1) {
			logger.warn("more than one DataWriterFactory found, choosing " + factoryName);
		}
		return factoryMap.get(factoryName).createDataWriter();
	}
}