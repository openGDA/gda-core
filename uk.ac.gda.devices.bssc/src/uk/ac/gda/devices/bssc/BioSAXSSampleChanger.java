package uk.ac.gda.devices.bssc;

/*
 * Project: BSSC - BioSAXS Sample Changer Date Author Changes 01.07.09 Gobbo Created Copyright 2009 by European
 * Molecular Biology Laboratory - Grenoble
 */

import gda.device.DeviceBase;
import gda.factory.Configurable;
import gda.factory.FactoryException;

import org.embl.BaseException;
import org.embl.ThreadTools;
import org.embl.bssc.scDevSamplePath.FocusPosition;
import org.embl.bssc.scPrefs.SampleType;
import org.embl.bssc.scPrefs.ViscosityLevel;
import org.embl.bssc.scServerInterface;
import org.embl.ctrl.State;
import org.embl.net.Event;
import org.embl.net.ExporterClient;
import org.embl.net.TransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A BSSC Java client implementation example for remote controlling the Sample Changer.
 * 
 * @author <a href="mailto:alexgobbo@gmail.com">Alexandre Gobbo</a>
 */
public class BioSAXSSampleChanger extends DeviceBase implements scServerInterface, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(BioSAXSSampleChanger.class);

	private int port = 9555;
	private String hostname = "localhost";
	private scServerInterface proxy = null;
	private ExporterClient ec = null;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public void configure() throws FactoryException {
		if (ec != null) {
			ec.disconnect();
			ec.dispose();
		}
		ec = new ExporterClient(hostname, port, TransportProtocol.STREAM, 2, 4000);
		proxy = (scServerInterface) (ec.getProxy(scServerInterface.class));
	}

	@Override
	public State getState() throws BaseException {
		String ret = ec.execute("getState");
		return State.fromString(ret);
	}

	public void waitReady() throws BaseException {
		waitReady(60000);
	}

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
	
	public void onEvent(Event event) {
		logger.info("RECEIVED EVENT: " + event.toString());
	}

	@Override
	public void abort() throws BaseException {
		proxy.abort();
	}

	@Override
	public int calibrate() throws BaseException {
		return proxy.calibrate();
	}

	@Override
	public String checkTaskResult(int arg0) throws BaseException {
		return proxy.checkTaskResult(arg0);
	}

	@Override
	public int clean() throws BaseException {
		return proxy.clean();
	}

	@Override
	public boolean detectCapillary() throws BaseException {
		return proxy.detectCapillary();
	}

	@Override
	public int dry(double arg0) throws BaseException {
		return proxy.dry(arg0);
	}

	@Override
	public int fill(int arg0, int arg1, int arg2, double arg3) throws BaseException {
		return proxy.fill(arg0, arg1, arg2, arg3);
	}

	@Override
	public int flow(double arg0, double arg1) throws BaseException {
		return proxy.flow(arg0, arg1);
	}

	@Override
	public int flowAll(double arg0) throws BaseException {
		return proxy.flowAll(arg0);
	}

	@Override
	public void forceEnablePlate(int arg0) throws BaseException {
		proxy.forceEnablePlate(arg0);
	}

	@Override
	public String[] getAlarmList() throws BaseException {
		return proxy.getAlarmList();
	}

	@Override
	public String getBeamLocation() throws BaseException {
		return proxy.getBeamLocation();
	}

	@Override
	public double getBeamMarkVolume() throws BaseException {
		return proxy.getBeamMarkVolume();
	}

	@Override
	public boolean getCleanVenturiOK() throws BaseException {
		return proxy.getCleanVenturiOK();
	}

	@Override
	public boolean getCollisionDetected() throws BaseException {
		return proxy.getCollisionDetected();
	}

	@Override
	public String getCommandException() throws BaseException {
		return proxy.getCommandException();
	}

	@Override
	public String getCommandOutput() throws BaseException {
		return proxy.getCommandOutput();
	}

	@Override
	public boolean getCoverOpen() throws BaseException {
		return proxy.getCoverOpen();
	}

	@Override
	public int[] getCurrentLiquidPosition() throws BaseException {
		return proxy.getCurrentLiquidPosition();
	}

	@Override
	public boolean getDetergentEmpty() throws BaseException {
		return proxy.getDetergentEmpty();
	}

	@Override
	public int getDetergentLevel() throws BaseException {
		return proxy.getDetergentLevel();
	}

	@Override
	public double getEffectiveLoadedVolume(double arg0, ViscosityLevel arg1) throws BaseException {
		return proxy.getEffectiveLoadedVolume(arg0, arg1);
	}

	@Override
	public boolean getEnablePlateBarcodeScan() throws BaseException {
		return proxy.getEnablePlateBarcodeScan();
	}

	@Override
	public boolean getEnableSpectrometer() throws BaseException {
		return proxy.getEnableSpectrometer();
	}

	@Override
	public boolean getEnableVolumeDetectionInWell() throws BaseException {
		return proxy.getEnableVolumeDetectionInWell();
	}

	@Override
	public boolean getFlooding() throws BaseException {
		return proxy.getFlooding();
	}

	@Override
	public FocusPosition getFocusPosition() throws BaseException {
		return proxy.getFocusPosition();
	}

	@Override
	public boolean getHardwareInitPending() throws BaseException {
		return proxy.getHardwareInitPending();
	}

	@Override
	public byte[] getImageJPG() throws BaseException {
		return proxy.getImageJPG();
	}

	@Override
	public String[] getLastTaskInfo() throws BaseException {
		return proxy.getLastTaskInfo();
	}

	@Override
	public int getLightLevel() throws BaseException {
		return proxy.getLightLevel();
	}

	@Override
	public boolean getLiquidPositionFixed() throws BaseException {
		return proxy.getLiquidPositionFixed();
	}

	@Override
	public boolean getLocalLockout() throws BaseException {
		return proxy.getLocalLockout();
	}

	@Override
	public boolean getOverflowVenturiOK() throws BaseException {
		return proxy.getOverflowVenturiOK();
	}

	@Override
	public State getPLCState() throws BaseException {
		return proxy.getPLCState();
	}

	@Override
	public double[] getPlateInfo(int arg0) throws BaseException {
		return proxy.getPlateInfo(arg0);
	}

	@Override
	public double[] getPlateTypeInfo(String arg0) throws BaseException {
		return proxy.getPlateTypeInfo(arg0);
	}

	@Override
	public String[] getPlatesIDs() throws BaseException {
		return proxy.getPlatesIDs();
	}

	@Override
	public boolean getPower12OK() throws BaseException {
		return proxy.getPower12OK();
	}

	@Override
	public double getSamplePathDeadVolume() throws BaseException {
		return proxy.getSamplePathDeadVolume();
	}

	@Override
	public SampleType getSampleType() throws BaseException {
		return proxy.getSampleType();
	}

	@Override
	public double getSampleVolumeWell() throws BaseException {
		return proxy.getSampleVolumeWell();
	}

	@Override
	public double getSpectrometerDarkReadout() throws BaseException {
		return proxy.getSpectrometerDarkReadout();
	}

	@Override
	public double getSpectrometerReadout() throws BaseException {
		return proxy.getSpectrometerReadout();
	}

	@Override
	public double getSpectrometerRealPathLenght() throws BaseException {
		return proxy.getSpectrometerRealPathLenght();
	}

	@Override
	public String getStatus() throws BaseException {
		return proxy.getStatus();
	}

	@Override
	public String[] getTaskInfo(int arg0) throws BaseException {
		return proxy.getTaskInfo(arg0);
	}

	@Override
	public double getTemperatureSEU() throws BaseException {
		return proxy.getTemperatureSEU();
	}

	@Override
	public double getTemperatureSEUSetpoint() throws BaseException {
		return proxy.getTemperatureSEUSetpoint();
	}

	@Override
	public double getTemperatureSampleStorage() throws BaseException {
		return proxy.getTemperatureSampleStorage();
	}

	@Override
	public double getTemperatureSampleStorageSetpoint() throws BaseException {
		return proxy.getTemperatureSampleStorageSetpoint();
	}

	@Override
	public String getUptime() throws BaseException {
		return proxy.getUptime();
	}

	@Override
	public boolean getVacuumOK() throws BaseException {
		return proxy.getVacuumOK();
	}

	@Override
	public String getVersion() throws BaseException {
		return proxy.getVersion();
	}

	@Override
	public ViscosityLevel getViscosityLevel() throws BaseException {
		return proxy.getViscosityLevel();
	}

	@Override
	public boolean getWasteFull() throws BaseException {
		return proxy.getWasteFull();
	}

	@Override
	public int getWasteLevel() throws BaseException {
		return proxy.getWasteLevel();
	}

	@Override
	public boolean getWaterEmpty() throws BaseException {
		return proxy.getWaterEmpty();
	}

	@Override
	public int getWaterLevel() throws BaseException {
		return proxy.getWaterLevel();
	}

	@Override
	public int getWellLiquidVolume(int arg0, int arg1, int arg2) throws BaseException {
		return proxy.getWellLiquidVolume(arg0, arg1, arg2);
	}

	@Override
	public double getWellVolume(int arg0, int arg1, int arg2) throws BaseException {
		return proxy.getWellVolume(arg0, arg1, arg2);
	}

	@Override
	public boolean isTaskRunning(int arg0) throws BaseException {
		return proxy.isTaskRunning(arg0);
	}

	@Override
	public int loadPlates() throws BaseException {
		return proxy.loadPlates();
	}

	@Override
	public int measureConcentration(int arg0, int arg1, int arg2) throws BaseException {
		return proxy.measureConcentration(arg0, arg1, arg2);
	}

	@Override
	public int mix(int arg0, int arg1, int arg2, double arg3, int arg4) throws BaseException {
		return proxy.mix(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public void moveSyringeBackward(double arg0) throws BaseException {
		proxy.moveSyringeBackward(arg0);
	}

	@Override
	public void moveSyringeForward(double arg0) throws BaseException {
		proxy.moveSyringeForward(arg0);
	}

	@Override
	public void onExposureCellFilled() throws BaseException {
		proxy.onExposureCellFilled();
	}

	@Override
	public int pull(double arg0, double arg1) throws BaseException {
		return proxy.pull(arg0, arg1);
	}

	@Override
	public int push(double arg0, double arg1) throws BaseException {
		return proxy.push(arg0, arg1);
	}

	@Override
	public int recuperate(int arg0, int arg1, int arg2) throws BaseException {
		return proxy.recuperate(arg0, arg1, arg2);
	}

	@Override
	public void restart(boolean arg0) throws BaseException {
		proxy.restart(arg0);
	}

	@Override
	public int scanAndPark() throws BaseException {
		return proxy.scanAndPark();
	}

	@Override
	public void setBeamLocation(String arg0) throws BaseException {
		proxy.setBeamLocation(arg0);
	}

	@Override
	public void setBeamShapeEllipse(boolean arg0) throws BaseException {
		proxy.setBeamShapeEllipse(arg0);
	}

	@Override
	public void setEnablePlateBarcodeScan(boolean arg0) throws BaseException {
		proxy.setEnablePlateBarcodeScan(arg0);
	}

	@Override
	public void setEnableSpectrometer(boolean arg0) throws BaseException {
		proxy.setEnableSpectrometer(arg0);
	}

	@Override
	public void setEnableVolumeDetectionInWell(boolean arg0) throws BaseException {
		proxy.setEnableVolumeDetectionInWell(arg0);
	}

	@Override
	public void setFocusPosition(FocusPosition arg0) throws BaseException {
		proxy.setFocusPosition(arg0);
	}

	@Override
	public void setLightLevel(int arg0) throws BaseException {
		proxy.setLightLevel(arg0);
	}

	@Override
	public void setLiquidPositionFixed(boolean arg0) throws BaseException {
		proxy.setLiquidPositionFixed(arg0);
	}

	@Override
	public void setLocalLockout(boolean arg0) throws BaseException {
		proxy.setLocalLockout(arg0);
	}

	@Override
	public void setSamplePathDeadVolume(double arg0) throws BaseException {
		proxy.setSamplePathDeadVolume(arg0);
	}

	public void setSampleType(String arg0) throws BaseException {
		proxy.setSampleType(SampleType.valueOf(arg0));
	}
	
	@Override
	public void setSampleType(SampleType arg0) throws BaseException {
		proxy.setSampleType(arg0);
	}

	@Override
	public void setTemperatureSEU(double arg0) throws BaseException {
		proxy.setTemperatureSEU(arg0);
	}

	@Override
	public void setTemperatureSEUSetpoint(double arg0) throws BaseException {
		proxy.setTemperatureSEUSetpoint(arg0);
	}

	@Override
	public void setTemperatureSampleStorage(double arg0) throws BaseException {
		proxy.setTemperatureSampleStorage(arg0);
	}

	@Override
	public void setTemperatureSampleStorageSetpoint(double arg0) throws BaseException {
		proxy.setTemperatureSampleStorageSetpoint(arg0);
	}

	public void setViscosityLevel(String arg0) throws BaseException {
		proxy.setViscosityLevel(ViscosityLevel.valueOf(arg0));
	}
	
	@Override
	public void setViscosityLevel(ViscosityLevel arg0) throws BaseException {
		proxy.setViscosityLevel(arg0);
	}

	@Override
	public void stopSyringe() throws BaseException {
		proxy.stopSyringe();
	}

	@Override
	public int transfer(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, double arg6) throws BaseException {
		return proxy.transfer(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public int waitTemperatureSEU(double arg0) throws BaseException {
		return proxy.waitTemperatureSEU(arg0);
	}

	@Override
	public int waitTemperatureSample(double arg0) throws BaseException {
		return proxy.waitTemperatureSample(arg0);
	}
}