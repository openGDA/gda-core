/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.AsyncNXCollectionStrategy;
import gda.scan.ScanInformation;

public class MbsAnalyserCollectionStrategy implements AsyncNXCollectionStrategy{

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(MbsAnalyserCollectionStrategy.class);
	protected MbsAnalyser analyser;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private Future<Integer> acquisitionTask;
	protected MbsAnalyserCompletedRegion completedRegion;

	private static final String CPS_OUTPUT_FORMAT = "%5.5g";

	public MbsAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(MbsAnalyser analyser) {
		this.analyser = analyser;
	}

	@Override
	public double getAcquireTime() throws Exception {
		return analyser.getCollectionTime();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return analyser.getAcquirePeriod();
	}

	@Override
	@Deprecated(since="GDA 8.26")
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		logger.deprecatedMethod("configureAcquireAndPeriodTimes(double)");
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		analyser.setCollectionTime(collectionTime);
		analyser.disableAutomaticDetectorOff();
	}

	@Override
	public void collectData() throws Exception {
		acquisitionTask = executorService.submit(() -> {
			analyser.startAcquiringWait();
			if (analyser.getAnalyserStatus() != MbsAnalyserStatus.IDLE) {
				return Detector.FAULT;
			}

			completedRegion = analyser.getCompletedRegion();
			return Detector.IDLE;
		});
	}

	@Override
	public int getStatus() throws Exception {
		return analyser.getDetectorState();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		if (acquisitionTask == null) {
			return;
		}

		int status = acquisitionTask.get();
		if (status != Detector.IDLE) {
			throw new DeviceException("Analyser was not idle, Status is: " + analyser.getAnalyserStatus() + ", Message is: " + analyser.getStatusMessage());
		}
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		// No-op
	}

	@Override
	public boolean isGenerateCallbacks() {
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return 1;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return false;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		// No-op
	}

	@Override
	public void prepareForLine() throws Exception {
		// No-op
	}

	@Override
	public void completeLine() throws Exception {
		// No-op
	}

	@Override
	public void completeCollection() throws Exception {
		acquisitionTask = null;
		completedRegion = null;
		analyser.enableAutomaticDetectorOff();
	}

	@Override
	public void atCommandFailure() throws Exception {
		stop();
	}

	@Override
	public void stop() throws Exception {
		analyser.stopAcquiring();
		completeCollection();
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList("cps");
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList(CPS_OUTPUT_FORMAT);
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException, DeviceException {

		return Arrays.asList(new MbsNXDetectorDataAppender(completedRegion));
	}
}
