package org.opengda.detector.electronanalyser.nxdetector;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NXDetector;
import gda.device.detector.NexusDetector;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanInformation;
import gda.util.Sleep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.nexusformat.NeXusFileInterface;
import org.opengda.detector.electronanalyser.NotSupportedException;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DocumentRoot;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.SequenceEditingDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class EW4000 extends NXDetector implements InitializingBean, NexusDetector,PositionCallableProvider<NexusTreeProvider> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1155203719584202094L;

	private static final Logger logger = LoggerFactory.getLogger(EW4000.class);
	private ObservableComponent oc = new ObservableComponent();
	private String sequenceFilename;


	private Long scannumber;

	private Sequence sequence;
	public EW4000() {
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
		return getSequenceFilename();
	}

	@Override
	@MethodAccessProtected(isProtected = true)
	public void asynchronousMoveTo(Object position) throws DeviceException {
		setSequenceFilename(position.toString());
		atScanLineStart();
		NumTracker numTracker;
		try {
			numTracker = new NumTracker(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME));
		} catch (IOException e) {
			logger.error("Cannot create Number tracker instance", e);
			throw new DeviceException("Cannot create Number tracker instance", e);
		}
		long scannumber=numTracker.getCurrentFileNumber();
		if (scannumber==this.scannumber) {
			this.scannumber=numTracker.incrementNumber();
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
	public boolean isAt(Object positionToTest) throws DeviceException {
		return super.isAt(positionToTest);
	}
	@Override
	public String[] getInputNames() {
		return new String[] {"SequenceFilename"};
	}
	@Override
	public void setInputNames(String[] names) {
		super.setInputNames(names);
	}
	@Override
	public String[] getExtraNames() {
		return super.getExtraNames();
	}
	@Override
	public void setExtraNames(String[] names) {
		super.setExtraNames(names);
	}
	@Override
	public void setOutputFormat(String[] names) {
		super.setOutputFormat(names);
	}
	@Override
	public String[] getOutputFormat() {
		return new String[]{"%s"};
	}
	@Override
	public void atScanStart() throws DeviceException {
		ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		this.scannumber=scanInfo.getScanNumber();
		loadSequenceData();
		if (getCollectionStrategy() instanceof EW4000CollectionStrategy){
			EW4000CollectionStrategy ew4000CollectionStrategy = (EW4000CollectionStrategy)getCollectionStrategy();
			ew4000CollectionStrategy.setFirstInScan(true);
			ew4000CollectionStrategy.setSequence(sequence);
			ew4000CollectionStrategy.setScanDataPoint(0);
		}
		
		super.atScanStart();		
	}
	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();		
	}
	private void loadSequenceData() throws DeviceException{
		logger.debug("Sequence file changed to {}{}", FilenameUtils.getFullPath(sequenceFilename), FilenameUtils.getName(sequenceFilename));
		// List<Region> regions = regionResourceutil.getRegions(filename);
	try {
		Resource resource = getResource(sequenceFilename);
		resource.unload();
		resource.load(Collections.emptyMap());

		sequence = getSequence(resource);
	} catch (Exception e) {
		logger.error("Cannot load sequence file {}", sequenceFilename);
		throw new DeviceException("Cannot load sequence file.", e);
	}

	}
	@Override
	public void atScanLineStart() throws DeviceException {
		super.atScanLineStart();
		
	}
	@Override
	public void atScanLineEnd() throws DeviceException {
		super.atScanLineEnd();		
	}
	@Override
	public void atPointStart() throws DeviceException {
		super.atPointStart();		
	}
	@Override
	public void atPointEnd() throws DeviceException {
		super.atPointEnd();		
	}
	@Override
	public void atLevelMoveStart() throws DeviceException {
		super.atLevelMoveStart();		
	}
	@Override
	public void atCommandFailure() throws DeviceException {
		super.atCommandFailure();
	}
	@Override
	public String toFormattedString() {
		return super.toFormattedString();
	}
	@Override
	public void configure() throws FactoryException {
		super.configure();
	}
	@Override
	public void reconfigure() throws FactoryException {
		super.reconfigure();
	}
	@Override
	public Callable<NexusTreeProvider> getPositionCallable()
			throws DeviceException {
		return super.getPositionCallable();
	}
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return super.readout();
	}
	@Override
	public void afterPropertiesSet() {
		if (getSequenceFilename()== null) {
			throw new IllegalStateException("No sequenec file been configured");
		}
		super.afterPropertiesSet();
	}
	
	public Resource getResource(String fileName) throws Exception {
		ResourceSet resourceSet = getResourceSet();
		File seqFile = new File(fileName);
		if (seqFile.exists()) {
			URI fileURI = URI.createFileURI(fileName);
			return resourceSet.getResource(fileURI, true);
		}
		return null;
	}

	private ResourceSet getResourceSet() throws Exception {
		EditingDomain sequenceEditingDomain = SequenceEditingDomain.INSTANCE.getEditingDomain();
		ResourceSet resourceSet = sequenceEditingDomain.getResourceSet();

		return resourceSet;
	}

	public Sequence getSequence(Resource res) throws Exception {
		if (res != null) {
			List<EObject> contents = res.getContents();
			EObject eobj = contents.get(0);
			if (eobj instanceof DocumentRoot) {
				DocumentRoot root = (DocumentRoot) eobj;
				return root.getSequence();
			}
		}
		return null;
	}

	public List<Region> getRegions(Sequence sequence) throws Exception {
		if (sequence != null) {
			return sequence.getRegion();
		}
		return Collections.emptyList();
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



}
