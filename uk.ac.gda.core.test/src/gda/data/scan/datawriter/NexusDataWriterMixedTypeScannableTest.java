/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.measure.quantity.Length;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.dataset.IDataset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gda.TestHelpers;
import gda.data.ServiceHolder;
import gda.device.Scannable;
import gda.device.scannable.DummyMultiFieldUnitsScannable;
import gda.scan.ConcurrentScan;

class NexusDataWriterMixedTypeScannableTest {

	private String outputDir;

	private Scannable scannable;

	private static final String SCANNABLE_NAME = "s1";
	private static final String NUM_FIELD_NAME = "numField";
	private static final String STRING_FIELD_NAME = "strField";
	private static final String STRING_POS = "strPos";

	private static final int NUM_POINTS = 6;

	@BeforeAll
	public static void setUpServices() throws Exception {
		final ServiceHolder gdaDataServiceHolder = new ServiceHolder();
		gdaDataServiceHolder.setNexusTemplateService(new NexusTemplateServiceImpl());

		final org.eclipse.dawnsci.nexus.ServiceHolder oednServiceHolder = new org.eclipse.dawnsci.nexus.ServiceHolder();
		oednServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
	}

	@BeforeEach
	void setUp() throws Exception {
		final DummyMultiFieldUnitsScannable<Length> dummyScannable = new DummyMultiFieldUnitsScannable<>(SCANNABLE_NAME);
		dummyScannable.setInputNames(new String[] { NUM_FIELD_NAME });
		dummyScannable.setExtraNames(new String[] { STRING_FIELD_NAME });
		dummyScannable.setExtraFieldsPosition(STRING_POS);

		this.scannable = dummyScannable;
	}

	@Disabled("Test is flaky")
	@Test
	void testMixedScannableFieldTypes() throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), "testMixedScannableFieldTypes", true);
		outputDir = testDir + "/Data/";

		// create and run the scan
		final Object[] scanArgs = new Object[] { scannable, 0, NUM_POINTS - 1, 1};
		final ConcurrentScan scan = new ConcurrentScan(scanArgs);
		scan.run();

		// check the nexus file was written ok and has the expected structure
		final Path nexusFilePath = Paths.get(outputDir, "1.nxs");
		assertThat(Files.exists(nexusFilePath), is(true));

		try(final NexusFile nexusFile = NexusTestUtils.openNexusFile(nexusFilePath.toString())) {
			checkNexusFile(nexusFile);
		}
	}

	private void checkNexusFile(final NexusFile nexusFile) throws NexusException {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		assertThat(nexusTree, is(notNullValue()));
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry("entry1");
		assertThat(entry, is(notNullValue()));
		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));
		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(SCANNABLE_NAME, "source"));

		final NXpositioner positioner = instrument.getPositioner(SCANNABLE_NAME);
		assertThat(positioner, is(notNullValue()));
		assertThat(positioner.getDataNodeNames(), containsInAnyOrder(NUM_FIELD_NAME, STRING_FIELD_NAME));

		final int[] scanShape = new int[] { NUM_POINTS };
		final IDataset numValueDataset = positioner.getDataset(NUM_FIELD_NAME);
		assertThat(numValueDataset, is(notNullValue()));
		assertThat(numValueDataset.getShape(), is(equalTo(scanShape)));
		for (int pointNum = 0; pointNum < NUM_POINTS; pointNum++) {
			assertThat(numValueDataset.getDouble(pointNum), is(closeTo(pointNum, 1e-15)));
		}

		final IDataset strValueDataset = positioner.getDataset(STRING_FIELD_NAME);
		assertThat(strValueDataset, is(notNullValue()));
		assertThat(strValueDataset.getShape(), is(equalTo(scanShape)));
		for (int pointNum = 0; pointNum < NUM_POINTS; pointNum++) {
			assertThat(strValueDataset.getString(pointNum), is(equalTo(STRING_POS)));
		}
	}

}
