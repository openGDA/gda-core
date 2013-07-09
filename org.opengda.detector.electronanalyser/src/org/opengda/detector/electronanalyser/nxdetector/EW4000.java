package org.opengda.detector.electronanalyser.nxdetector;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetector;
import gda.device.detector.NexusDetector;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class EW4000 extends NXDetector implements InitializingBean, NexusDetector,PositionCallableProvider<NexusTreeProvider>, IObserver {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1155203719584202094L;

	private static final Logger logger = LoggerFactory.getLogger(EW4000.class);
	private ObservableComponent oc = new ObservableComponent();
	private String sequenceFilename;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;


	public EW4000() {
	}
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getCollectionStrategy() instanceof EW4000CollectionStrategy) {
				EW4000CollectionStrategy ew4000CollectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
				ew4000CollectionStrategy.setSourceSelectable(regionDefinitionResourceUtil.isSourceSelectable());
				ew4000CollectionStrategy.setXRaySourceEnergyLimit(regionDefinitionResourceUtil.getXRaySourceEnergyLimit());
				ew4000CollectionStrategy.addIObserver(this);
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
		// No-op time is set in setNewRegion(region)		
	}
	@Override
	public double getCollectionTime() throws DeviceException {
		return super.getCollectionTime();
	}
	@Override
	public int getStatus() throws DeviceException {
		//this wrapper detector may not have other status
		if (isBusy()) {
			return Detector.BUSY;
		} else {
			return Detector.IDLE;
		}
	}
	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		super.waitWhileBusy();
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
		stop(); //to clear file handles
		return getSequenceFilename();
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
		long scannumber=numTracker.incrementNumber();
		if (getCollectionStrategy() instanceof EW4000CollectionStrategy) {
			EW4000CollectionStrategy collectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
			collectionStrategy.createDataWriter(scannumber);
			collectionStrategy.setSequence(sequence);
			collectionStrategy.setScanDataPoint(0);
		}
		
		collectData();
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
	@Override
	public String[] getInputNames() {
		return new String[] {"SequenceFilename"};
	}

	@Override
	public String[] getOutputFormat() {
		return new String[]{"%s"};
	}
	@Override
	public void atScanStart() throws DeviceException {
		Sequence sequence=loadSequenceData(getSequenceFilename());
		if (getCollectionStrategy() instanceof EW4000CollectionStrategy){
			EW4000CollectionStrategy collectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
			collectionStrategy.setSequence(sequence);
			collectionStrategy.setScanDataPoint(0);
		}
		
		super.atScanStart();		
	}

	private Sequence loadSequenceData(String sequenceFilename) throws DeviceException {
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
		if (getCollectionStrategy()==source) {
			oc.notifyIObservers(this, arg);
		}		
	}
}
