package org.opengda.detector.electronanalyser.scan;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.scannable.ScannableBase;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import org.opengda.detector.electronanalyser.NotSupportedException;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;

public class RegionScannable extends ScannableBase implements Scannable {
	private ObservableComponent oc=new ObservableComponent();
	private Region region;
	private String name;
	private IVGScientaAnalyser analyser;
	private boolean busy;
	public RegionScannable() {
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return busy;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return region;
	}

	@Override
	@MethodAccessProtected(isProtected = true)
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (position instanceof Region) {
			try {
				region=(Region)position;
				setNewRegion(region);
			} catch (Exception e) {
				throw new DeviceException("Set new region to analyser failed.", e);
			}
		}
	}

	private void setNewRegion(Region region) throws Exception{
		try {
			busy=true;
			getAnalyser().setCameraMinX(region.getFirstXChannel());
			getAnalyser().setCameraMinY(region.getFirstYChannel());
			getAnalyser().setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel());
			getAnalyser().setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel());
			getAnalyser().setSlices(region.getSlices());
			getAnalyser().setDetectorMode(region.getDetectorMode().getLiteral());
			
			getAnalyser().setLensMode(region.getLensMode());
			getAnalyser().setEnergysMode(region.getEnergyMode().getLiteral());
			getAnalyser().setPassEnergy(region.getPassEnergy());
			if (region.getAcquisitionMode() == ACQUISITION_MODE.SWEPT) {
				getAnalyser().setStartEnergy(region.getLowEnergy());
				getAnalyser().setEndEnergy(region.getHighEnergy());
			} else {
				getAnalyser().setCentreEnergy(region.getFixEnergy());
			}
			getAnalyser().setStepTime(region.getStepTime());
			getAnalyser().setEnergyStep(region.getEnergyStep() / 1000.0);
			if (!region.getRunMode().isConfirmAfterEachIteration()) {
				if (!region.getRunMode().isRepeatUntilStopped()) {
					getAnalyser().setNumberInterations(region.getRunMode().getNumIterations());
					getAnalyser().setImageMode(ImageMode.SINGLE); 
				} else {
					getAnalyser().setNumberInterations(1);
					getAnalyser().setImageMode(ImageMode.CONTINUOUS);
				}
			} else {
				getAnalyser().setNumberInterations(1);
				getAnalyser().setImageMode(ImageMode.SINGLE);
				throw new NotSupportedException("Confirm after each iteraction is not yet supported");
			}
			getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral());
		} catch (Exception e) {
			throw e;
		} finally {
			busy=false;
		}
		notifyObservers(region);
	}

	
	private void notifyObservers(Region region) {
		oc.notifyIObservers(this, region);		
	}
	
	@Override
	public void stop() throws DeviceException {
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

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();		
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
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
	public String toFormattedString() {
		return region.toString();
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}
	@Override
	public void setName(String name) {
		this.name=name;
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
