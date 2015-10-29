/*-
 * Copyright © 2013 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.scan;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.detector.HardwareTriggeredNXDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.device.motor.TotalDummyMotor;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiScanTest {


	@Before
	public void setUp() {
	}


	@Test
	public void simplestScan() throws Exception {


		TestHelpers.setUpTest(MultiScanTest.class, "simplestScan2", true);
		LocalProperties.setScanSetsScanNumber(true);

		Scannable simpleScannable1 = TestHelpers.createTestScannable("csvc", 0., new String[] {},
				new String[] { "csvc" }, 0, new String[] { "%5.2g" }, null);

		int totalLength;
		int[] dims2 = new int[] { 2, 3 };
		totalLength = NexusExtractor.calcTotalLength(dims2);
		String [] outputFormat = new String[totalLength+1];
		for (int index = 0; index < totalLength; index++) {
			outputFormat[index] =  "%5.2g";
		}
		outputFormat[totalLength] =  "%5.2g"; //for collectionTime

		MyCMC cmc = new MyCMC();
		My my = new My();
		my.setName("csvc");
		my.setMotor(new TotalDummyMotor());
		my.setCmc(cmc);
		my.configure();
		cmc.setScannableBeingMoved(my);

		HardwareTriggeredNXDetector htd = new HardwareTriggeredNXDetector();
		htd.setName("htd");
		htd.setHardwareTriggerProvider(cmc);
		htd.setCollectionStrategy(new MyCSP());
		htd.setAdditionalPluginList(Arrays.asList(new NXPluginBase[]{new MyNXPlugin()}));
		htd.afterPropertiesSet();
		htd.configure();

		ConcurrentScan scan1 = new ConcurrentScan(new Object[]{simpleScannable1, 0, 10, 1, htd, .1});
		ConstantVelocityScanLine cvls = new ConstantVelocityScanLine(new Object[]{my, 0, 10, 1, htd, .1});
		MultiScan ms = new MultiScan(Arrays.asList(new ScanBase[]{scan1, cvls}));
		ms.runScan();
	}

}



class My extends ScannableMotor implements ContinuouslyScannableViaController, PositionCallableProvider<Double>{
	private static final Logger logger = LoggerFactory.getLogger(My.class);
	ContinuousMoveController cmc;



	public ContinuousMoveController getCmc() {
		return cmc;
	}

	public void setCmc(ContinuousMoveController cmc) {
		this.cmc = cmc;
	}

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		return new Callable<Double>(){

			@Override
			public Double call() throws Exception {
				return 0.;
			}};
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
	}

	@Override
	public boolean isOperatingContinously() {
		return true;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return cmc;
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		throw new IllegalArgumentException("setContinuousMoveController("+controller.getName()+") not supported on "+this.getName());
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (isOperatingContinously()) {
			addPoint(PositionConvertorFunctions.toDouble(externalToInternal(position)));
		} else {
			super.asynchronousMoveTo(position);
		}
	}
	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			Object[] pos = (Object[]) internalToExternal(new Double[]{getLastPointAdded()});
			if (pos == null) {
				// First point is in process of being added
				return super.getPosition();
			}
			return pos[0];
		}
		return super.getPosition();
	}

	List<Double> points = null;


	public void addPoint(Double point) {
		if(points == null){
			points = new ArrayList<Double>();
		}
		points.add(point);
	}

	public Double getLastPointAdded() {
		if (points == null || points.size() == 0) {
			logger.info(getName() + ".getLastPointAdded() returning null, as no points have yet been added");
			return null;
		}
		return points.get(points.size() - 1);
	}

}
class MyCMC extends ScannableBase implements ConstantVelocityMoveController{
	private static final Logger logger = LoggerFactory.getLogger(MyCMC.class);

	private double triggerPeriod;
	private double end;
	private double step;
	private double start;
	Scannable scannableBeingMoved;


	public Scannable getScannableBeingMoved() {
		return scannableBeingMoved;
	}

	public void setScannableBeingMoved(Scannable scannableBeingMoved) {
		this.scannableBeingMoved = scannableBeingMoved;
	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		logger.info("setTriggerPeriod:"+seconds);
		triggerPeriod = seconds; //readout need to use readout time;

	}

	@Override
	public int getNumberTriggers() {
		logger.info("getNumberTriggers");
		try {
			return ScannableUtils.getNumberSteps(scannableBeingMoved, new Double(start),new Double(end),new Double(step))+1;
		} catch (Exception e) {
			logger.error("Error getting number of triggers", e);
			return 0;
		}
	}

	@Override
	public double getTotalTime() throws DeviceException {
		logger.info("getTotalTime");
		return (getNumberTriggers() == 0) ? 0 : triggerPeriod * (getNumberTriggers() - 1);
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		logger.info("prepareForMove");
	}

	@Override
	public void startMove() throws DeviceException {
		logger.info("startMove");
	}

	@Override
	public boolean isMoving() throws DeviceException {
		logger.info("isMoving");
		return false;
	}

	@Override
	public void waitWhileMoving() throws DeviceException, InterruptedException {
		logger.info("waitWhileMoving");
	}

	@Override
	public void stopAndReset() throws DeviceException, InterruptedException {
		logger.info("stopAndReset");
	}

	@Override
	public void setStart(double start) throws DeviceException {
		logger.info("setStart:" + start);
		this.start = start;
	}

	@Override
	public double getStart() {
		return start;
	}

	@Override
	public void setEnd(double end) throws DeviceException {
		logger.info("setEnd:" + end);
		this.end = end;
	}

	@Override
	public double getEnd() {
		return end;
	}

	@Override
	public void setStep(double step) throws DeviceException {
		logger.info("setStep:"+ step);
		this.step = step;

	}

	@Override
	public double getStep() {
		return step;
	}
}


class MyCSP implements NXCollectionStrategyPlugin, NXPlugin {

	@Override
	public String getName() {
		return null;
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
	}

	@Override
	public List<String> getInputStreamNames() {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add("pulse");
		return fieldNames;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();
		formats.add("%d");
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), Arrays.asList(new Double[]{1.0})));
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
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
	}

	@Override
	public void collectData() throws Exception {
	}

	@Override
	public int getStatus() throws Exception {
		return 0;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
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
		return 0;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}
}

class MyNXPlugin implements NXPlugin{

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDataAppender(){

			@Override
			public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
				data.addData(detectorName, getName(), new NexusGroupData(0d), null, null);
			}});
		return appenders;
	}

	@Override
	public String getName() {
		return "mynxplugin";
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
	}

	@Override
	public List<String> getInputStreamNames() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Collections.emptyList();
	}
}