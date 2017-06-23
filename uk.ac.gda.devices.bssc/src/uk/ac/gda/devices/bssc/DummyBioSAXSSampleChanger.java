package uk.ac.gda.devices.bssc;

import org.embl.BaseException;
import org.embl.ThreadTools;
import org.embl.bssc.scDevSamplePath.FocusPosition;
import org.embl.bssc.scPrefs.SampleType;
import org.embl.bssc.scPrefs.ViscosityLevel;
import org.embl.ctrl.State;
import org.embl.net.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FactoryException;

public class DummyBioSAXSSampleChanger extends BioSAXSSampleChanger {
	private static final Logger logger = LoggerFactory.getLogger(DummyBioSAXSSampleChanger.class);

	private int port;
	private String hostname;
	public static final boolean DUMMY = true;

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public void configure() throws FactoryException {
	}

	@Override
	public State getState() throws BaseException {
		return State.ON;
	}

	@Override
	public void waitReady() throws BaseException {
		waitReady(60000);
	}

	@Override
	public void waitReady(int timeout) throws BaseException {
		long start = System.currentTimeMillis();
		while (true) {
			if (isReady())
				return;
			if ((System.currentTimeMillis() - start) > timeout) {
				throw new BaseException("Timeout waiting application ready");
			}
			ThreadTools.sleep(100);
		}
	}

	@Override
	public boolean isReady() throws BaseException {
		State state = getState();
		switch (state.getID()) {
		case State.ID_RUNNING:
		case State.ID_MOVING:
		case State.ID_INIT:
		case State.ID_BUSY:
			return false;
		default:
			return true;
		}
	}

	@Override
	public void onEvent(Event event) {
		logger.info("RECEIVED EVENT: " + event.toString());
	}

	@Override
	public void abort() throws BaseException {
	}

	@Override
	public int calibrate() throws BaseException {
		return 0;
	}

	@Override
	public String checkTaskResult(int arg0) throws BaseException {
		return "complete";
	}

	@Override
	public int clean() throws BaseException {
		return 0;
	}

	@Override
	public boolean detectCapillary() throws BaseException {
		return true;
	}

	@Override
	public int dry(double arg0) throws BaseException {
		return 0;
	}

	@Override
	public int fill(int arg0, int arg1, int arg2, double arg3) throws BaseException {
		return 0;
	}

	@Override
	public int flow(double arg0, double arg1) throws BaseException {
		return 0;
	}

	@Override
	public int flowAll(double arg0) throws BaseException {
		return 0;
	}

	@Override
	public void forceEnablePlate(int arg0) throws BaseException {
	}

	@Override
	public String[] getAlarmList() throws BaseException {
		return new String[] {};
	}

	@Override
	public String getBeamLocation() throws BaseException {
		return "location";
	}

	@Override
	public double getBeamMarkVolume() throws BaseException {
		return 0;
	}

	@Override
	public boolean getCleanVenturiOK() throws BaseException {
		return true;
	}

	@Override
	public boolean getCollisionDetected() throws BaseException {
		return false;
	}

	@Override
	public String getCommandException() throws BaseException {
		return "command exception";
	}

	@Override
	public String getCommandOutput() throws BaseException {
		return "command output";
	}

	@Override
	public boolean getCoverOpen() throws BaseException {
		return false;
	}

	@Override
	public int[] getCurrentLiquidPosition() throws BaseException {
		return new int[] {};
	}

	@Override
	public boolean getDetergentEmpty() throws BaseException {
		return false;
	}

	@Override
	public int getDetergentLevel() throws BaseException {
		return 0;
	}

	@Override
	public double getEffectiveLoadedVolume(double arg0, ViscosityLevel arg1) throws BaseException {
		return 0;
	}

	@Override
	public boolean getEnablePlateBarcodeScan() throws BaseException {
		return false;
	}

	@Override
	public boolean getEnableSpectrometer() throws BaseException {
		return true;
	}

	@Override
	public boolean getEnableVolumeDetectionInWell() throws BaseException {
		return true;
	}

	@Override
	public boolean getFlooding() throws BaseException {
		return false;
	}

	@Override
	public FocusPosition getFocusPosition() throws BaseException {
		return FocusPosition.middle;
	}

	@Override
	public boolean getHardwareInitPending() throws BaseException {
		return false;
	}

	@Override
	public byte[] getImageJPG() throws BaseException {
		return new byte[] {};
	}

	@Override
	public String[] getLastTaskInfo() throws BaseException {
		return new String[] { "last", "task", "info" };
	}

	@Override
	public int getLightLevel() throws BaseException {
		return 0;
	}

	@Override
	public boolean getLiquidPositionFixed() throws BaseException {
		return true;
	}

	@Override
	public boolean getLocalLockout() throws BaseException {
		return false;
	}

	@Override
	public boolean getOverflowVenturiOK() throws BaseException {
		return true;
	}

	@Override
	public State getPLCState() throws BaseException {
		return State.ON;
	}

	@Override
	public double[] getPlateInfo(int arg0) throws BaseException {
		return new double[] { 4, 5, 6 };
	}

	@Override
	public double[] getPlateTypeInfo(String arg0) throws BaseException {
		return new double[] { 1, 2, 3 };
	}

	@Override
	public String[] getPlatesIDs() throws BaseException {
		return new String[] { "A", "B", "C" };
	}

	@Override
	public boolean getPower12OK() throws BaseException {
		return false;
	}

	@Override
	public double getSamplePathDeadVolume() throws BaseException {
		return 0;
	}

	@Override
	public SampleType getSampleType() throws BaseException {
		return SampleType.green;
	}

	@Override
	public double getSampleVolumeWell() throws BaseException {
		return 0;
	}

	@Override
	public double getSpectrometerDarkReadout() throws BaseException {
		return 0;
	}

	@Override
	public double getSpectrometerReadout() throws BaseException {
		return 0;
	}

	@Override
	public double getSpectrometerRealPathLenght() throws BaseException {
		return 0;
	}

	@Override
	public String getStatus() throws BaseException {
		return "bsscStatus";
	}

	@Override
	public String[] getTaskInfo(int arg0) throws BaseException {
		return new String[] {};
	}

	@Override
	public double getTemperatureSEU() throws BaseException {
		return 273.0;
	}

	@Override
	public double getTemperatureSEUSetpoint() throws BaseException {
		return 273.0;
	}

	@Override
	public double getTemperatureSampleStorage() throws BaseException {
		return 293.0;
	}

	@Override
	public double getTemperatureSampleStorageSetpoint() throws BaseException {
		return 293.0;
	}

	@Override
	public String getUptime() throws BaseException {
		return "17days";
	}

	@Override
	public boolean getVacuumOK() throws BaseException {
		return true;
	}

	@Override
	public String getVersion() throws BaseException {
		return "v1.0";
	}

	@Override
	public ViscosityLevel getViscosityLevel() throws BaseException {
		return ViscosityLevel.medium;
	}

	@Override
	public boolean getWasteFull() throws BaseException {
		return false;
	}

	@Override
	public int getWasteLevel() throws BaseException {
		return 0;
	}

	@Override
	public boolean getWaterEmpty() throws BaseException {
		return false;
	}

	@Override
	public int getWaterLevel() throws BaseException {
		return 0;
	}

	@Override
	public int getWellLiquidVolume(int arg0, int arg1, int arg2) throws BaseException {
		return 0;
	}

	@Override
	public double getWellVolume(int arg0, int arg1, int arg2) throws BaseException {
		return 0;
	}

	@Override
	public boolean isTaskRunning(int arg0) throws BaseException {
		return false;
	}

	@Override
	public int loadPlates() throws BaseException {
		return 0;
	}

	@Override
	public int measureConcentration(int arg0, int arg1, int arg2) throws BaseException {
		return 0;
	}

	@Override
	public int mix(int arg0, int arg1, int arg2, double arg3, int arg4) throws BaseException {
		return 0;
	}

	@Override
	public void moveSyringeBackward(double arg0) throws BaseException {
	}

	@Override
	public void moveSyringeForward(double arg0) throws BaseException {
	}

	@Override
	public void onExposureCellFilled() throws BaseException {
	}

	@Override
	public int pull(double arg0, double arg1) throws BaseException {
		return 0;
	}

	@Override
	public int push(double arg0, double arg1) throws BaseException {
		return 0;
	}

	@Override
	public int recuperate(int arg0, int arg1, int arg2) throws BaseException {
		return 0;
	}

	@Override
	public void restart(boolean arg0) throws BaseException {
	}

	@Override
	public int scanAndPark() throws BaseException {
		return 0;
	}

	@Override
	public void setBeamLocation(String arg0) throws BaseException {
	}

	@Override
	public void setBeamShapeEllipse(boolean arg0) throws BaseException {
	}

	@Override
	public void setEnablePlateBarcodeScan(boolean arg0) throws BaseException {
	}

	@Override
	public void setEnableSpectrometer(boolean arg0) throws BaseException {
	}

	@Override
	public void setEnableVolumeDetectionInWell(boolean arg0) throws BaseException {
	}

	@Override
	public void setFocusPosition(FocusPosition arg0) throws BaseException {
	}

	@Override
	public void setLightLevel(int arg0) throws BaseException {
	}

	@Override
	public void setLiquidPositionFixed(boolean arg0) throws BaseException {
	}

	@Override
	public void setLocalLockout(boolean arg0) throws BaseException {
	}

	@Override
	public void setSamplePathDeadVolume(double arg0) throws BaseException {
	}

	@Override
	public void setSampleType(String arg0) throws BaseException {
	}

	@Override
	public void setSampleType(SampleType arg0) throws BaseException {
	}

	@Override
	public void setTemperatureSEU(double arg0) throws BaseException {
	}

	@Override
	public void setTemperatureSEUSetpoint(double arg0) throws BaseException {
	}

	@Override
	public void setTemperatureSampleStorage(double arg0) throws BaseException {
	}

	@Override
	public void setTemperatureSampleStorageSetpoint(double arg0) throws BaseException {
	}

	@Override
	public void setViscosityLevel(String arg0) throws BaseException {
	}

	@Override
	public void setViscosityLevel(ViscosityLevel arg0) throws BaseException {
	}

	@Override
	public void stopSyringe() throws BaseException {
	}

	@Override
	public int transfer(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, double arg6) throws BaseException {
		return 0;
	}

	@Override
	public int waitTemperatureSEU(double arg0) throws BaseException {
		return 0;
	}

	@Override
	public int waitTemperatureSample(double arg0) throws BaseException {
		return 0;
	}
}