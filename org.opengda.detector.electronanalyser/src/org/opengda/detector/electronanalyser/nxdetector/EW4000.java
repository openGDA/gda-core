package org.opengda.detector.electronanalyser.nxdetector;

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
import gda.jython.InterfaceProvider;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.Sleep;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
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
public class EW4000 extends NXDetector implements InitializingBean, NexusDetector,PositionCallableProvider<NexusTreeProvider>, IObserver {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1155203719584202094L;

	private static final Logger logger = LoggerFactory.getLogger(EW4000.class);
	private ObservableComponent oc = new ObservableComponent();
	private String sequenceFilename;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	EW4000CollectionStrategy collectionStrategy;


	public EW4000() {
	}
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getCollectionStrategy() instanceof EW4000CollectionStrategy) {
				collectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
				collectionStrategy.setSourceSelectable(regionDefinitionResourceUtil.isSourceSelectable());
				collectionStrategy.setXRaySourceEnergyLimit(regionDefinitionResourceUtil.getXRaySourceEnergyLimit());
				collectionStrategy.addIObserver(this);
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
			return getCollectionStrategy().getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("Cannot get contained regions time.", e);
		}
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return super.getDataDimensions();
	}
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}
	@Override
	public String getDescription() throws DeviceException {
		return "EW4000 container";
	}
	@Override
	public String getDetectorID() throws DeviceException {
		return "EW4000";
	}
	@Override
	public String getDetectorType() throws DeviceException {
		return "EW4000";
	}
	@Override
	public Object getPosition() throws DeviceException {
		try {
			return getCollectionStrategy().getAcquireTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		super.moveTo(position);
		try {
			getCollectionStrategy().completeCollection();
			InterfaceProvider.getTerminalPrinter().print("region collections completed.");
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
		InterfaceProvider.getTerminalPrinter().print("This is not a scan, so no scan file will be written");
		long scannumber=numTracker.incrementNumber();
//		if (getCollectionStrategy() instanceof EW4000CollectionStrategy) {
//			EW4000CollectionStrategy collectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
			collectionStrategy.setSequence(sequence);
			collectionStrategy.createDataWriter(scannumber);
			collectionStrategy.setScanDataPoint(1);
			collectionStrategy.setTotalPoints(1);
//		}
		collectData();
		Sleep.sleep(1000);
	}
	@Override
	public void stop() throws DeviceException {
		super.stop();		
	}
	@Override
	public boolean isBusy() {
		try {
			return getCollectionStrategy().getStatus()==Detector.BUSY;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
//	@Override
//	public String[] getInputNames() {
//		return new String[] {"SequenceFilename"};
//	}

	@Override
	public void atScanStart() throws DeviceException {
		Long scannumber =InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getScanNumber();
		String dataDir=PathConstructor.createFromDefaultProperty();
		if (!dataDir.endsWith(File.separator)) {
			dataDir += File.separator;
		}
		String beamline=LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
		String scanFilename=dataDir+String.format("%s-%d", beamline,scannumber) + ".nxs";
		InterfaceProvider.getTerminalPrinter().print("Scan file with data link to individual region data files below will be written to : "+ scanFilename);
		int[] dims = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getDimensions();
		Sequence sequence=loadSequenceData(getSequenceFilename());
//		if (getCollectionStrategy() instanceof EW4000CollectionStrategy){
//			EW4000CollectionStrategy collectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
			collectionStrategy.setSequence(sequence);
			collectionStrategy.createDataWriter(scannumber);
			collectionStrategy.setScanDataPoint(1); //fisrt data point
			int expectedNumPixels = dims[0];
			for (int i = 1; i < dims.length; i++) {
				expectedNumPixels = expectedNumPixels * dims[i];
			}
			collectionStrategy.setTotalPoints(expectedNumPixels);
//		}
		
		super.atScanStart();		
	}

	public Sequence loadSequenceData(String sequenceFilename) throws DeviceException {
		Sequence sequence;
		logger.debug("Sequence file changed to {}{}",
				FilenameUtils.getFullPath(sequenceFilename),
				FilenameUtils.getName(sequenceFilename));
		try {
			Resource resource = regionDefinitionResourceUtil.getResource(sequenceFilename);
			resource.unload();
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

	@Override
	public void addIObserver(IObserver observer) {
		oc.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		oc.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		oc.deleteIObservers();
	}
	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}
	public void setRegionDefinitionResourceUtil(
			RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
	}
	@Override
	public void update(Object source, Object arg) {
		if (source instanceof EW4000CollectionStrategy && arg instanceof RegionChangeEvent) {
			logger.debug("new region is set to : {}", ((RegionChangeEvent)arg).getRegionName());
			oc.notifyIObservers(this, arg);
		}		
	}
}
