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
	List<Region> regionlist=new ArrayList<Region>();
	private String sequenceFilename;
	private VGScientaAnalyser analyser;
	AtomicBoolean busy = new AtomicBoolean(false);
	NexusDataWriterExtension nexusDataWriter;
	private Thread collectionThread;
	private List<String> extraValues=new ArrayList<String>();
	private boolean sourceSelectable=false;
	private double XRaySourceEnergyLimit=2100;
	private Scannable dcmenergy;
	private Scannable pgmenergy;


	private Long scannumber;
	public EW4000() {
	}
	@Override
	public void collectData() throws DeviceException {
		Runnable target = new Runnable() {
			
			@Override
			public void run() {
				nexusDataWriter = new NexusDataWriterExtension(scannumber);
				busy.getAndSet(true);
				for (Region region : regionlist) {
					if(Thread.currentThread().isInterrupted()) break;
					if (region.isEnabled()) {
						try {
							configureAnalyser(region);
							NeXusFileInterface file = nexusDataWriter.createFile(region.getName(), "%d_%s");
							getAnalyser().setNexusFile(file);
							getAnalyser().collectData();
							getAnalyser().waitWhileBusy();
							extraValues.add(getAnalyser().writeOut());
						} catch (InterruptedException e) {
							try {
								getAnalyser().stop();
								extraValues.add(getAnalyser().writeOut());
							} catch (DeviceException e1) {
								logger.error("failed to stop the analyser acquisition on interrupt.", e1);
							}
						} catch (Exception e) {
							logger.error("Set new region to detector failed", e);
						}
					}
				}
				busy.getAndSet(false);
			}
		};
		collectionThread=new Thread(target, "ew4000");
		collectionThread.start();
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
		while (isBusy()) {
			Sleep.sleep(500);
		}
	}
	@Override
	public int[] getDataDimensions() throws DeviceException {
		int count=0;
		for (Region region : regionlist) {
			if (region.isEnabled()) {
				count += 1;
			}
		}
		return new int[] { count };
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
	public void moveTo(Object position) throws DeviceException {
		super.moveTo(position);		
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
		//TODO re-investigate this.
		Thread mythread=collectionThread;
		collectionThread=null;
		mythread.interrupt();
		try {
			nexusDataWriter.completeCollection();
		} catch (Exception e) {
			throw new DeviceException("Exception on "+getName()+"'s own NexusDataWriter.completeCollection() at stop()", e);
		}
		super.stop();		
	}
	@Override
	public boolean isBusy() {
		return busy.get();
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
		List<String> extraNames = new ArrayList<String>();
		for (Region region : regionlist) {
			if (region.isEnabled()) {
				extraNames.add(region.getName());
			}
		}
		if (new HashSet<String>(extraNames).size() < extraNames.size()) {
			String namesString = StringUtils.join(extraNames.toArray(), ", ");
			throw new IllegalStateException("The configured sequence contains duplicate region names: '" + namesString + "'.");
		}
		return (String[]) ArrayUtils.addAll(extraNames.toArray(new String[] {}), super.getExtraNames());
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
		List<String> formats = new ArrayList<String>();
		// specify inputName format
		formats.add("%s");
		// specify extraName format
		for (Region region : regionlist) {
			if (region.isEnabled()) {
				formats.add("%s");
			}
		}
		return (String[]) ArrayUtils.addAll(formats.toArray(new String[] {}), super.getOutputFormat());
	}
	@Override
	public void atScanStart() throws DeviceException {
		ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		this.scannumber=scanInfo.getScanNumber();
		super.atScanStart();		
	}
	@Override
	public void atScanEnd() throws DeviceException {
		try {
			nexusDataWriter.completeCollection();
		} catch (Exception e) {
			throw new DeviceException("Exception on "+getName()+"'s own NexusDataWriter.completeCollection() at atScanEnd()", e);
		}
		super.atScanEnd();		
	}
	@Override
	public void atScanLineStart() throws DeviceException {
		logger.debug("Sequence file changed to {}{}", FilenameUtils.getFullPath(sequenceFilename), FilenameUtils.getName(sequenceFilename));
			// List<Region> regions = regionResourceutil.getRegions(filename);
		try {
			Resource resource = getResource(sequenceFilename);
			resource.unload();
			resource.load(Collections.emptyMap());

			Sequence sequence = getSequence(resource);
			regionlist = getRegions(sequence);
		} catch (Exception e) {
			logger.error("Cannot load sequence file {}", sequenceFilename);
			throw new DeviceException("Cannot load sequence file.", e);
		}
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
	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}
	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
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
	private void configureAnalyser(Region region) throws Exception {
		try {
			getAnalyser().setCameraMinX(region.getFirstXChannel()-1, 1.0);
			getAnalyser().setCameraMinY(region.getFirstYChannel()-1, 1.0);
			getAnalyser().setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel()+1, 1.0);
			getAnalyser().setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel()+1, 1.0);
			getAnalyser().setSlices(region.getSlices(), 1.0);
			getAnalyser().setDetectorMode(region.getDetectorMode().getLiteral(), 1.0);
			getAnalyser().setLensMode(region.getLensMode(), 1.0);
			String literal = region.getEnergyMode().getLiteral();
			getAnalyser().setEnergysMode(literal,1.0);
			Double beamenergy;
			if (isSourceSelectable()) {
				if (region.getExcitationEnergy()<getXRaySourceEnergyLimit()) {
					beamenergy=Double.valueOf(getPgmenergy().getPosition().toString());
				} else {
					beamenergy=Double.valueOf(getDcmenergy().getPosition().toString())*1000;
				}
			} else {
				beamenergy=Double.valueOf(getPgmenergy().getPosition().toString());
			}
			getAnalyser().setExcitationEnergy(beamenergy);
			getAnalyser().setPassEnergy(region.getPassEnergy(), 1.0);
			if (literal.equalsIgnoreCase("Binding")) {
				//TODO a hack to solve EPICS cannot do binding energy issue, should be removed once EPICS issue solved.
				if (region.getExcitationEnergy()<getXRaySourceEnergyLimit()) {
					getAnalyser().setStartEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getHighEnergy(), 1.0);
					getAnalyser().setEndEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getLowEnergy(), 1.0);
					getAnalyser().setCentreEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getFixEnergy(), 1.0);
				} else {
					getAnalyser().setStartEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getHighEnergy(), 1.0);
					getAnalyser().setEndEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getLowEnergy(), 1.0);
					getAnalyser().setCentreEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getFixEnergy(), 1.0);
				}
				getAnalyser().setEnergysMode("Kinetic",1.0);
			} else {
				getAnalyser().setStartEnergy(region.getLowEnergy(), 1.0);
				getAnalyser().setEndEnergy(region.getHighEnergy(), 1.0);
				getAnalyser().setCentreEnergy(region.getFixEnergy(), 1.0);
			}
			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral(), 1.0);
			getAnalyser().setEnergyStep(region.getEnergyStep() / 1000.0, 1.0);
			collectionTime = region.getStepTime();
			getAnalyser().setStepTime(collectionTime, 1.0);
			if (!region.getRunMode().isConfirmAfterEachIteration()) {
				if (!region.getRunMode().isRepeatUntilStopped()) {
					getAnalyser().setNumberInterations(region.getRunMode().getNumIterations(), 1.0);
					getAnalyser().setImageMode(ImageMode.SINGLE, 1.0);
				} else {
					getAnalyser().setNumberInterations(1000000, 1.0);
					getAnalyser().setImageMode(ImageMode.SINGLE, 1.0);
				}
			} else {
				getAnalyser().setNumberInterations(1, 1.0);
				getAnalyser().setImageMode(ImageMode.SINGLE, 1.0);
				throw new NotSupportedException(
						"Confirm after each iteraction is not yet supported");
			}
			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral(), 1.0);
		} catch (Exception e) {
			throw e;
		} 
		// if (scriptController!=null) {
		// ((ScriptControllerBase)scriptController).update(this, new
		// RegionChangeEvent(region.getRegionId()));
		// }
		oc.notifyIObservers(this, new RegionChangeEvent(region.getRegionId()));
	}
	public boolean isSourceSelectable() {
		return sourceSelectable;
	}
	public void setSourceSelectable(boolean sourceSelectable) {
		this.sourceSelectable = sourceSelectable;
	}
	public double getXRaySourceEnergyLimit() {
		return XRaySourceEnergyLimit;
	}
	public void setXRaySourceEnergyLimit(double xRaySourceEnergyLimit) {
		XRaySourceEnergyLimit = xRaySourceEnergyLimit;
	}
	public Scannable getDcmenergy() {
		return dcmenergy;
	}
	public void setDcmenergy(Scannable dcmenergy) {
		this.dcmenergy = dcmenergy;
	}
	public Scannable getPgmenergy() {
		return pgmenergy;
	}
	public void setPgmenergy(Scannable pgmenergy) {
		this.pgmenergy = pgmenergy;
	}

}
