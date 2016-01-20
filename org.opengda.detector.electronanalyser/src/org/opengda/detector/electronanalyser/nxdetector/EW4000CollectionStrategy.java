package org.opengda.detector.electronanalyser.nxdetector;

import gda.data.nexus.tree.INexusTree;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXFileWriterPlugin;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.dawnsci.nexus.NexusFile;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.event.RegionStatusEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.STATUS;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.nxdata.NXDetectorDataAnalyserRegionAppender;
import org.opengda.detector.electronanalyser.nxdata.NXDetectorDataFilenamesAppender;
import org.opengda.detector.electronanalyser.nxdetector.NexusDataWriterExtension.RegionFileMapper;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EW4000CollectionStrategy implements NXCollectionStrategyPlugin, NXFileWriterPlugin,IObservable{
	private static final Logger logger = LoggerFactory.getLogger(EW4000CollectionStrategy.class);
	private ObservableComponent oc = new ObservableComponent();
	private AtomicBoolean busy = new AtomicBoolean(false);
	private NexusDataWriterExtension nexusDataWriter;
	private Vector<String> activeRegionNames=new Vector<String>();
	private Thread collectionThread;
	private Vector<String> extraValues=new Vector<String>();
	private Scannable softXRayFastShutter;
	private Scannable hardXRayFastShutter;
	public Vector<String> getExtraValues() {
		return extraValues;
	}

	public void setExtraValues(Vector<String> extraValues) {
		this.extraValues = extraValues;
	}

	private boolean sourceSelectable=false;
	private double XRaySourceEnergyLimit=2100;
	private int scannumber;
	private Sequence sequence;
	private AtomicInteger scanDatapoint=new AtomicInteger(0);

	private VGScientaAnalyser analyser;
	private Scannable dcmenergy;
	private Scannable pgmenergy;
	private boolean singleDataFile=true;
	private String name = "ew4000_collection_strategy";
	private Scriptcontroller scriptcontroller;
	protected Region lastregion=null;
	private volatile boolean called=true;

	@Override
	public void prepareForCollection(int numberImagesPerCollection,	ScanInformation scanInfo) throws Exception {
		this.scannumber=scanInfo.getScanNumber();
		if (!isSingleDataFile() && nexusDataWriter==null) {
			createDataWriter(scannumber);
		}
		setActiveRegionNames(getRegionNames());
	}

	public void createDataWriter(int scannumber) {
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
							"Error on create for region " + region.getName(), e);
					throw new RuntimeException(
							"Error on create for region " + region.getName(), e);
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

	Vector<INexusTree> regionDataList= new Vector<INexusTree>();
	private Vector<Double> totalIntensity=new Vector<Double>();

	private boolean stillHaveDataToWrite=false;

	protected void beforeCollectData() throws InterruptedException {
		busy.getAndSet(true);
		scanDatapoint.incrementAndGet();
		while (!called){
			Thread.sleep(100);
		}
		called=false;
		if (!regionDataList.isEmpty()) {
			regionDataList.clear();
			totalIntensity.clear();
		}
	}
	@Override
	public void collectData() throws Exception {
		beforeCollectData();
		Runnable target = new Runnable() {

			@Override
			public void run() {
				int i=0;
				for (Region region : sequence.getRegion()) {
					if(Thread.currentThread().isInterrupted()) break;
					if (region.isEnabled()) {
						i++;
						try {
							configureAnalyser(region);
							Thread.sleep(1000);
							lastregion=region;
							if (!isSingleDataFile()) {
								NexusFile file = nexusDataWriter.getNXFile(region.getName(), scanDatapoint.get());
								getAnalyser().setNexusFile(file);
							}
							//open/close fast shutter according to beam used
							if (region.getExcitationEnergy()<getXRaySourceEnergyLimit()) {
								if (softXRayFastShutter!=null) {
									softXRayFastShutter.moveTo("Out");
								}
								if (hardXRayFastShutter!=null) {
									hardXRayFastShutter.moveTo("In");
								}
							} else {
								if (softXRayFastShutter!=null) {
									softXRayFastShutter.moveTo("In");
								}
								if (hardXRayFastShutter!=null) {
									hardXRayFastShutter.moveTo("Out");
								}
							}
							if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
								((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new RegionStatusEvent(region.getRegionId(), STATUS.RUNNING,i));
							}
							getAnalyser().collectData();
							Thread.sleep(1000);
							getAnalyser().waitWhileBusy();
							if (!isSingleDataFile()) {
								getAnalyser().writeOut(scanDatapoint.get());
								totalIntensity.add(getAnalyser().getTotalIntensity());
							} else {
								if (scanDatapoint.get()==1) {
									regionDataList.add(getAnalyser().createRegionNodeWithFirstData(region.getName()));
									totalIntensity.add(getAnalyser().getTotalIntensity());
								} else {
									regionDataList.add(getAnalyser().createRegionNodeWithNewData(region.getName()));
									totalIntensity.add(getAnalyser().getTotalIntensity());
								}
							}
							if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
								((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new RegionStatusEvent(region.getRegionId(), STATUS.COMPLETED,i));
							}

						} catch (InterruptedException e) {
							try {
								getAnalyser().stop();
								if (!isSingleDataFile()) {
									getAnalyser().writeOut(scanDatapoint.get());
								} else {
									if (!regionDataList.isEmpty()) {
										//on interruption,regionDataList may contain region data already collected, these should be write to the data file
										logger.warn("region data list is not empty. Have {} regions data to write out",regionDataList.size());
										setStillHaveDataToWrite(true);
									}
								}
							} catch (DeviceException e1) {
								logger.error("failed to stop the analyser acquisition on interrupt.", e1);
							}
							if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
								((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new RegionStatusEvent(region.getRegionId(), STATUS.ABORTED,i));
							}
						}catch (DeviceException e) {
							logger.error("failed to collectdata or waitWhileBusy from the analyser.", e);
							if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
								((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new RegionStatusEvent(region.getRegionId(), STATUS.INVALID,i));
							}
							break;
						}
						catch (Exception e) {
							logger.error("Set new region to detector failed", e);
							if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
								((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new RegionStatusEvent(region.getRegionId(), STATUS.INVALID,i));
							}
							break;
						}
					}
				}
				afterCollectData();
				busy.getAndSet(false);
			}
		};
		collectionThread=new Thread(target, "ew4000collectionstrategy");
		collectionThread.start();
	}
	// synced data
	List<INexusTree> copyofregiondatalist;
	List<Double> copyoftotalintensity;
	// temp copy of data
	ArrayList<INexusTree> lastRegionDataList;
	ArrayList<Double> lastTotalIntensityList;
	private void afterCollectData(){
		lastRegionDataList = new ArrayList<INexusTree>();
		for (INexusTree item : regionDataList) {
			lastRegionDataList.add(item);
		}
		copyofregiondatalist=Collections.synchronizedList(new ArrayList<INexusTree>(lastRegionDataList));
		lastTotalIntensityList= new ArrayList<Double>();
		for (Double item : totalIntensity) {
			lastTotalIntensityList.add(item);
		}
		copyoftotalintensity=Collections.synchronizedList(new ArrayList<Double>(lastTotalIntensityList));
	}

	@Override
	public void completeCollection() throws Exception {
		if (!isSingleDataFile()) {
			nexusDataWriter.completeCollection();
		} else {
//			while (isStillHaveDataToWrite()) {
//				Sleep.sleep(100);
//			}
			regionDataList.clear();
			lastRegionDataList.clear();
			copyofregiondatalist.clear();
			totalIntensity.clear();
			lastTotalIntensityList.clear();
			copyoftotalintensity.clear();

			called=true;
		}
	}

	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();
	}

	@Override
	public void stop() throws Exception {
		Thread mythread=collectionThread;
		collectionThread=null;
		if (mythread!=null) {
			mythread.interrupt();
		}
		// close fast shutter according to beam used
		if (softXRayFastShutter != null) {
			softXRayFastShutter.moveTo("In");
		}
		if (hardXRayFastShutter != null) {
			hardXRayFastShutter.moveTo("In");
		}
		getAnalyser().stop();
		// waitWhileBusy();
		completeCollection();
	}

	@Override
	public List<String> getInputStreamNames() {
		//TODO why this being called 7 time in a scan start
		return getRegionNames();
	}
	public Vector<String> getRegionNames() {
		Vector<String> extraNames = new Vector<String>();
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
		for (Region region : sequence.getRegion()) {
			if (region.isEnabled()) {
				formats.add("%f");
			}
		}
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)	throws NoSuchElementException, InterruptedException,
			DeviceException {
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		if (!isSingleDataFile()) {
			if (getRegionNames().size() != extraValues.size()) {
				throw new DeviceException(getName()+" : Names size = "+getRegionNames().size()+" and values size = "+extraValues.size()+" are different.");
			}
			appenders.add(new NXDetectorDataFilenamesAppender(getRegionNames(), extraValues, copyoftotalintensity));
		} else {
			appenders.add(new NXDetectorDataAnalyserRegionAppender(copyofregiondatalist, copyoftotalintensity));
			called=true;
		}
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
		if (!isSingleDataFile()) {
			if (nexusDataWriter== null) {
				createDataWriter(scannumber);
			}
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
			Thread.sleep(500);
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
			// fix the EPICS IOC issue - excitation energy does not update in EPICS during energy scan
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
			// fix EPICS does not support BINDING mode bug
			String literal = region.getEnergyMode().getLiteral();
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
				getAnalyser().setEnergyMode(literal,10.0);
			}
			if (lastregion == region) {
				//only set analyser region when region changed.
				return;
			}
			getAnalyser().setRegionName(region.getName());
			getAnalyser().setCameraMinX(region.getFirstXChannel(), 10.0);
			getAnalyser().setCameraMinY(region.getFirstYChannel(), 10.0);
			getAnalyser().setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel()+1, 10.0);
			getAnalyser().setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel()+1, 10.0);
			getAnalyser().setSlices(region.getSlices(), 10.0);
			getAnalyser().setDetectorMode(region.getDetectorMode().getLiteral(), 10.0);
			getAnalyser().setLensMode(region.getLensMode(), 10.0);
			getAnalyser().setPassEnergy(region.getPassEnergy(), 10.0);
			//Hack to fix EPICS does not support bind energy input values, energy values in EPICS are kinetic energy only
			getAnalyser().setCachedEnergyMode(literal);

			getAnalyser().setEnergyStep(region.getEnergyStep() / 1000.0, 10.0);
			double collectionTime = region.getStepTime();
			getAnalyser().setStepTime(collectionTime, 10.0);
			if (!region.getRunMode().isRepeatUntilStopped()) {
				getAnalyser().setNumberInterations(region.getRunMode().getNumIterations(), 10.0);
				getAnalyser().setImageMode(ImageMode.SINGLE, 10.0);
			} else {
				getAnalyser().setNumberInterations(1000000, 10.0);
				getAnalyser().setImageMode(ImageMode.SINGLE, 10.0);
			}
			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral(), 10.0);
		} catch (Exception e) {
			throw e;
		}
		if (getScriptcontroller()!=null && getScriptcontroller() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getScriptcontroller()).update(getScriptcontroller(), new RegionChangeEvent(region.getRegionId(), region.getName()));
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

	public Scriptcontroller getScriptcontroller() {
		return scriptcontroller;
	}

	public void setScriptcontroller(Scriptcontroller scriptcontroller) {
		this.scriptcontroller = scriptcontroller;
	}

	@Override
	public boolean appendsFilepathStrings() {
		if (!isSingleDataFile()) {
			return true;
		}
		return false;
	}

	@Override
	@Deprecated
	public String getFullFileName() throws Exception {
		return null;
	}

	public boolean isStillHaveDataToWrite() {
		return stillHaveDataToWrite;
	}

	public void setStillHaveDataToWrite(boolean haveRegionDataToWriteOut) {
		this.stillHaveDataToWrite = haveRegionDataToWriteOut;
	}

	public boolean isSingleDataFile() {
		return singleDataFile;
	}

	public void setSingleDataFile(boolean singleDataFile) {
		this.singleDataFile = singleDataFile;
	}

	public Vector<String> getActiveRegionNames() {
		return activeRegionNames;
	}

	public void setActiveRegionNames(Vector<String> activeRegionNames) {
		this.activeRegionNames = activeRegionNames;
	}

	public List<Double> getTotalIntensity() {
		return copyoftotalintensity;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		// TODO Auto-generated method stub
		return false;
	}

	public Scannable getSoftXRayFastShutter() {
		return softXRayFastShutter;
	}

	public void setSoftXRayFastShutter(Scannable softXRayFastShutter) {
		this.softXRayFastShutter = softXRayFastShutter;
	}

	public Scannable getHardXRayFastShutter() {
		return hardXRayFastShutter;
	}

	public void setHardXRayFastShutter(Scannable hardXRayFastShutter) {
		this.hardXRayFastShutter = hardXRayFastShutter;
	}

}
