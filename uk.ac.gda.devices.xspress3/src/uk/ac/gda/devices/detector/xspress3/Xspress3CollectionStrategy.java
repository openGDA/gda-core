package uk.ac.gda.devices.detector.xspress3;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.scan.ScanInformation;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Xspress3CollectionStrategy implements NXCollectionStrategyPlugin, Configurable{

	private Xspress3Controller controller;
//	private int framesRead;
//	private boolean writeHDF5Files = false;

	Xspress3CollectionStrategy(Xspress3Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public void configure() throws FactoryException {
		controller.configure();
	}
	
//	public boolean isWriteHDF5Files() {
//		return writeHDF5Files;
//	}
//
//	public void setWriteHDF5Files(boolean writeHDF5Files) {
//		this.writeHDF5Files = writeHDF5Files;
//	}

	@Override
	public String getName() {
		return "Xspress3 Driver";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
	}

	@Override
	public void prepareForLine() throws Exception {
//		framesRead = 0;
		controller.doErase();
//		if (writeHDF5Files) {
//			// do not do this if writeHDF5Files is false as may cause errors in
//			// epics
//			controller.setSavingFiles(writeHDF5Files);
//		}
		controller.doStart();
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
		controller.doStop();
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender> ();
		appenders.add(new NXDetectorDataNullAppender());
		return appenders;
	}

	@Override
	public double getAcquireTime() throws Exception {
		return 0;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return 0;
	}

	@Override
	@Deprecated
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		controller.setNumFramesToAcquire(scanInfo.getDimensions()[0]);
	}

	@Override
	public void collectData() throws Exception {
	}

	@Override
	public int getStatus() throws Exception {
		return controller.getStatus();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		while (getStatus() == Detector.BUSY){
			Thread.sleep(100);
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
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return 1;
	}

}
