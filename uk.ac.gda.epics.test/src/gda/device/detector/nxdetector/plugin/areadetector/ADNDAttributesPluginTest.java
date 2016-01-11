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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.scan.ScanInformation;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ADNDAttributesPluginTest {
	private ADNDAttributesPlugin adNdAttributesPlugin;
	@Mock
	private NDFileHDF5 ndFile;
	@Mock
	private ScanInformation scanInfo;
	@Mock
	private NXDetectorData data;
	private String nDAttributeNodeNameInHDF5File;
	private String detectorName;


	@Before
	public void setUp() {
		adNdAttributesPlugin = new ADNDAttributesPlugin();
		nDAttributeNodeNameInHDF5File = "RingCurrent";
		adNdAttributesPlugin.setNDAttributeNodeNameInHDF5File(nDAttributeNodeNameInHDF5File);
		detectorName = "det1";

	}

	@Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesValidation() throws Exception {
		adNdAttributesPlugin.setNDAttributeNodeNameInHDF5File(null);
		adNdAttributesPlugin.afterPropertiesSet();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNDAttributesLinkAppenderWithFilenameNull() throws Exception {
		when(ndFile.getFullFileName_RBV()).thenReturn(null);
		adNdAttributesPlugin.setNdFile(ndFile);
		adNdAttributesPlugin.prepareForCollection(1, scanInfo);
		List<NXDetectorDataAppender> appender = adNdAttributesPlugin.read(1);
		appender.get(0).appendTo(data, "det1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNDAttributesLinkAppenderWithFilenameZeroLength() throws Exception {
		when(ndFile.getFullFileName_RBV()).thenReturn("");
		adNdAttributesPlugin.setNdFile(ndFile);
		adNdAttributesPlugin.prepareForCollection(1, scanInfo);
		List<NXDetectorDataAppender> appender = adNdAttributesPlugin.read(1);
		appender.get(0).appendTo(data, detectorName);
	}

	@Test
	public void testNDAttributesLinkAppenderCallOnce() throws Exception {
		when(ndFile.getFullFileName_RBV()).thenReturn("/dls/ixx/data/2015/cmxxxx-x/" + detectorName + "-0000.hdf");
		String filename = ndFile.getFullFileName_RBV();
		adNdAttributesPlugin.setNdFile(ndFile);
		adNdAttributesPlugin.prepareForCollection(1, scanInfo);
		List<NXDetectorDataAppender> appender;
		for (int i = 0; i < 4; i++) {
			appender = adNdAttributesPlugin.read(1);
			appender.get(0).appendTo(data, detectorName);
		}

		verify(data, times(1)).addExternalFileLink("det1", nDAttributeNodeNameInHDF5File,
				"nxfile://" + filename + "#entry/instrument/NDAttributes/" + nDAttributeNodeNameInHDF5File, false, false);
	}

}
