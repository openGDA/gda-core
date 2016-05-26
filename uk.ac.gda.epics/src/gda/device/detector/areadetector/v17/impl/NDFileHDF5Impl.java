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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.NDFileHDF5Type;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class NDFileHDF5Impl implements InitializingBean, NDFileHDF5 {
	/* Note  NDFileHDF5Impl doesn't extend NDBaseImpl since it 'contains an' NDFile rather than being an NDFile. */

	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

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
	private NDFileHDF5Type config;
	private String deviceName;

	private Vector<String> compressionTypes = new Vector<String>();

	private Integer initialNumRowChunks = null;

	private Integer initialNumExtraDims = null;

	private Integer initialExtraDimSizeN = null;

	private Integer initialExtraDimSizeX = null;

	private Integer initialExtraDimSizeY = null;

	private String initialCompression = null;

	private Integer initialNumBitPrecision = null;

	private Integer initialNumBitOffset = null;

	private Integer initialSzipNumPixels = null;

	private Integer initialZCompressLevel = null;
	// Setup the logging facilities
	static final Logger logger = LoggerFactory.getLogger(NDFileHDF5Impl.class);

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
				config = Configurator.getConfiguration(deviceName, NDFileHDF5Type.class);
			} catch (ConfigurationNotFoundException e) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
			} catch (Exception ex) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", ex);
			}
		}
	}

	private static final String NumRowChunks = "NumRowChunks";

	private static final String NumRowChunks_RBV = "NumRowChunks_RBV";

	private static final String NumColChunks = "NumColChunks";

	private static final String NumColChunks_RBV = "NumColChunks_RBV";

	private static final String NumFramesChunks = "NumFramesChunks";

	private static final String NumFramesChunks_RBV = "NumFramesChunks_RBV";

	private static final String NumFramesFlush = "NumFramesFlush";
	private static final String LazyOpen = "LazyOpen";
	private static final String LazyOpen_RBV = "LazyOpen_RBV";

	private static final String NumFramesFlush_RBV = "NumFramesFlush_RBV";


	private static final String NumExtraDims = "NumExtraDims";

	private static final String NumExtraDims_RBV = "NumExtraDims_RBV";

	private static final String ExtraDimSizeN = "ExtraDimSizeN";

	private static final String ExtraDimSizeN_RBV = "ExtraDimSizeN_RBV";

	private static final String ExtraDimSizeX = "ExtraDimSizeX";

	private static final String ExtraDimSizeX_RBV = "ExtraDimSizeX_RBV";

	private static final String ExtraDimSizeY = "ExtraDimSizeY";

	private static final String ExtraDimSizeY_RBV = "ExtraDimSizeY_RBV";

	private static final String runtime = "Runtime";

	private static final String IOSpeed = "IOSpeed";

	private static final String Compression = "Compression";

	private static final String Compression_RBV = "Compression_RBV";

	private static final String NumBitPrecision = "NumDataBits";

	private static final String NumBitPrecision_RBV = "NumDataBits_RBV";

	private static final String NumBitOffset = "DataBitsOffset";

	private static final String NumBitOffset_RBV = "DataBitsOffset_RBV";

	private static final String szipNumPixels = "SZipNumPixels";

	private static final String szipNumPixels_RBV = "SZipNumPixels_RBV";

	private static final String ZCompressLevel = "ZLevel";

	private static final String ZCompressLevel_RBV = "ZLevel_RBV";

	private static final String StoreAttr = "StoreAttr";

	private static final String StorePerform = "StorePerform";

	private static final String BoundaryAlign = "BoundaryAlign";

	private static final String BoundaryAlign_RBV = "BoundaryAlign_RBV";

	private static final String AttrByDim_RBV = "DimAttDatasets_RBV";

	private static final String AttrByDim = "DimAttDatasets";

	@Override
	public int getNumRowChunks() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumRowChunks_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumRowChunks_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumRowChunks", ex);
			throw ex;
		}
	}

	@Override
	public void setNumRowChunks(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumRowChunks().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumRowChunks), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumRowChunks", ex);
			throw ex;
		}
	}

	@Override
	public int getNumExtraDims() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumExtraDims_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumExtraDims_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumExtraDims", ex);
			throw ex;
		}
	}

	@Override
	public void setNumExtraDims(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumExtraDims().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumExtraDims), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumExtraDims", ex);
			throw ex;
		}
	}

	@Override
	public int getExtraDimSizeN() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getExtraDimSizeN_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ExtraDimSizeN_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getExtraDimSizeN", ex);
			throw ex;
		}
	}

	@Override
	public void setExtraDimSizeN(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getExtraDimSizeN().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ExtraDimSizeN), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setExtraDimSizeN", ex);
			throw ex;
		}
	}

	@Override
	public int getExtraDimSizeX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getExtraDimSizeX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ExtraDimSizeX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getExtraDimSizeX", ex);
			throw ex;
		}
	}

	@Override
	public void setExtraDimSizeX(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getExtraDimSizeX().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ExtraDimSizeX), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setExtraDimSizeX", ex);
			throw ex;
		}
	}

	@Override
	public int getExtraDimSizeY() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getExtraDimSizeY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ExtraDimSizeY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getExtraDimSizeY", ex);
			throw ex;
		}
	}

	@Override
	public void setExtraDimSizeY(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getExtraDimSizeY().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ExtraDimSizeY), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setExtraDimSizeY", ex);
			throw ex;
		}
	}

	@Override
	public double getRuntime() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getRuntime().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(runtime));
		} catch (Exception ex) {
			logger.warn("Cannot getRuntime", ex);
			throw ex;
		}
	}

	@Override
	public double getIOSpeed() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getIOSpeed().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(IOSpeed));
		} catch (Exception ex) {
			logger.warn("Cannot getIOSpeed", ex);
			throw ex;
		}
	}

	@Override
	public Vector<String> listCompressionTypes() {
		return this.compressionTypes;
	}

	private void getCompressionTypes(double timeout) throws Exception {
		String[] compressionTypes = new String[] {};

		if (config != null) {
			compressionTypes = EPICS_CONTROLLER.cagetLabels(createChannel(config.getCompression().getPv()), timeout);
		} else {
			compressionTypes = EPICS_CONTROLLER.cagetLabels(getChannel(Compression), timeout);
		}
		for (String type : compressionTypes) {
			this.compressionTypes.add(type);
		}
	}

	@Override
	public String getCompression() throws Exception {
		int type = -1;
		if (this.compressionTypes.isEmpty())
			getCompressionTypes(10);
		try {
			if (config != null) {
				type = EPICS_CONTROLLER.cagetEnum(createChannel(config.getCompression_RBV().getPv()));
			} else {
				type = EPICS_CONTROLLER.cagetEnum(getChannel(Compression_RBV));
			}
			return this.compressionTypes.get(type);
		} catch (Exception ex) {
			logger.warn("Cannot getCompression", ex);
			throw ex;
		}
	}

	@Override
	public void setCompression(String type) throws Exception {
		int value = -1;
		if (this.compressionTypes.isEmpty())
			getCompressionTypes(10);
		if (this.compressionTypes.contains(type)) {
			value = this.compressionTypes.indexOf(type);
		} else {
			throw new IllegalArgumentException("Requested Compression Type " + type + " not supported.");
		}
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getCompression().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Compression), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setCompression", ex);
			throw ex;
		}
	}

	@Override
	public int getNumBitPrecision() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumBitPrecision_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumBitPrecision_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumBitPrecision", ex);
			throw ex;
		}
	}

	@Override
	public void setNumBitPrecision(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumBitPrecision().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumBitPrecision), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumBitPrecision", ex);
			throw ex;
		}
	}

	@Override
	public int getNumBitOffset() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumBitOffset_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumBitOffset_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumBitOffset", ex);
			throw ex;
		}
	}

	@Override
	public void setNumBitOffset(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumBitOffset().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumBitOffset), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumBitOffset", ex);
			throw ex;
		}
	}

	@Override
	public int getSzipNumPixels() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSzipNumPixels_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(szipNumPixels_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSzipNumPixels", ex);
			throw ex;
		}
	}

	@Override
	public void setSzipNumPixels(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSzipNumPixels().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(szipNumPixels), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSzipNumPixels", ex);
			throw ex;
		}
	}

	@Override
	public int getZCompressLevel() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getZCompressLevel_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ZCompressLevel_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getZCompressLevel", ex);
			throw ex;
		}
	}

	@Override
	public void setZCompressLevel(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getZCompressLevel().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ZCompressLevel), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setZCompressLevel", ex);
			throw ex;
		}
	}

	@Override
	public int getStoreAttr() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(StoreAttr));
		} catch (Exception ex) {
			logger.warn("Cannot getStoreAttr", ex);
			throw ex;
		}
	}

	@Override
	public void setStoreAttr(int storeAttr) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(StoreAttr), storeAttr);
		} catch (Exception ex) {
			logger.warn("Cannot setStoreAttr", ex);
			throw ex;
		}
	}

	@Override
	public int getStorePerform() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(StorePerform));
		} catch (Exception ex) {
			logger.warn("Cannot getStorePerform", ex);
			throw ex;
		}
	}

	@Override
	public void setStorePerform(int storePerform) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(StorePerform), storePerform);
		} catch (Exception ex) {
			logger.warn("Cannot setStorePerform", ex);
			throw ex;
		}
	}

	/**
	 * @return Returns the basePVName.
	 */
	public String getBasePVName() {
		return basePVName;
	}

	public void setInitialNumRowChunks(int initialNumRowChunks) {
		this.initialNumRowChunks = initialNumRowChunks;
	}

	public int getInitialNumRowChunks() {
		return initialNumRowChunks;
	}

	public void setInitialNumExtraDims(int initialNumExtraDims) {
		this.initialNumExtraDims = initialNumExtraDims;
	}

	public int getInitialNumExtraDims() {
		return initialNumExtraDims;
	}

	public void setInitialExtraDimSizeN(int initialExtraDimSizeN) {
		this.initialExtraDimSizeN = initialExtraDimSizeN;
	}

	public int getInitialExtraDimSizeN() {
		return initialExtraDimSizeN;
	}

	public void setInitialExtraDimSizeX(int initialExtraDimSizeX) {
		this.initialExtraDimSizeX = initialExtraDimSizeX;
	}

	public int getInitialExtraDimSizeX() {
		return initialExtraDimSizeX;
	}

	public void setInitialExtraDimSizeY(int initialExtraDimSizeY) {
		this.initialExtraDimSizeY = initialExtraDimSizeY;
	}

	public int getInitialExtraDimSizeY() {
		return initialExtraDimSizeY;
	}

	public void setInitialCompression(String initialCompression) {
		switch (initialCompression) {
		case "N-bit":
			logger.error("{} uses '{}' compression which is dangerous, it throws away data!",
					getIdentifier(), initialCompression);
			break;
		case "szip":
			logger.warn("{} uses '{}' compression which is encumbered. It cannot be used for commercial purposes!",
					getIdentifier(), initialCompression);
			break;
		case "zlib":
			logger.info("{} uses '{}' compression.",
					getIdentifier(), initialCompression);
			break;
		default:
			logger.error("{} requests unsupported compression '{}', the recommended compression is 'zlib'",
					getIdentifier(), initialCompression);
		}
		this.initialCompression = initialCompression;
	}

	public String getInitialCompression() {
		return initialCompression;
	}

	public void setInitialNumBitPrecision(int initialNumBitPrecision) {
		this.initialNumBitPrecision = initialNumBitPrecision;
	}

	public int getInitialNumBitPrecision() {
		return initialNumBitPrecision;
	}

	public void setInitialNumBitOffset(int initialNumBitOffset) {
		this.initialNumBitOffset = initialNumBitOffset;
	}

	public int getInitialNumBitOffset() {
		return initialNumBitOffset;
	}

	public void setInitialSzipNumPixels(int initialszipNumPixels) {
		this.initialSzipNumPixels = initialszipNumPixels;
	}

	public int getInitialSzipNumPixels() {
		return initialSzipNumPixels;
	}

	public void setInitialZCompressLevel(int initialZCompressLevel) {
		this.initialZCompressLevel = initialZCompressLevel;
	}

	public int getInitialZCompressLevel() {
		return initialZCompressLevel;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (deviceName == null && basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
		}
		if (file == null) {
			throw new IllegalArgumentException("'file' needs to be declared");
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
	protected Channel getChannel(String pvElementName, String... args) throws Exception {
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
			logger.warn("g.d.d.a.v.i.NDFileNexusImpl-> Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("g.d.d.a.v.i.NDFileNexusImpl-> Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("g.d.d.a.v.i.NDFileNexusImpl-> Problem creating channel", te);
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
		if (initialNumRowChunks != null) {
			setNumRowChunks(initialNumRowChunks);
		}
		if (initialNumExtraDims != null) {
			setNumExtraDims(initialNumExtraDims);
		}
		if (initialExtraDimSizeN != null) {
			setExtraDimSizeN(initialExtraDimSizeN);
		}
		if (initialExtraDimSizeX != null) {
			setExtraDimSizeX(initialExtraDimSizeX);
		}
		if (initialExtraDimSizeY != null) {
			setExtraDimSizeY(initialExtraDimSizeY);
		}
		if (initialCompression != null) {
			setCompression(initialCompression);
		}
		if (initialNumBitPrecision != null) {
			setNumBitPrecision(initialNumBitPrecision);
		}
		if (initialNumBitOffset != null) {
			setNumBitOffset(initialNumBitOffset);
		}
		if (initialSzipNumPixels != null) {
			setSzipNumPixels(initialSzipNumPixels);
		}
		if (initialZCompressLevel != null) {
			setZCompressLevel(initialZCompressLevel);
		}
	}

	@Override
	public int getStatus() {
		return file.getStatus();
	}

	@Override
	public void startCapture() throws Exception {
		file.startCapture();
	}

	@Override
	public short getCapture() throws Exception {
		return file.getCapture();
	}

	@Override
	public void stopCapture() throws Exception {
		if (getFile().getPluginBase().isCallbackEnabled()) {
			file.stopCapture();
		}
	}

	@Override
	public String getFilePath() throws Exception {
		return file.getFilePath();
	}

	@Override
	public void setFilePath(String filepath) throws Exception {
		file.setFilePath(filepath);
	}

	@Override
	public String getFileName() throws Exception {
		return file.getFileName();
	}

	@Override
	public void setFileName(String filename) throws Exception {
		file.setFileName(filename);
	}

	@Override
	public int getFileNumber() throws Exception {
		return file.getFileNumber();
	}

	@Override
	public void setFileNumber(int filenumber) throws Exception {
		file.setFileNumber(filenumber);
	}

	@Override
	public short getAutoIncrement() throws Exception {
		return file.getAutoIncrement();
	}

	@Override
	public void setAutoIncrement(int autoincrement) throws Exception {
		file.setAutoIncrement(autoincrement);
	}

	@Override
	public String getFileTemplate() throws Exception {
		return file.getFileTemplate();
	}

	@Override
	public void setFileTemplate(String filetemplate) throws Exception {
		file.setFileTemplate(filetemplate);
	}

	@Override
	public short getAutoSave() throws Exception {
		return file.getAutoSave();
	}

	@Override
	public void setAutoSave(int autosave) throws Exception {
		file.setAutoSave(autosave);
	}

	@Override
	public short getWriteFile() throws Exception {
		return file.getWriteFile();
	}

	@Override
	public void setWriteFile(int writefile) throws Exception {
		file.setWriteFile(writefile);
	}

	@Override
	public short getReadFile() throws Exception {
		return file.getReadFile();
	}

	@Override
	public void setReadFile(int readfile) throws Exception {
		file.setReadFile(readfile);
	}

	@Override
	public String getFullFileName_RBV() throws Exception {
		return file.getFullFileName_RBV();
	}

	@Override
	public void setNumCapture(int numberOfDarks) throws Exception {
		file.setNumCapture(numberOfDarks);

	}

	@Override
	public short getCapture_RBV() throws Exception {
		return file.getCapture_RBV();
	}

	@Override
	public int getNumCaptured_RBV() throws Exception {
		return file.getNumCaptured_RBV();
	}

	@Override
	public String getArrayPort() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetString(getChannel("NDArrayPort"));
		} catch (Exception ex) {
			logger.warn("Cannot get Array port", ex);
			throw ex;
		}
	}

	@Override
	public int getNumColChunks() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumColChunks_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumColChunks_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumColChunks", ex);
			throw ex;
		}

	}

	@Override
	public void setNumColChunks(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumColChunks().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumColChunks), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumColChunks", ex);
			throw ex;
		}
	}

	@Override
	public int getNumFramesChunks() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumFramesChunks_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFramesChunks_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFramesChunks", ex);
			throw ex;
		}
	}

	@Override
	public void setNumFramesChunks(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumFramesChunks().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumFramesChunks), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumFramesChunks", ex);
			throw ex;
		}

	}

	@Override
	public int getNumFramesFlush() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumFramesFlush_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFramesFlush_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFramesFlush", ex);
			throw ex;
		}
	}

	@Override
	public void setNumFramesFlush(int value) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumFramesFlush().getPv()), value);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumFramesFlush), value);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setNumFramesFlush", ex);
			throw ex;
		}

	}

	@Override
	public void setLazyOpen(boolean open) throws Exception {
		if (config != null) {
			throw new IllegalArgumentException("LazyOpen not yet in the interface file");
		}
		EPICS_CONTROLLER.caputWait(getChannel(LazyOpen), open ? 1:0);
	}

	@Override
	public boolean isLazyOpen() throws Exception {
		if (config != null) {
			throw new IllegalArgumentException("LazyOpen not yet in the interface file");
		}
		return EPICS_CONTROLLER.cagetInt(getChannel(LazyOpen_RBV))==1;
	}

	@Override
	public void setBoundaryAlign(int boundaryAlign) throws Exception {
		if (config != null) {
			throw new IllegalArgumentException("BoundaryAlign not yet in the interface file");
		}
		EPICS_CONTROLLER.caputWait(getChannel(BoundaryAlign), boundaryAlign);

	}

	@Override
	public int getBoundaryAlign() throws Exception {
		if (config != null) {
			throw new IllegalArgumentException("BoundaryAlign not yet in the interface file");
		}
		return EPICS_CONTROLLER.cagetInt(getChannel(BoundaryAlign_RBV));
	}

	private String getIdentifier() {
		// Since this can be configured with either a deviceName, pvProvider or basePVname, return an identifier based on
		// whichever is currently in use. If multiple are available, return in this order, which matches usage in the class.
		String id = getDeviceName();
		if (id == null && pvProvider != null)
			try {
				id = pvProvider.getPV("");
			} catch (Exception e) {
				logger.error("pvProvider.getPV('') threw exception: ", e);
			}
		if (id == null) id = basePVName;
		return id;
	}

	@Override
	public boolean isStoreAttributesByDimension() throws Exception {
		try {
			int value = EPICS_CONTROLLER.cagetInt(getChannel(AttrByDim_RBV));
			if (value == 1) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			logger.warn("Cannot getAttrByDim", ex);
			throw ex;
		}
	}

	@Override
	public void setStoreAttributesByDimension(boolean storeAttributesByDimension) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(AttrByDim), storeAttributesByDimension ? 1 : 0);
		} catch (Exception ex) {
			logger.warn("Cannot setAttrByDim", ex);
			throw ex;
		}
	}

}
