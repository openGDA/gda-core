/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.junit.Before;
import org.junit.Test;

public class BeamScannableTest {

	private BeamScannable beamScannable;

	@Before
	public void setUp() {
		beamScannable = new BeamScannable();
		beamScannable.setBeamSize(0.1);
	}

	@Test
	public void testGetSetBeamSize() {
		assertThat(beamScannable.getBeamSize(), is(equalTo(0.1)));
		beamScannable.setBeamSize(0.234);
		assertThat(beamScannable.getBeamSize(), is(equalTo(0.234)));
	}

	@Test
	public void testGetSetPosition() throws Exception {
		assertThat(beamScannable.getPosition(), is(equalTo(0.1)));
		beamScannable.setBeamSize(0.234);
		assertThat(beamScannable.getPosition(), is(equalTo(0.234)));
		beamScannable.setPosition(0.567);
		assertThat(beamScannable.getPosition(), is(equalTo(0.567)));
	}

	@Test
	public void testGetNexusProvider() throws Exception {
		final NexusObjectProvider<NXbeam> nexusObjectProvider = beamScannable.getNexusProvider(null);
		assertThat(nexusObjectProvider, is(notNullValue()));
		assertThat(nexusObjectProvider.getCategory(), is(NexusBaseClass.NX_SAMPLE));
		final NXbeam beam = nexusObjectProvider.getNexusObject();
		assertThat(beam, is(notNullValue()));
		final DataNode extentDataNode = beam.getDataNode("extent");
		assertThat(extentDataNode, is(notNullValue()));
		final IDataset extentDataset = extentDataNode.getDataset().getSlice();
		assertThat(extentDataset, is(equalTo(DatasetFactory.createFromObject(beamScannable.getBeamSize()))));
	}

}
