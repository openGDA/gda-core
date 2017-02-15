/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileNexus;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.NDFileNexusType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class NDFileNexusImpl implements InitializingBean, NDFileNexus {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	private IPVProvider pvProvider;

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private NDFile file;

	/**
	 * List all the PVs
	 */
	private NDFileNexusType config;
	private String deviceName;

	private String initialTemplateFileName;

	private String initialTemplateFilePath;
	// Setup the logging facilities
	static final Logger logger = LoggerFactory.getLogger(NDFileNexusImpl.class);

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) throws FactoryException {
		this.deviceName = deviceName;
		initializeConfig();
	}

	private void initializeConfig() throws FactoryException {
		if (deviceName != null) {
			try {
				config = Configurator.getConfiguration(deviceName, NDFileNexusType.class);
			} catch (ConfigurationNotFoundException e) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
			} catch (Exception ex) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", ex);
			}
		}
	}

	private static final String TemplateFilePath = "TemplateFilePath";

	private static final String TemplateFilePath_RBV = "TemplateFilePath_RBV";

	private static final String TemplateFileName = "TemplateFileName";

	private static final String TemplateFileName_RBV = "TemplateFileName_RBV";

	private static final String FileTemplateValid = "FileTemplateValid";

	/**
	*
	*/
	@Override
	public String getTemplateFilePath() throws Exception {
		try {
			if (config != null) {
				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getTemplateFilePath().getPv())))
						.trim();
			}
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(TemplateFilePath))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getTemplateFilePath", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setTemplateFilePath(String templatefilepath) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getTemplateFilePath().getPv()),
						(templatefilepath + '\0').getBytes());
			} else {
				EPICS_CONTROLLER.caput(getChannel(TemplateFilePath), (templatefilepath + '\0').getBytes());
			}
		} catch (Exception ex) {
			logger.warn("Cannot setTemplateFilePath", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getTemplateFilePath_RBV() throws Exception {
		try {
			if (config != null) {
				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getTemplateFilePath_RBV()
						.getPv()))).trim();
			}
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(TemplateFilePath_RBV))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getTemplateFilePath_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getTemplateFileName() throws Exception {
		try {
			if (config != null) {
				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getTemplateFileName().getPv())))
						.trim();
			}
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(TemplateFileName))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getTemplateFileName", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setTemplateFileName(String templatefilename) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getTemplateFileName().getPv()),
						(templatefilename + '\0').getBytes());
			} else {
				EPICS_CONTROLLER.caput(getChannel(TemplateFileName), (templatefilename + '\0').getBytes());
			}
		} catch (Exception ex) {
			logger.warn("Cannot setTemplateFileName", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getTemplateFileName_RBV() throws Exception {
		try {
			if (config != null) {
				return new String(EPICS_CONTROLLER.cagetByteArray(createChannel(config.getTemplateFileName_RBV()
						.getPv()))).trim();
			}
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(TemplateFileName_RBV))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getTemplateFileName_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getFileTemplateValid() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFileTemplateValid().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(FileTemplateValid));
		} catch (Exception ex) {
			logger.warn("Cannot getFileTemplateValid", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFileTemplateValid(int filetemplatevalid) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFileTemplateValid().getPv()), filetemplatevalid);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FileTemplateValid), filetemplatevalid);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setFileTemplateValid", ex);
			throw ex;
		}
	}

	/**
	 * @return Returns the basePVName.
	 */
	public String getBasePVName() {
		return basePVName;
	}

	public void setInitialTemplateFileName(String initialTemplateFileName){
		this.initialTemplateFileName = initialTemplateFileName;
	}
	public String getInitialTemplateFileName(){
		return initialTemplateFileName;
	}
	public void setInitialTemplateFilePath(String initialTemplateFilePath){
		this.initialTemplateFilePath = initialTemplateFilePath;
	}
	public String getInitialTemplateFilePath(){
		return initialTemplateFilePath;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		if (deviceName == null && basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
		}
	}

	/**
	 * @param basePVName
	 *            The basePVName to set.
	 */
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	/**
	 * This method allows to toggle between the method in which the PV is acquired.
	 *
	 * @param pvElementName
	 * @param args
	 * @return {@link Channel} to talk to the relevant PV.
	 * @throws Exception
	 */
	private Channel getChannel(String pvElementName, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			String fullPvName;
			if (pvProvider != null) {
				fullPvName = pvProvider.getPV(pvElementName);
			} else {
				fullPvName = basePVName + pvPostFix;
			}
			return createChannel(fullPvName);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("Problem creating channel", te);
				throw te;

			}
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	/**
	 * @return Returns the pvProvider.
	 */
	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	/**
	 * @param pvProvider
	 *            The pvProvider to set.
	 */
	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	@Override
	public NDFile getFile() {
		return file;
	}

	/**
	 * @param file
	 *            The file to set.
	 */
	public void setFile(NDFile file) {
		this.file = file;
	}
	@Override
	public void reset() throws Exception {
		// Reset the plugin base variables.
		file.reset();
		// Reset local variables.
		if (initialTemplateFileName != null) {
			setTemplateFileName(initialTemplateFileName);
		}
		if (initialTemplateFilePath != null) {
			setTemplateFilePath(initialTemplateFilePath);
		}
	}

}
