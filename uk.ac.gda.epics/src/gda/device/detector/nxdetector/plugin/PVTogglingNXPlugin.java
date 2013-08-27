/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.scan.ScanInformation;

public class PVTogglingNXPlugin implements NXPlugin {

	
	private final String name;
	
	private Double valueDuringCollection;

	private Double valueOutsideCollection;

	private final PV<Double> pv;
	
	public PVTogglingNXPlugin(String name, String pvName) {
		this.name = name;
		pv = LazyPVFactory.newDoublePV(pvName);
	}
	
	public Double getValueDuringCollection() {
		return valueDuringCollection;
	}

	public void setValueDuringCollection(Double valueDuringCollection) {
		this.valueDuringCollection = valueDuringCollection;
	}

	public Double getValueOutsideCollection() {
		return valueOutsideCollection;
	}

	public void setValueOutsideCollection(Double valueOutsideCollection) {
		this.valueOutsideCollection = valueOutsideCollection;
	}
	
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (getValueDuringCollection() != null) {
			pv.putWait(getValueDuringCollection());
		}
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
		if (getValueOutsideCollection()!=null) {
			pv.putWait(getValueOutsideCollection());
		}
	}

	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();
	}

	@Override
	public void stop() throws Exception {
		completeCollection();
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
		List<NXDetectorDataAppender> list = new ArrayList<NXDetectorDataAppender>();
		list.add(new NXDetectorDataNullAppender());
		return list;
				
	}
	
	public void i() throws Exception { // for in!
		prepareForCollection(1, null);
	}
	
	public void o() throws Exception { // for out!
		completeCollection();
	}
	@Override
	public boolean supportsAsynchronousRead() {
		return true;
	}

}
