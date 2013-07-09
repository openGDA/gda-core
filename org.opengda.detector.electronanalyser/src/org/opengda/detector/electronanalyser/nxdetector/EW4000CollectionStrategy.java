package org.opengda.detector.electronanalyser.nxdetector;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataFilenamesAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanInformation;
import gda.util.Sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.nexusformat.NeXusFileInterface;
import org.opengda.detector.electronanalyser.NotSupportedException;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Sequence;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EW4000CollectionStrategy implements NXCollectionStrategyPlugin, IObservable{
	private static final Logger logger = LoggerFactory.getLogger(EW4000CollectionStrategy.class);
	private ObservableComponent oc = new ObservableComponent();
	private AtomicBoolean busy = new AtomicBoolean(false);
	private NexusDataWriterExtension nexusDataWriter;
	private Thread collectionThread;
	private List<String> extraValues=new ArrayList<String>();
	private boolean sourceSelectable=false;
	private double XRaySourceEnergyLimit=2100;
	private Long scannumber;
	private Sequence sequence;
	private AtomicInteger scanDatapoint=new AtomicInteger(0);

	private VGScientaAnalyser analyser;
	private Scannable dcmenergy;
	private Scannable pgmenergy;
	private String name = "ew4000_collection_strategy";
	
	@Override
	public void prepareForCollection(int numberImagesPerCollection,	ScanInformation scanInfo) throws Exception {
		this.scannumber=scanInfo.getScanNumber();
		createDataWriter(scannumber);
	}
	
	public void createDataWriter(Long scannumber) {
		nexusDataWriter = new NexusDataWriterExtension(scannumber);
		extraValues.clear();
	}
	
	@Override
	public void collectData() throws Exception {
		Runnable target = new Runnable() {
			
			@Override
			public void run() {
				if (!extraValues.isEmpty()) {
					extraValues.clear();
				}
				busy.getAndSet(true);
				for (Region region : sequence.getRegion()) {
					if(Thread.currentThread().isInterrupted()) break;
					if (region.isEnabled()) {
						try {
							configureAnalyser(region);
							NeXusFileInterface file = nexusDataWriter.createFile(region.getName(), sequence);
							getAnalyser().setNexusFile(file);
							getAnalyser().collectData();
							getAnalyser().waitWhileBusy();
							extraValues.add(getAnalyser().writeOut(scanDatapoint.get()));
						} catch (InterruptedException e) {
							try {
								getAnalyser().stop();
								extraValues.add(getAnalyser().writeOut(scanDatapoint.get()));
								//ensure size matches with number of active regions
								while (extraValues.size()<getNumberOfActiveRegions()) {
									extraValues.add("");
								}
							} catch (DeviceException e1) {
								logger.error("failed to stop the analyser acquisition on interrupt.", e1);
							}
						} catch (Exception e) {
							logger.error("Set new region to detector failed", e);
						}
					}
				}
				busy.getAndSet(false);
				scanDatapoint.incrementAndGet();
			}
		};
		collectionThread=new Thread(target, "ew4000collectionstrategy");
		collectionThread.start();
		
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
//			if (literal.equalsIgnoreCase("Binding")) {
//				// a hack to solve EPICS cannot do binding energy issue, should be removed once EPICS issue solved.
//				if (region.getExcitationEnergy()<getXRaySourceEnergyLimit()) {
//					getAnalyser().setStartEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getHighEnergy(), 1.0);
//					getAnalyser().setEndEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getLowEnergy(), 1.0);
//					getAnalyser().setCentreEnergy(Double.parseDouble(getPgmenergy().getPosition().toString())-region.getFixEnergy(), 1.0);
//				} else {
//					getAnalyser().setStartEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getHighEnergy(), 1.0);
//					getAnalyser().setEndEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getLowEnergy(), 1.0);
//					getAnalyser().setCentreEnergy(Double.parseDouble(getDcmenergy().getPosition().toString())*1000-region.getFixEnergy(), 1.0);
//				}
//				getAnalyser().setEnergysMode("Kinetic",1.0);
//			} else {
				getAnalyser().setStartEnergy(region.getLowEnergy(), 1.0);
				getAnalyser().setEndEnergy(region.getHighEnergy(), 1.0);
				getAnalyser().setCentreEnergy(region.getFixEnergy(), 1.0);
//			}
			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral(), 1.0);
			getAnalyser().setEnergyStep(region.getEnergyStep() / 1000.0, 1.0);
			double collectionTime = region.getStepTime();
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

	@Override
	public void completeCollection() throws Exception {
		nexusDataWriter.completeCollection();
		nexusDataWriter=null;
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
		// specify extraName format
		for (Region region : sequence.getRegion()) {
			if (region.isEnabled()) {
				formats.add("%s");
			}
		}
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)	throws NoSuchElementException, InterruptedException,
			DeviceException {
		if (getInputStreamNames().size() != extraValues.size()) {
			throw new DeviceException(getName()+" : Names and values size are different.");
		}
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDataFilenamesAppender(getInputStreamNames(), extraValues));
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

}
