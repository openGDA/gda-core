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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.factory.Finder;

public class DefaultDataWriterFactory extends FindableConfigurableBase implements DataWriterFactory {
	private static final Logger logger = LoggerFactory.getLogger(DefaultDataWriterFactory.class);

	private List<IDataWriterExtender> dataWriterExtenders = new ArrayList<>();

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
			dw = (DataWriter) dataHandlerClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new FactoryException("Unable to create data handler of type " + StringUtils.quote(dataHandlerClassName) + " (for data format " + StringUtils.quote(dataFormat) + ")", e);
		}

		dataWriterExtenders.forEach(dw::addDataWriterExtender);

		return dw;
	}


	public void setDataWriterExtenders(Collection<IDataWriterExtender> dataWriterExtenders) {
		this.dataWriterExtenders.clear();
		dataWriterExtenders.forEach(this::addDataWriterExtender);
	}

	/**
	 * Add extender to be attached to newly created DataWriters
	 *
	 * @param dataWriterExtender
	 */
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		if (dataWriterExtender != null) {
			this.dataWriterExtenders.add(dataWriterExtender);
		}
	}

	/**
	 * Remove the given DataWriterExtender from the list of extenders added to fresh DataWriters created
	 *
	 * @param dataWriterExtender
	 */
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		dataWriterExtenders.removeIf(entry -> entry == dataWriterExtender);
	}

	/**
	 * Add extender to be attached to newly created DataWriters
	 *
	 * @param dataWriterExtenderName
	 * @deprecated use {@link #addDataWriterExtender(IDataWriterExtender)}
	 */
	@Deprecated(forRemoval = true, since = "GDA 9.25")
	public void addDataWriterExtender(String dataWriterExtenderName) {
		logger.warn("addDataWriterExtender(String) is deprecated and will be removed in GDA 9.27");
		IDataWriterExtender dataWriterExtender = findByName(dataWriterExtenderName);
		if (dataWriterExtender != null) {
			this.dataWriterExtenders.add(dataWriterExtender);
		}

	}


	/**
	 * Allow to retrieve the complete list of DataWriterExtenders configured
	 *
	 * @return list of DataWriterExtenders
	 */
	public Collection<IDataWriterExtender> getDataWriterExtenders() {
		return dataWriterExtenders;
	}

	/**
	 * @deprecated can be deleted once {@link #addDataWriterExtender(String)} is removed
	 */
	@Deprecated
	private IDataWriterExtender findByName(String dweName) {
		Object tmp = Finder.find(dweName);
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
	public static DataWriter createDataWriterFromFactory() throws Exception {

		Map<String, DataWriterFactory> factoryMap = Finder.getFindablesOfType(DataWriterFactory.class);

		if (factoryMap.size() == 0) {
			// no factory configured - default for now
			return new DefaultDataWriterFactory().createDataWriter();
		}

		String factoryName = factoryMap.keySet().iterator().next();
		if (factoryMap.size() > 1) {
			logger.warn("more than one DataWriterFactory found, choosing {}", factoryName);
		}
		return factoryMap.get(factoryName).createDataWriter();
	}
}