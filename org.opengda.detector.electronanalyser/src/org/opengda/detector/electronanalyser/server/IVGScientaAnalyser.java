package org.opengda.detector.electronanalyser.server;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.factory.Findable;
import uk.ac.gda.devices.vgscienta.VGScientaController;

public interface IVGScientaAnalyser extends Findable {
	public abstract int getNdarrayXsize() throws Exception;

	public abstract int getNdarrayYsize() throws Exception;

	public abstract AnalyserCapabilities getCapabilities();

	public abstract void setCapabilities(AnalyserCapabilities ac);

	public abstract VGScientaController getController();

	public abstract void setController(VGScientaController controller);

	public abstract int getNumberOfSweeptSteps() throws Exception;

	public abstract double[] getEnergyAxis() throws Exception;

	public abstract double[] getAngleAxis() throws Exception;

	public abstract void setFixedMode(boolean b) throws Exception;

	public abstract int[] getFixedModeRegion();

	public abstract void setFixedModeRegion(int[] fixedModeRegion);

	public abstract double getCollectionTime() throws DeviceException;

	public abstract void setCollectionTime(double collectionTime)
			throws DeviceException;

	public abstract void setNumberInterations(int value) throws Exception;

	public abstract Integer getNumberIterations() throws Exception;

	public abstract void setCameraMinX(int value) throws Exception;

	public abstract int getCameraMinX() throws Exception;

	public abstract void setCameraMinY(int value) throws Exception;

	public abstract int getCameraMinY() throws Exception;

	public abstract void setCameraSizeX(int value) throws Exception;

	public abstract int getCameraSizeX() throws Exception;

	public abstract void setCameraSizeY(int value) throws Exception;

	public abstract void setImageMode(ImageMode imagemode) throws Exception;

	public abstract int getCameraSizeY() throws Exception;

	public abstract void setLensMode(String value) throws Exception;

	public abstract String getLensMode() throws Exception;

	public abstract void setAcquisitionMode(String value) throws Exception;

	public abstract String getAcquisitionMode() throws Exception;

	public abstract void setEnergyMode(String value) throws Exception;

	public abstract String getEnergyMode() throws Exception;

	public abstract void setDetectorMode(String value) throws Exception;

	public abstract String getDetectorMode() throws Exception;

	public abstract void setElement(String value) throws Exception;

	public abstract String getElement() throws Exception;

	public abstract void setPassEnergy(Integer value) throws Exception;

	public abstract Integer getPassEnergy() throws Exception;

	public abstract void setStartEnergy(Double value) throws Exception;

	public abstract Double getStartEnergy() throws Exception;

	public abstract void setCentreEnergy(Double value) throws Exception;

	public abstract Double getCentreEnergy() throws Exception;

	public abstract void setEndEnergy(Double value) throws Exception;

	public abstract Double getEndEnergy() throws Exception;

	public abstract void setEnergyStep(Double value) throws Exception;

	public abstract Double getEnergyStep() throws Exception;

	public abstract void setFrames(Integer value) throws Exception;

	public abstract Integer getFrames() throws Exception;

	public abstract void setStepTime(double value) throws Exception;

	public abstract void setSlices(int value) throws Exception;

	public abstract int getSlices() throws Exception;

	public abstract Integer getTotalSteps() throws Exception;

	public abstract void zeroSupplies() throws Exception;

	public abstract void stop() throws DeviceException;
	public abstract void start() throws Exception;

	public abstract void waitWhileBusy() throws InterruptedException, DeviceException;

	void setCameraMinX(int value, double timeout) throws Exception;

	void setCameraMinY(int value, double timeout) throws Exception;

	void setCameraSizeX(int value, double timeout) throws Exception;

	void setCameraSizeY(int value, double timeout) throws Exception;

	void setSlices(int value, double timeout) throws Exception;

	void setLensMode(String value, double timeout) throws Exception;

	void setDetectorMode(String value, double timeout) throws Exception;

	void setEnergyMode(String value, double timeout) throws Exception;

	void setPassEnergy(Integer value, double timeout) throws Exception;

	void setStartEnergy(Double value, double timeout) throws Exception;

	void setEndEnergy(Double value, double timeout) throws Exception;

	void setCentreEnergy(Double value, double timeout) throws Exception;

	void setStepTime(double value, double timeout) throws Exception;

	void setEnergyStep(Double value, double timeout) throws Exception;

	void setNumberInterations(int value, double timeout) throws Exception;

	void setImageMode(ImageMode imagemode, double timeout) throws Exception;

	void setAcquisitionMode(String value, double timeout) throws Exception;

	public abstract String[] getPassENergies() throws DeviceException;

	public abstract String[] getLensModes() throws DeviceException;

	public double getExcitationEnergy() throws Exception;

	public void setExcitationEnergy(double energy) throws Exception;

	double getStepTime() throws Exception;

	public abstract String[] getElementSet() throws DeviceException;

	public abstract double[] getExtIO(int length) throws Exception;

	public abstract double[] getImage(int i) throws Exception;

	public abstract double[] getSpectrum(int length) throws Exception;

}