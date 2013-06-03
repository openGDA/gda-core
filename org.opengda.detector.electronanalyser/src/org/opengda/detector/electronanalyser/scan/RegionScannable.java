package org.opengda.detector.electronanalyser.scan;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import org.opengda.detector.electronanalyser.NotSupportedException;
import org.opengda.detector.electronanalyser.event.RegionChangeEvent;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class RegionScannable extends ScannableBase implements Scannable {
	private ObservableComponent oc = new ObservableComponent();
	private Region region;
	private String name;
	private VGScientaAnalyser analyser;
	// private Scriptcontroller scriptController;
	private boolean busy;
	private static final Logger logger = LoggerFactory
			.getLogger(RegionScannable.class);

	public RegionScannable() {
		// scriptController=Finder.getInstance().find("SequenceFileObserver");
		setOutputFormat(new String[] {"%s"});
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return busy;
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (region == null) {
			// no poistion is not setted by GDA Scan
			try {
				return getPositionFromEPICS();
			} catch (Exception e) {
				logger.error("Cannot get region parameters from EPICS IOC.", e);
				throw new DeviceException(
						"Cannot get region parameters from EPICS IOC.", e);
			}
			// return
			// "No region is set by default in this scannable object. It will be set dynamically during a analyserscan";
		}
		return region;
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
		String energysMode = getAnalyser().getEnergysMode();
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
			} catch (Exception e) {
				throw new DeviceException("Set new region to analyser failed.",
						e);
			}
		}
	}

	private void setNewRegion(Region region) throws Exception {
		try {
			busy = true;
			getAnalyser().setCameraMinX(region.getFirstXChannel(), 1.0);
			getAnalyser().setCameraMinY(region.getFirstYChannel(), 1.0);
			getAnalyser().setCameraSizeX(
					region.getLastXChannel() - region.getFirstXChannel(), 1.0);
			getAnalyser().setCameraSizeY(
					region.getLastYChannel() - region.getFirstYChannel(), 1.0);
			getAnalyser().setSlices(region.getSlices(), 1.0);
			getAnalyser().setDetectorMode(
					region.getDetectorMode().getLiteral(), 1.0);

			getAnalyser().setLensMode(region.getLensMode(), 1.0);
			getAnalyser().setEnergysMode(region.getEnergyMode().getLiteral(),
					1.0);
			getAnalyser().setPassEnergy(region.getPassEnergy(), 1.0);
			if (region.getAcquisitionMode() == ACQUISITION_MODE.SWEPT) {
				getAnalyser().setStartEnergy(region.getLowEnergy(), 1.0);
				getAnalyser().setEndEnergy(region.getHighEnergy(), 1.0);
			} else {
				getAnalyser().setCentreEnergy(region.getFixEnergy(), 1.0);
			}
			getAnalyser().setStepTime(region.getStepTime(), 1.0);
			getAnalyser().setEnergyStep(region.getEnergyStep() / 1000.0, 1.0);
			if (!region.getRunMode().isConfirmAfterEachIteration()) {
				if (!region.getRunMode().isRepeatUntilStopped()) {
					getAnalyser().setNumberInterations(
							region.getRunMode().getNumIterations(), 1.0);
					getAnalyser().setImageMode(ImageMode.SINGLE, 1.0);
				} else {
					getAnalyser().setNumberInterations(1, 1.0);
					getAnalyser().setImageMode(ImageMode.CONTINUOUS, 1.0);
				}
			} else {
				getAnalyser().setNumberInterations(1, 1.0);
				getAnalyser().setImageMode(ImageMode.SINGLE, 1.0);
				throw new NotSupportedException(
						"Confirm after each iteraction is not yet supported");
			}
			getAnalyser().setAcquisitionMode(
					region.getAcquisitionMode().getLiteral(), 1.0);
		} catch (Exception e) {
			throw e;
		} finally {
			busy = false;
		}
		// if (scriptController!=null) {
		// ((ScriptControllerBase)scriptController).update(this, new
		// RegionChangeEvent(region.getRegionId()));
		// }
		oc.notifyIObservers(this, new RegionChangeEvent(region.getRegionId()));
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

}
