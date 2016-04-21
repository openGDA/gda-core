/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.test.serialization;

import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;

public class TestSerialization {

	private IMarshallerService service;

	@Before
	public void createService() {
		service = new MarshallerService();
	}

	@Test
	public void testCircleMappingSerialization() throws Exception {

		CircularMappingRegion originalRegion = new CircularMappingRegion();
		String json = service.marshal(originalRegion);

		System.out.println(json);
		final CircularMappingRegion deserialisedRegion = service.unmarshal(json, CircularMappingRegion.class);

		assertEquals("Deserialised and serialised object are different", originalRegion, deserialisedRegion);
	}

	@Test
	public void testCircleMappingInvalidDeserializeAfterJsonModification() throws Exception {

		CircularMappingRegion originalRegion = new CircularMappingRegion();
		String json = service.marshal(originalRegion);

		System.out.println(json);
		json = json.replace("CIRCLE", RegionType.ELLIPSE.name());
		System.out.println(json);

		final CircularMappingRegion deserialisedRegion = service.unmarshal(json, CircularMappingRegion.class);

		assertEquals("The region type was sucessfully altered!", originalRegion.whichPlottingRegionType(), deserialisedRegion.whichPlottingRegionType());
		assertEquals("Deserialised and serialised object are different", originalRegion, deserialisedRegion);
	}

}
