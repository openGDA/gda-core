package org.opengda.detector.electronanalyser.server;

import java.util.Set;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.factory.Findable;
import uk.ac.gda.devices.vgscienta.VGScientaController;

public interface IVGScientaAnalyser extends Findable {

	int getNdarrayXsize() throws Exception;

	int getNdarrayYsize() throws Exception;

	VGScientaController getController();

	void setController(VGScientaController controller);

	int getNumberOfSweeptSteps() throws Exception;

	double[] getEnergyAxis() throws Exception;

	double[] getAngleAxis() throws Exception;

	void setFixedMode(boolean b) throws Exception;

	int[] getFixedModeRegion();

	void setFixedModeRegion(int[] fixedModeRegion);

	double getCollectionTime() throws DeviceException;

	void setCollectionTime(double collectionTime)
			throws DeviceException;

	void setNumberInterations(int value) throws Exception;

	Integer getNumberIterations() throws Exception;

	void setCameraMinX(int value) throws Exception;

	int getCameraMinX() throws Exception;

	void setCameraMinY(int value) throws Exception;

	int getCameraMinY() throws Exception;

	void setCameraSizeX(int value) throws Exception;

	int getCameraSizeX() throws Exception;

	void setCameraSizeY(int value) throws Exception;

	void setImageMode(ImageMode imagemode) throws Exception;

	int getCameraSizeY() throws Exception;

	void setLensMode(String value) throws Exception;

	String getLensMode() throws Exception;

	void setAcquisitionMode(String value) throws Exception;

	String getAcquisitionMode() throws Exception;

	void setEnergyMode(String value) throws Exception;

	String getEnergyMode() throws Exception;

	void setDetectorMode(String value) throws Exception;

	String getDetectorMode() throws Exception;

	void setPsuMode(String value) throws Exception;

	String getPsuMode() throws Exception;

	void setPassEnergy(Integer value) throws Exception;

	Integer getPassEnergy() throws Exception;

	void setStartEnergy(Double value) throws Exception;

	Double getStartEnergy() throws Exception;

	void setCentreEnergy(Double value) throws Exception;

	Double getCentreEnergy() throws Exception;

	void setEndEnergy(Double value) throws Exception;

	Double getEndEnergy() throws Exception;

	void setEnergyStep(Double value) throws Exception;

	Double getEnergyStep() throws Exception;

	void setFrames(Integer value) throws Exception;

	Integer getFrames() throws Exception;

	void setStepTime(double value) throws Exception;

	void setSlices(int value) throws Exception;

	int getSlices() throws Exception;

	Integer getTotalSteps() throws Exception;

	void zeroSupplies() throws Exception;

	void stop() throws DeviceException;

	void start() throws Exception;

	void waitWhileBusy() throws InterruptedException, DeviceException;

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

	String[] getPassENergies() throws DeviceException;

	String[] getLensModes() throws DeviceException;

	public double getExcitationEnergy() throws Exception;

	public void setExcitationEnergy(double energy) throws Exception;

	double getStepTime() throws Exception;

	Set<String> getPsuModes() throws DeviceException;

	double[] getExtIO(int length) throws Exception;

	double[] getImage(int i) throws Exception;

	double[] getSpectrum(int length) throws Exception;
}