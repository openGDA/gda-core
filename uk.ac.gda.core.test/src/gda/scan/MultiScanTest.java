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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.MotorStatus;
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
import gda.device.motor.MotorBase;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableUtils;

public class MultiScanTest {

	@Test
	public void simplestScan() throws Exception {
		TestHelpers.setUpTest(MultiScanTest.class, "simplestScan2", true);
		LocalProperties.setScanSetsScanNumber(true);

		final Scannable simpleScannable1 = TestHelpers.createTestScannable("csvc", 0., new String[] {},
				new String[] { "csvc" }, 0, new String[] { "%5.2g" }, null);

		int totalLength;
		final int[] dims2 = new int[] { 2, 3 };
		totalLength = NexusExtractor.calcTotalLength(dims2);
		final String [] outputFormat = new String[totalLength+1];
		for (int index = 0; index < totalLength; index++) {
			outputFormat[index] =  "%5.2g";
		}
		outputFormat[totalLength] =  "%5.2g"; //for collectionTime

		final MyMoveController moveController = new MyMoveController();
		final MyScannableMotor scannableMotor = new MyScannableMotor();
		scannableMotor.setName("csvc");
		scannableMotor.setMotor(new MyMotor());
		scannableMotor.setCmc(moveController);
		scannableMotor.configure();
		moveController.setScannableBeingMoved(scannableMotor);

		final HardwareTriggeredNXDetector htd = new HardwareTriggeredNXDetector();
		htd.setName("htd");
		htd.setHardwareTriggerProvider(moveController);
		htd.setCollectionStrategy(createCollectionStrategyPlugin());
		htd.setAdditionalPluginList(Arrays.asList(new NXPluginBase[] { createNXPlugin() }));
		htd.afterPropertiesSet();
		htd.configure();

		final ConcurrentScan scan1 = new ConcurrentScan(new Object[]{simpleScannable1, 0, 10, 1, htd, .1});
		final ConstantVelocityScanLine cvls = new ConstantVelocityScanLine(new Object[]{scannableMotor, 0, 10, 1, htd, .1});
		final MultiScan ms = new MultiScan(Arrays.asList(new ScanBase[]{scan1, cvls}));
		ms.runScan();
	}

	//-----------------------------------------------------------------------------------------------
	// Mock NXPlugin
	//-----------------------------------------------------------------------------------------------
	private NXPlugin createNXPlugin() throws Exception {
		final String pluginName = "mynxplugin";
		final NXPlugin nxPlugin = mock(NXPlugin.class);
		when(nxPlugin.getName()).thenReturn(pluginName);
		when(nxPlugin.read(anyInt())).thenReturn(createNxPluginAppenders(pluginName));
		return nxPlugin;
	}

	private List<NXDetectorDataAppender> createNxPluginAppenders(String name) throws Exception {
		final List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDataAppender() {
			@Override
			public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
				data.addData(detectorName, name, new NexusGroupData(0d), null, null);
			}
		});
		return appenders;
	}

	//-----------------------------------------------------------------------------------------------
	// Mock NXCollectionStrategyPlugin
	//-----------------------------------------------------------------------------------------------
	private interface MyCollectionStrategyPlugin extends NXCollectionStrategyPlugin, NXPlugin {}
	private NXCollectionStrategyPlugin createCollectionStrategyPlugin() throws Exception {
		final MyCollectionStrategyPlugin strategy = mock(MyCollectionStrategyPlugin.class);
		when(strategy.getInputStreamNames()).thenReturn(inputStreamNames());
		when(strategy.getInputStreamFormats()).thenReturn(Arrays.asList(new String[] {"%d"}));
		when(strategy.read(anyInt())).thenReturn(createAppenders());
		when(strategy.requiresAsynchronousPlugins()).thenReturn(true);
		return strategy;
	}

	private List<NXDetectorDataAppender> createAppenders() {
		final List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDataDoubleAppender(inputStreamNames(), Arrays.asList(new Double[] { 1.0 })));
		return appenders;
	}

	private List<String> inputStreamNames() {
		return Arrays.asList(new String[] {"pulse"});
	}

	//--------------------------------------------------------------------------------------------------------------

	private static class MyScannableMotor extends ScannableMotor implements ContinuouslyScannableViaController, PositionCallableProvider<Double>{
		private static final Logger logger = LoggerFactory.getLogger(MyScannableMotor.class);
		private ContinuousMoveController cmc;
		private List<Double> points = null;

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

	//--------------------------------------------------------------------------------------------------------------

	private static class MyMoveController extends ScannableBase implements ConstantVelocityMoveController{
		private static final Logger logger = LoggerFactory.getLogger(MyMoveController.class);

		private double triggerPeriod;
		private double end;
		private double step;
		private double start;
		private Scannable scannableBeingMoved;

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

	//--------------------------------------------------------------------------------------------------------------

	private static class MyMotor extends MotorBase {

		private double posn = 0.0;
		private double speed = 2;
		private MotorStatus status = MotorStatus.READY;
		private boolean moving = false;

		public MyMotor() {
			super();
			setName("myMotor");
		}

		@Override
		public void configure(){
			// no configuration required
		}

		@Override
		public void moveBy(double steps) throws MotorException {
			moving = true;
			posn += steps;
			moving = false;
		}

		@Override
		public void moveTo(double steps) throws MotorException {
			moving = true;
			posn = steps;
			moving = false;
		}

		@Override
		public void moveContinuously(int direction) throws MotorException {
			moving = true;
			posn = posn + direction * 10;
		}

		@Override
		public void setPosition(double steps) throws MotorException {
			posn = steps;
		}

		@Override
		public double getPosition() throws MotorException {
			return posn;
		}

		@Override
		public void setSpeed(double speed) throws MotorException {
			this.speed = speed;
		}

		@Override
		public double getSpeed() throws MotorException {
			return speed;
		}

		@Override
		public void stop() throws MotorException {
			moving = false;
		}

		@Override
		public void panicStop() throws MotorException {
			moving = false;
		}

		@Override
		public MotorStatus getStatus() {
			return status;
		}

		@Override
		public boolean isMoving() throws MotorException {
			return moving;
		}
	}
}
