package org.opengda.detector.electronanalyser.scan;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.Sleep;

import java.util.concurrent.atomic.AtomicInteger;

import org.opengda.detector.electronanalyser.NotSupportedException;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.nxdetector.plugins.ADArrayPlugin;
import org.opengda.detector.electronanalyser.nxdetector.plugins.PVArrayPlugin;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CorbaImplClass(DeviceImpl.class)
@CorbaAdapterClass(DeviceAdapter.class)
public class RegionScannable extends ScannableBase implements Scannable {
	private ObservableComponent oc = new ObservableComponent();
	private Region region;
	private String name;
	private VGScientaAnalyser analyser;
	private ADArrayPlugin adArray;
	private PVArrayPlugin pvArray;
	private Scannable dcmenergy;
	private Scannable pgmenergy;
	private boolean sourceSelectable=false;
	private double XRaySourceEnergyLimit=2100;

	// private Scriptcontroller scriptController;
	private boolean busy;
	private AtomicInteger count=new AtomicInteger(); // enabled region position count
	private boolean firstInScan;
	private Scriptcontroller scriptController;
	private static final Logger logger = LoggerFactory
			.getLogger(RegionScannable.class);

	public RegionScannable() {
		setInputNames(new String[] {"region_number"});
		// scriptController=Finder.getInstance().find("SequenceFileObserver");
		//setOutputFormat(new String[] {"%s"});
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return busy;
	}

	@Override
	public Object getPosition() throws DeviceException {
//		if (region == null) {
//			// no poistion is not setted by GDA Scan
//			try {
//				return getPositionFromEPICS();
//			} catch (Exception e) {
//				logger.error("Cannot get region parameters from EPICS IOC.", e);
//				throw new DeviceException(
//						"Cannot get region parameters from EPICS IOC.", e);
//			}
//			// return
//			// "No region is set by default in this scannable object. It will be set dynamically during a analyserscan";
//		}
//		return region;
		return count.intValue(); // return the position number of active regions in the sequence
	}

	private String getPositionFromEPICS() throws Exception {
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (Current region parameters in EPICS IOC for the analyser: "); //$NON-NLS-1$
		result.append(", lensMode: "); //$NON-NLS-1$
		result.append(getAnalyser().getLensMode());
		result.append(", passEnergy: "); //$NON-NLS-1$
		result.append(getAnalyser().getPassEnergy());
		result.append(", acquisitionMode: "); //$NON-NLS-1$
		result.append(getAnalyser().getAcquisitionMode());
		result.append(", energyMode: "); //$NON-NLS-1$
		String energysMode = getAnalyser().getEnergyMode();
		result.append(energysMode);
		if (energysMode.equalsIgnoreCase("Fixed")) {
			result.append(", fixEnergy: "); //$NON-NLS-1$
			result.append(getAnalyser().getCentreEnergy());
		} else {
			result.append(", lowEnergy: "); //$NON-NLS-1$
			result.append(getAnalyser().getStartEnergy());
			result.append(", highEnergy: "); //$NON-NLS-1$
			result.append(getAnalyser().getEndEnergy());
		}
		result.append(", energyStep: "); //$NON-NLS-1$
		result.append(getAnalyser().getEnergyStep());
		result.append(", stepTime: "); //$NON-NLS-1$
		result.append(getAnalyser().getCollectionTime());
		result.append(", firstXChannel: "); //$NON-NLS-1$
		result.append(getAnalyser().getAdBase().getMinX_RBV());
		result.append(", lastXChannel: "); //$NON-NLS-1$
		result.append(getAnalyser().getAdBase().getMaxSizeX_RBV()
				+ getAnalyser().getAdBase().getMinX_RBV());
		result.append(", firstYChannel: "); //$NON-NLS-1$
		result.append(getAnalyser().getAdBase().getMinY_RBV());
		result.append(", lastYChannel: "); //$NON-NLS-1$
		result.append(getAnalyser().getAdBase().getMaxSizeY_RBV()
				+ getAnalyser().getAdBase().getMinY_RBV());
		result.append(", slices: "); //$NON-NLS-1$
		result.append(getAnalyser().getSlices());
		result.append(", detectorMode: "); //$NON-NLS-1$
		result.append(getAnalyser().getDetectorMode());
		result.append(", totalSteps: "); //$NON-NLS-1$
		result.append(getAnalyser().getTotalSteps());
		result.append(", totalTime: "); //$NON-NLS-1$
		result.append(getAnalyser().getTotalSteps()
				* getAnalyser().getCollectionTime());
		result.append(')');
		return result.toString();
	}

	@Override
	@MethodAccessProtected(isProtected = true)
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (position instanceof Region) {
			try {
				region = (Region) position;
				setNewRegion(region);
				adArray.setRegionName(region.getName());
				pvArray.setRegionName(region.getName());
			} catch (Exception e) {
				throw new DeviceException("Set new region to analyser failed.",
						e);
			}
		}
	}

	private void setNewRegion(Region region) throws Exception {
		if (!firstInScan) {
			return;
		}
		try {
			busy = true;
			getAnalyser().setCameraMinX(region.getFirstXChannel()-1, 5.0);
			getAnalyser().setCameraMinY(region.getFirstYChannel()-1, 5.0);
			getAnalyser().setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel()+1, 5.0);
			getAnalyser().setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel()+1, 5.0);
			getAnalyser().setSlices(region.getSlices(), 5.0);
			getAnalyser().setDetectorMode(region.getDetectorMode().getLiteral(), 5.0);
			getAnalyser().setLensMode(region.getLensMode(), 5.0);
			String literal = region.getEnergyMode().getLiteral();
			getAnalyser().setEnergyMode(literal,5.0);
			if (isSourceSelectable()) {
				if (region.getExcitationEnergy()<getXRaySourceEnergyLimit()) {
					getAnalyser().setExcitationEnergy(Double.valueOf(pgmenergy.getPosition().toString()));
				} else {
					getAnalyser().setExcitationEnergy(Double.valueOf(dcmenergy.getPosition().toString())*1000);
				}
			} else {
				getAnalyser().setExcitationEnergy(Double.valueOf(pgmenergy.getPosition().toString()));
			}
			getAnalyser().setPassEnergy(region.getPassEnergy(), 5.0);
			if (literal.equalsIgnoreCase("Binding")) {
				//TODO a hack to solve EPICS cannot do binding energy issue, should be removed once EPICS issue solved.
				if (region.getExcitationEnergy()<getXRaySourceEnergyLimit()) {
					getAnalyser().setStartEnergy(Double.parseDouble(pgmenergy.getPosition().toString())-region.getHighEnergy(), 5.0);
					getAnalyser().setEndEnergy(Double.parseDouble(pgmenergy.getPosition().toString())-region.getLowEnergy(), 5.0);
					getAnalyser().setCentreEnergy(Double.parseDouble(pgmenergy.getPosition().toString())-region.getFixEnergy(), 5.0);
				} else {
					getAnalyser().setStartEnergy(Double.parseDouble(dcmenergy.getPosition().toString())*1000-region.getHighEnergy(), 5.0);
					getAnalyser().setEndEnergy(Double.parseDouble(dcmenergy.getPosition().toString())*1000-region.getLowEnergy(), 5.0);
					getAnalyser().setCentreEnergy(Double.parseDouble(dcmenergy.getPosition().toString())*1000-region.getFixEnergy(), 5.0);
				}
				getAnalyser().setEnergyMode("Kinetic",5.0);
			} else {
				getAnalyser().setStartEnergy(region.getLowEnergy(), 5.0);
				getAnalyser().setEndEnergy(region.getHighEnergy(), 5.0);
				getAnalyser().setCentreEnergy(region.getFixEnergy(), 5.0);
			}
			getAdArray().setEnergyMode(literal);
			
			getAnalyser().setStepTime(region.getStepTime(), 5.0);
			getAnalyser().setEnergyStep(region.getEnergyStep()/1000.0, 5.0);
			if (!region.getRunMode().isConfirmAfterEachIteration()) {
				if (!region.getRunMode().isRepeatUntilStopped()) {
					getAnalyser().setNumberInterations(
							region.getRunMode().getNumIterations(), 5.0);
					getAnalyser().setImageMode(ImageMode.SINGLE, 5.0);
				} else {
					getAnalyser().setNumberInterations(1, 5.0);
					getAnalyser().setImageMode(ImageMode.CONTINUOUS, 5.0);
				}
			} else {
				getAnalyser().setNumberInterations(1, 5.0);
				getAnalyser().setImageMode(ImageMode.SINGLE, 5.0);
				throw new NotSupportedException(
						"Confirm after each iteraction is not yet supported");
			}
			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral(), 5.0);
			firstInScan=false;
		} catch (Exception e) {
			throw e;
		} finally {
			busy = false;
		}
		 if (getScriptController()!=null && getScriptController() instanceof ScriptControllerBase) {
		 ((ScriptControllerBase)getScriptController()).update(this, new RegionChangeEvent(region.getRegionId(), region.getName()));
		 }
//		oc.notifyIObservers(this, new RegionChangeEvent(region.getRegionId(), region.getName()));
	}
@Override
public void atScanStart() throws DeviceException {
	firstInScan=true;
	super.atScanStart();
}
	@Override
	public void atScanLineStart() throws DeviceException {
		count.set(0);
		super.atScanLineStart();
	}
	@Override
	public void atPointStart() throws DeviceException {
		count.incrementAndGet();
		super.atPointStart();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		super.atPointEnd();
	}

	@Override
	public void stop() throws DeviceException {
		// oc.notifyIObservers(this,new RegionStatusEvent(region.getRegionId(),
		// Status.ABORTED));
	}

	@Override
	public String[] getInputNames() {
		return super.getInputNames();
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
		return super.getOutputFormat();
	}

	public VGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
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

	public String getRegionName() {
		return region.getName();
	}

	public PVArrayPlugin getPvArray() {
		return pvArray;
	}

	public void setPvArray(PVArrayPlugin pvArray) {
		this.pvArray = pvArray;
	}

	public ADArrayPlugin getAdArray() {
		return adArray;
	}

	public void setAdArray(ADArrayPlugin adArray) {
		this.adArray = adArray;
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

	public Scriptcontroller getScriptController() {
		return scriptController;
	}

	public void setScriptController(Scriptcontroller scriptController) {
		this.scriptController = scriptController;
	}

}
