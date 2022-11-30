/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Initially, this class is only used to add a link in the Nexus to the ring current recorded in EPICs using HDF5 file writer plugin, in the future multiple
 * links to different NDAttributes can be added.
 */

public class ADNDAttributesPlugin extends NullNXPlugin implements InitializingBean {
	private String filename;
	private boolean isFirstReadoutInScan;
	private String nDAttributeNodeNameInHDF5File;
	private NDFileHDF5 ndFile;

	public ADNDAttributesPlugin() {
		isFirstReadoutInScan = false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, gda.scan.ScanInformation scanInfo) throws Exception {
		isFirstReadoutInScan = true;
	}


	@Override
	public String getName() {
		return "NDAttributePlugin";
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	public String getFullFileNameFromPv() throws DeviceException {
		String filenameFromPv;
		try {
			filenameFromPv = ndFile.getFullFileName_RBV();
		} catch (Exception e) {
			throw new DeviceException("Problem getting the full HDF5 filename from EPICs");
		}
		return filenameFromPv;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		NXDetectorDataAppender dataAppender = null;
		if (isFirstReadoutInScan) {
			// It seems that in the PrepareForCollection the filename is not set up as the file name is udpated in the PrepareForCollection of the
			// MultipleImagesPerHDF5FileWriter plugin
			filename = getFullFileNameFromPv();
			dataAppender = new NDAttributesLinkAppender();
		} else {
			dataAppender = new NXDetectorDataNullAppender();
		}
		isFirstReadoutInScan = false;
		ArrayList<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		appenders.add(dataAppender);
		return appenders;

	}

	public String getNDAttributeNodeNameInHDF5File() {
		return nDAttributeNodeNameInHDF5File;
	}

	public void setNDAttributeNodeNameInHDF5File(String nDAttributeNodeNameInHDF5File) {
		this.nDAttributeNodeNameInHDF5File = nDAttributeNodeNameInHDF5File;
	}

	public NDFileHDF5 getNdFile() {
		return ndFile;
	}

	public void setNdFile(NDFileHDF5 ndFile) {
		this.ndFile = ndFile;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (nDAttributeNodeNameInHDF5File == null)
			throw new IllegalArgumentException("The NDAttribute node name (nDAttributeNodeNameInHDF5File) in HDF5 file is not initialized.");
	}

	class NDAttributesLinkAppender implements NXDetectorDataAppender {

		@Override
		public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
			if (!StringUtils.hasLength(filename)) {
				throw new IllegalArgumentException("filename is null or zero length");
			}
			data.addExternalFileLink(detectorName, nDAttributeNodeNameInHDF5File, "nxfile://" + filename + "#entry/instrument/NDAttributes/"
					+ nDAttributeNodeNameInHDF5File, false, false);
		}
	}

}
