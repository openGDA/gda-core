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

package gda.device.detector.nxdetector.plugin;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.scan.ScanInformation;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class NullNXCollectionStrategy implements NXCollectionStrategyPlugin {

	private String name;
	private Double collectionTime=0.;

	public NullNXCollectionStrategy(String name) {
		super();
		this.name = name;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public boolean willRequireCallbacks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void prepareForLine() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void completeLine() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void completeCollection() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void atCommandFailure() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList(new String[]{"time"});
//		return Arrays.asList(new String[0]);
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList(new String[]{"%5.5g"});
//		return Arrays.asList(new String[0]);
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		// TODO Auto-generated method stub
		Vector<NXDetectorDataAppender> vector = new Vector<NXDetectorDataAppender>();
//		vector.add(new NXDetectorDataNullAppender());
		vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), Arrays.asList(new Double[]{collectionTime})));
		return vector;
	}

	@Override
	public double getAcquireTime() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
		this.collectionTime = collectionTime;

	}

	@Override
	public void collectData() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getStatus() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isGenerateCallbacks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
