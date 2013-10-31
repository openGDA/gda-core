package org.opengda.detector.electronanalyser.nxdetector;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXPlugin;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanInformation;
import gda.util.Sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.nexusformat.NeXusFileInterface;
import org.opengda.detector.electronanalyser.NotSupportedException;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.nxdata.NXDetectorDataFilenamesAppender;
import org.opengda.detector.electronanalyser.nxdetector.NexusDataWriterExtension.RegionFileMapper;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EW4000CollectionStrategy implements NXCollectionStrategyPlugin, NXPlugin, IObservable{
	private static final Logger logger = LoggerFactory.getLogger(EW4000CollectionStrategy.class);
	private ObservableComponent oc = new ObservableComponent();
	private AtomicBoolean busy = new AtomicBoolean(false);
	private NexusDataWriterExtension nexusDataWriter;
	private Thread collectionThread;
	private Vector<String> extraValues=new Vector<String>();
	public Vector<String> getExtraValues() {
		return extraValues;
	}

	public void setExtraValues(Vector<String> extraValues) {
		this.extraValues = extraValues;
	}

	private boolean sourceSelectable=false;
	private double XRaySourceEnergyLimit=2100;
	private Long scannumber;
	private Sequence sequence;
	private AtomicInteger scanDatapoint=new AtomicInteger(1);

	private VGScientaAnalyser analyser;
	private Scannable dcmenergy;
	private Scannable pgmenergy;
	private String name = "ew4000_collection_strategy";
	private int totalPoints;
	private Scriptcontroller scriptcontroller;
	
	@Override
	public void prepareForCollection(int numberImagesPerCollection,	ScanInformation scanInfo) throws Exception {
		this.scannumber=scanInfo.getScanNumber();
		createDataWriter(scannumber);
	}

	public void createDataWriter(Long scannumber) {
		if (nexusDataWriter!= null && !nexusDataWriter.getFiles().isEmpty()) {
			nexusDataWriter.releaseFile();
		}
		nexusDataWriter = new NexusDataWriterExtension(scannumber);
		extraValues.clear();
		for (Region region : sequence.getRegion()) {
			if (region.isEnabled()) {
				try {
					logger.warn("region name = {}", region.getName());
					nexusDataWriter.createFile(region.getName(), sequence);
				} catch (Exception e) {
					logger.error(
							"Error on create for for region "
									+ region.getName(), e);
					throw new RuntimeException(
							"Error on create for for region "
									+ region.getName(), e);
				}
			}
		}
		Map<String, RegionFileMapper> files = nexusDataWriter.getFiles();
		if (!files.isEmpty()) {
			for (Region region : sequence.getRegion()) {
				if (region.isEnabled()) {
					RegionFileMapper regionFileMapper = files.get(region.getName());
					extraValues.add(regionFileMapper.getURL());
				}
			}
		}
	}
	
	@Override
	public void collectData() throws Exception {
		Runnable target = new Runnable() {
			
			@Override
			public void run() {
				busy.getAndSet(true);
				for (Region region : sequence.getRegion()) {
					if(Thread.currentThread().isInterrupted()) break;
					if (region.isEnabled()) {
						try {
							configureAnalyser(region);
							Sleep.sleep(1000);
							NeXusFileInterface file = nexusDataWriter.getNXFile(region.getName(), scanDatapoint.get());
							getAnalyser().setNexusFile(file);
							getAnalyser().collectData();
							Sleep.sleep(1000);
							getAnalyser().waitWhileBusy();
							getAnalyser().writeOut(scanDatapoint.get());
						} catch (InterruptedException e) {
							try {
								getAnalyser().stop();
								getAnalyser().writeOut(scanDatapoint.get());
							} catch (DeviceException e1) {
								logger.error("failed to stop the analyser acquisition on interrupt.", e1);
							}
						}catch (DeviceException e) {
							logger.error("failed to collectdata or waitWhileBusy from the analyser.", e);
							break;
						}
						catch (Exception e) {
							logger.error("Set new region to detector failed", e);
							break;
						}
					}
//					Sleep.sleep(1000);
				}
				busy.getAndSet(false);
				scanDatapoint.incrementAndGet();
			}
		};
		collectionThread=new Thread(target, "ew4000collectionstrategy");
		collectionThread.start();
		
	}

	@Override
	public void completeCollection() throws Exception {
//		while (scanDatapoint.get()<=getTotalPoints()) {
//			Sleep.sleep(1000);
//		}
		nexusDataWriter.completeCollection();
		//nexusDataWriter=null;
	}

	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();		
	}

	@Override
	public void stop() throws Exception {
		Thread mythread=collectionThread;
		collectionThread=null;
		mythread.interrupt();
		waitWhileBusy();
		completeCollection();		
	}
	
	@Override
	public List<String> getInputStreamNames() {
		//TODO why this being called 7 time in a scan start
		List<String> extraNames = new ArrayList<String>();
		//extraNames.add("ew8000");
		return extraNames;
	}
	public List<String> getRegionNames() {
		List<String> extraNames = new ArrayList<String>();
		for (Region region : sequence.getRegion()) {
			if (region.isEnabled()) {
				extraNames.add(region.getName());
			}
		}
		return extraNames;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();
////		formats.add("%s");
//		// specify extraName format
//		for (Region region : sequence.getRegion()) {
//			if (region.isEnabled()) {
//				formats.add("%s");
//				
//			}
//		}
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)	throws NoSuchElementException, InterruptedException,
			DeviceException {
		if (getRegionNames().size() != extraValues.size()) {
			throw new DeviceException(getName()+" : Names size = "+getRegionNames().size()+" and values size = "+extraValues.size()+" are different.");
		}
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDataFilenamesAppender(getRegionNames(), extraValues));
		return appenders;
	}

	@Override
	public double getAcquireTime() throws Exception {
		double times = 0;
		for (Region region : sequence.getRegion()) {
			if (region.isEnabled()) {
				times += region.getTotalTime();
			}
		}
		return times;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return 0;
	}

	@Override
	@Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime)
			throws Exception {
	}

	@Override
	public void prepareForCollection(double collectionTime,
			int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		this.scannumber=scanInfo.getScanNumber();
		if (nexusDataWriter== null) {
			createDataWriter(scannumber);
		}
		try {
		NDPluginBase pluginBase = getAnalyser().getNdArray().getPluginBase();
		ADBase adBase = getAnalyser().getAdBase();
		if (!pluginBase.isCallbackEnabled()) {
			pluginBase.setNDArrayPort(adBase.getPortName_RBV());
			pluginBase.enableCallbacks();
			pluginBase.setBlockingCallbacks(1);
		}
		NDStats ndStats = getAnalyser().getNdStats();
		NDPluginBase pluginBase2 = ndStats.getPluginBase();
		if (!pluginBase2.isCallbackEnabled()) {
			pluginBase2.setNDArrayPort(adBase.getPortName_RBV());
			pluginBase2.enableCallbacks();
			pluginBase2.setBlockingCallbacks(1);
			ndStats.setComputeStatistics((short) 1);
			ndStats.setComputeCentroid((short) 1);
		}
		} catch (Exception e) {
			logger.error("failed to initialise ADArray and ADStats Plugins",e);
		}

	}


	@Override
	public int getStatus() throws Exception {
		if (busy.get()){
			return Detector.BUSY;
		} else {
			return Detector.IDLE;
		}
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		while (getStatus()==Detector.BUSY){
			Sleep.sleep(500);
		}		
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		
	}

	@Override
	public boolean isGenerateCallbacks() {
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime)
			throws Exception {
		return 1;
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

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
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
	public void setSequence(Sequence sequence) {
		this.sequence=sequence;		
	}
	public void setScanDataPoint(int i) {
		this.scanDatapoint.set(i);		
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
	protected int getNumberOfActiveRegions() {
		int size=0;
		for (Region region : sequence.getRegion()) {
			if (region.isEnabled()) {
				size +=1;
			}
		}
		return size;
	}
	private void configureAnalyser(Region region) throws Exception {
		try {
			getAnalyser().setRegionName(region.getName());
			getAnalyser().setCameraMinX(region.getFirstXChannel()-1, 10.0);
			getAnalyser().setCameraMinY(region.getFirstYChannel()-1, 10.0);
			getAnalyser().setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel()+1, 10.0);
			getAnalyser().setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel()+1, 10.0);
			getAnalyser().setSlices(region.getSlices(), 10.0);
			getAnalyser().setDetectorMode(region.getDetectorMode().getLiteral(), 10.0);
			getAnalyser().setLensMode(region.getLensMode(), 10.0);
			String literal = region.getEnergyMode().getLiteral();
			getAnalyser().setEnergyMode(literal,10.0);
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
			getAnalyser().setPassEnergy(region.getPassEnergy(), 10.0);
			if (literal.equalsIgnoreCase("Binding")) {
				// a hack to solve EPICS cannot do binding energy issue, should be removed once EPICS issue solved.
				if (region.getExcitationEnergy()<getXRaySourceEnergyLimit()) {
					getAnalyser().setStartEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getHighEnergy(), 10.0);
					getAnalyser().setEndEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getLowEnergy(), 10.0);
					getAnalyser().setCentreEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getFixEnergy(), 10.0);
				} else {
					getAnalyser().setStartEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getHighEnergy(), 10.0);
					getAnalyser().setEndEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getLowEnergy(), 10.0);
					getAnalyser().setCentreEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getFixEnergy(), 10.0);
				}
				getAnalyser().setEnergyMode("Kinetic",10.0);
			} else {
				getAnalyser().setStartEnergy(region.getLowEnergy(), 10.0);
				getAnalyser().setEndEnergy(region.getHighEnergy(), 10.0);
				getAnalyser().setCentreEnergy(region.getFixEnergy(), 10.0);
			}
			getAnalyser().setCachedEnergyMode(literal);
			
//			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral(), 10.0);
			getAnalyser().setEnergyStep(region.getEnergyStep() / 1000.0, 10.0);
			double collectionTime = region.getStepTime();
			getAnalyser().setStepTime(collectionTime, 10.0);
			if (!region.getRunMode().isConfirmAfterEachIteration()) {
				if (!region.getRunMode().isRepeatUntilStopped()) {
					getAnalyser().setNumberInterations(region.getRunMode().getNumIterations(), 10.0);
					getAnalyser().setImageMode(ImageMode.SINGLE, 10.0);
				} else {
					getAnalyser().setNumberInterations(1000000, 10.0);
					getAnalyser().setImageMode(ImageMode.SINGLE, 10.0);
				}
			} else {
				getAnalyser().setNumberInterations(1, 10.0);
				getAnalyser().setImageMode(ImageMode.SINGLE, 10.0);
				throw new NotSupportedException(
						"Confirm after each iteraction is not yet supported");
			}
			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral(), 10.0);
		} catch (Exception e) {
			throw e;
		} 
		if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getScriptcontroller()).update(this, new RegionChangeEvent(region.getRegionId(), region.getName()));
		}
	}
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name =name;
	}
	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForLine() throws Exception {
		// No-op
	}

	@Override
	public void completeLine() throws Exception {
		// No-op
	}


	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}

	public Scriptcontroller getScriptcontroller() {
		return scriptcontroller;
	}

	public void setScriptcontroller(Scriptcontroller scriptcontroller) {
		this.scriptcontroller = scriptcontroller;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

}
