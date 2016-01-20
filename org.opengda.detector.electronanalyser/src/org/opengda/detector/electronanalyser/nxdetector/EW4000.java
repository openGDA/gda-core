package org.opengda.detector.electronanalyser.nxdetector;

import static gda.jython.InterfaceProvider.getCurrentScanInformationHolder;
import static gda.jython.InterfaceProvider.getTerminalPrinter;
import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.NXDetector;
import gda.device.detector.NexusDetector;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.opengda.detector.electronanalyser.event.ScanEndEvent;
import org.opengda.detector.electronanalyser.event.ScanPointStartEvent;
import org.opengda.detector.electronanalyser.event.ScanStartEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
/**
 * a container class for VGScienta Electron Analyser, which takes a sequence file
 * defining a list of regions as input and collect analyser data - image, spectrum and external IO data -
 * for each active regions in the listed order, and create a nexus file for each region.
 *
 * @author fy65
 *
 */
@CorbaAdapterClass(DeviceAdapter.class)
@CorbaImplClass(DeviceImpl.class)
public class EW4000 extends NXDetector implements InitializingBean, NexusDetector,PositionCallableProvider<NexusTreeProvider> {
	/**
	 *
	 */
	private static final long serialVersionUID = -1155203719584202094L;

	private static final Logger logger = LoggerFactory.getLogger(EW4000.class);
	private String sequenceFilename;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	EW4000CollectionStrategy collectionStrategy;
	private Scriptcontroller scriptcontroller;

	private int currentPointNumber;


	public EW4000() {
	}
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getCollectionStrategy() instanceof EW4000CollectionStrategy) {
				collectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
				collectionStrategy.setSourceSelectable(regionDefinitionResourceUtil.isSourceSelectable());
				collectionStrategy.setXRaySourceEnergyLimit(regionDefinitionResourceUtil.getXRaySourceEnergyLimit());
			}
			setConfigured(true);
		}
		super.configure();
	}

	@Override
	public void collectData() throws DeviceException {
		super.collectData();
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		// No-op defined by the list of active regions
	}
	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			// total time from all regions
			return getCollectionStrategy().getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("Cannot get contained regions time.", e);
		}
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] {collectionStrategy.getNumberOfActiveRegions()};
	}
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}
	@Override
	public String getDescription() throws DeviceException {
		return "VGH Scienta Electron Analyser EW4000";
	}
	@Override
	public String getDetectorID() throws DeviceException {
		return "EW4000";
	}
	@Override
	public String getDetectorType() throws DeviceException {
		return "Electron Analyser";
	}
	@Override
	public Object getPosition() throws DeviceException {
		try {
			// return a list of total intensities - one from each active region
			return collectionStrategy.getTotalIntensity();
		} catch (Exception e) {
			logger.error("getposition() failed with exception",e);
			e.printStackTrace();
		}
		return 0;
	}
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		// TODO Auto-generated method stub
		return super.readout();
	}
	@Override
	public void moveTo(Object position) throws DeviceException {
		super.moveTo(position);
		try {
			getCollectionStrategy().completeCollection();
			print("region collections completed.");
		} catch (Exception e) {
			logger.error("Error on calling completeCollection", e);
			throw new DeviceException("Error on calling completeCollection", e);
		}
	}

	@Override
	@MethodAccessProtected(isProtected = true)
	public void asynchronousMoveTo(Object position) throws DeviceException {
		Sequence sequence = loadSequenceData(position.toString());
		NumTracker numTracker;
		try {
			numTracker = new NumTracker(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME));
		} catch (IOException e) {
			logger.error("Cannot create Number tracker instance", e);
			throw new DeviceException("Cannot create Number tracker instance", e);
		}
		print("This is not a scan, so no scan file will be written");
		int scannumber = numTracker.incrementNumber();
		collectionStrategy.setSequence(sequence);
		collectionStrategy.createDataWriter(scannumber);
		collectionStrategy.setScanDataPoint(0);
		print("=== Collect data starts at "+new Timestamp(new java.util.Date().getTime()).toString()+" ===");
		collectData();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Reinterupt this thread and log it
			logger.warn("Caught InterruptedException. Resetting thread to interupted", e);
			Thread.currentThread().interrupt();
		}
		print("=== Collect data ends at "+new Timestamp(new java.util.Date().getTime()).toString()+" ===");
	}
	private void print(String message) {
		getTerminalPrinter().print(message);
	}
	@Override
	public void stop() throws DeviceException {
		if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new ScanEndEvent());
		}

		super.stop();
	}
	@Override
	public boolean isBusy() {
		try {
			return getCollectionStrategy().getStatus()==Detector.BUSY;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void atScanStart() throws DeviceException {
		int scannumber =getCurrentScanInformationHolder().getCurrentScanInformation().getScanNumber();
		int numberOfPoints = getCurrentScanInformationHolder().getCurrentScanInformation().getNumberOfPoints();
		String dataDir=PathConstructor.createFromDefaultProperty();
		if (!dataDir.endsWith(File.separator)) {
			dataDir += File.separator;
		}
		String beamline=LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
		String scanFilename=dataDir+String.format("%s-%d", beamline,scannumber) + ".nxs";
		if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new ScanStartEvent(scannumber,numberOfPoints,scanFilename));
		}
		print("Scan data will write to : "+ scanFilename);
		Sequence sequence = loadSequenceData(getSequenceFilename());
		collectionStrategy.setSequence(sequence);
		collectionStrategy.setScanDataPoint(0); // first data point
		currentPointNumber=0;
		super.atScanStart();
	}

	@Override
	public void atPointStart() throws DeviceException {
		currentPointNumber++;
		if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new ScanPointStartEvent(currentPointNumber));
		}
		super.atPointStart();
	}
	@Override
	public void atPointEnd() throws DeviceException {
		super.atPointEnd();
	}
	@Override
	public void atScanEnd() throws DeviceException {
		if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new ScanEndEvent());
		}
		super.atScanEnd();
	}
	public Sequence loadSequenceData(String sequenceFilename) throws DeviceException {
		Sequence sequence;
		logger.debug("Sequence file changed to {}{}",
				FilenameUtils.getFullPath(sequenceFilename),
				FilenameUtils.getName(sequenceFilename));
		try {
			Resource resource = regionDefinitionResourceUtil.getResource(sequenceFilename);
			resource.unload(); // must remove exisiting resource first for new one to be loaded in
			resource.load(Collections.emptyMap());
			sequence = regionDefinitionResourceUtil.getSequence(resource);
		} catch (Exception e) {
			logger.error("Cannot load sequence file {}", sequenceFilename);
			throw new DeviceException("Cannot load sequence file: "
					+ sequenceFilename, e);
		}
		return sequence;
	}

	@Override
	public void afterPropertiesSet() {
		if (getRegionDefinitionResourceUtil()== null) {
			throw new IllegalStateException("No region definition resource util been configured");
		}
		super.afterPropertiesSet();
	}


	public String getSequenceFilename() {
		return sequenceFilename;
	}
	public void setSequenceFilename(String sequenceFilename) {
		this.sequenceFilename = sequenceFilename;
	}

	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}
	public void setRegionDefinitionResourceUtil(
			RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
	}
	public void createSingleFile(boolean b){
		collectionStrategy.setSingleDataFile(b);
	}
	public Scriptcontroller getScriptcontroller() {
		return scriptcontroller;
	}
	public void setScriptcontroller(Scriptcontroller scriptcontroller) {
		this.scriptcontroller = scriptcontroller;
	}
}
