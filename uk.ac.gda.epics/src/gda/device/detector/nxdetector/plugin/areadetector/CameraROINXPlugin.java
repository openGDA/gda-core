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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.scan.ScanInformation;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class to setup the ROI of the camera at prepareCollection and report roi in read
 */
public class CameraROINXPlugin implements NXPlugin{
	private boolean firstReadoutInScan;

	Integer minX, minY, sizeX, sizeY;
	int minX_rbv, minY_rbv, sizeX_rbv, sizeY_rbv;

	String name;
	ADBase adBase;
	public boolean isFirstReadoutInScan() {
		return firstReadoutInScan;
	}

	@Override
	public String getName() {
		return name;
	}

	public CameraROINXPlugin(String name, ADBase adBase) {
		super();
		this.name = name;
		this.adBase = adBase;
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		firstReadoutInScan=true;
		if( minX!= null){
			adBase.setMinX(minX);
		}
		minX_rbv = adBase.getMinX_RBV();
		if( minY!= null){
			adBase.setMinY(minY);
		}
		minY_rbv = adBase.getMinY_RBV();
		if( sizeX!= null){
			adBase.setSizeX(sizeX);
		}
		sizeX_rbv = adBase.getSizeX_RBV();
		if( sizeY!= null){
			adBase.setSizeY(sizeY);
		}
		sizeY_rbv = adBase.getSizeY_RBV();

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
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		NXDetectorDataAppender dataAppender = firstReadoutInScan ? new NXDetectorDataAppender(){

			@Override
			public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
				data.addData(detectorName, "region_origin", new int[]{minX_rbv, minY_rbv}, null);
				data.addData(detectorName, "region_size", new int[]{sizeX_rbv, sizeY_rbv}, null);
				}
			} : new NXDetectorDataNullAppender();
		firstReadoutInScan = false; 
		return Arrays.asList(new NXDetectorDataAppender[]{ dataAppender});
	}

	public Integer getMinX() {
		return minX;
	}

	public void setMinX(Integer minX) {
		this.minX = minX;
	}

	public Integer getMinY() {
		return minY;
	}

	public void setMinY(Integer minY) {
		this.minY = minY;
	}

	public Integer getSizeX() {
		return sizeX;
	}

	public void setSizeX(Integer sizeX) {
		this.sizeX = sizeX;
	}

	public Integer getSizeY() {
		return sizeY;
	}

	public void setSizeY(Integer sizeY) {
		this.sizeY = sizeY;
	}

}
