/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.AsyncNXCollectionStrategy;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.scan.ScanInformation;

/**
 * not used or tested yet.
 *
 * @author rjw82
 *
 */
@Deprecated
public class Xspress3CollectionStrategy extends ConfigurableBase implements AsyncNXCollectionStrategy{

	private Xspress3Controller controller;
	private Integer frameToLookFor = null;
//	private int framesRead;
//	private boolean writeHDF5Files = false;

	Xspress3CollectionStrategy(Xspress3Controller controller) {
		this.controller = controller;
	}

	@Override
	public void configure() throws FactoryException {
		controller.configure();
		setConfigured(true);
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

		//wait for 2s
		Thread.sleep(2000);

		frameToLookFor = 0;
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
		Vector<NXDetectorDataAppender> appenders = new Vector<> ();
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
		testForErrorState();
	}

	@Override
	public void collectData() throws Exception {
		testForErrorState();
		// at each point set the PV to rin the binary output to drive the xspress3
//		CAClient ca =new CAClient();
//		ca.caput("BL24I-EA-USER-01:BO2","1");
//		Thread.sleep(1100);
		// then set the number of the frame we are waiting for
		frameToLookFor++;
	}

	@Override
	public int getStatus() throws Exception {
		return controller.getStatus();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		// overrule the status if the frame we are waiting for becomes available (as set by the collectData() method).
		//FIXME this will probably not work in continuous scans where repeated calls to collectData() are not made.
		while (getStatus() == Detector.BUSY){
			int framesAvailable = controller.getTotalFramesAvailable();
			if (frameToLookFor != null && frameToLookFor > 0 && framesAvailable >= frameToLookFor) {
				return;
			}
			Thread.sleep(100);
		}

		testForErrorState();
	}

	private void testForErrorState() throws Exception, DeviceException {
		if (getStatus() == Detector.FAULT){
			throw new DeviceException(getName() + " is in an error state!");
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

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

}
