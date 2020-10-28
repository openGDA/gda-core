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

package gda.data.scan.datawriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import gda.data.ServiceHolder;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;

public class MetadataCalculatorTest {

	private ScannableWriter createScannableWriter(String... metadataScannableNames) {
		final SingleScannableWriter writer = new SingleScannableWriter();
		writer.setPrerequisiteScannableNames(Arrays.asList(metadataScannableNames));
		return writer;
	}

	@Test
	public void testCalculateMetadataScannableNames() {
		configureMetadataScannables();

		final Set<String> detectorNames = Sets.newHashSet("det1", "det2", "det3"); // TODO when we can use Java 9+, use Set.of(...)
		final Set<String> scannableNames = Sets.newHashSet("energy", "stage_y", "stage_x");
		final MetadataScannableCalculator metadataScannableCalculator =
				new MetadataScannableCalculator(detectorNames, scannableNames);

		final Set<String> metadataScannables = metadataScannableCalculator.calculateMetadataScannableNames();
		assertThat(metadataScannables, containsInAnyOrder("m1", "m2", "m3", "m4", "m5",
				"m6", "m7", "m8", "m9", "m10", "m11", "m12", "m13", "m14", "m15"));
	}

	private void configureMetadataScannables() {
		final NexusDataWriterConfiguration config = ServiceHolder.getNexusDataWriterConfiguration();
		config.setMetadataScannables(Sets.newHashSet("m1", "m2"));
		config.setMetadataScannablesForDetector("det1", Sets.newHashSet("m3", "m4"));
		config.setMetadataScannablesForDetector("det3", Sets.newHashSet("m5"));

		final Map<String, ScannableWriter> locationMap = new HashMap<>();
		locationMap.put("stage_x", createScannableWriter("m6", "m7"));
		locationMap.put("stage_y", createScannableWriter("m1", "m4"));

		locationMap.put("m4", createScannableWriter("m8", "m9", "stage_x"));
		locationMap.put("m5", createScannableWriter("m10"));
		locationMap.put("m10", createScannableWriter("m11"));
		locationMap.put("m11", createScannableWriter("m12"));
		locationMap.put("m12", createScannableWriter("m13", "m14"));
		locationMap.put("m6", createScannableWriter("m15"));

		config.setLocationMap(locationMap);
	}

}
