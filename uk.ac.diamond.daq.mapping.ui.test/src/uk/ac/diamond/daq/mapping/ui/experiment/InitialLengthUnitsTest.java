/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static gda.configuration.properties.LocalProperties.GDA_INITIAL_LENGTH_UNITS;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import gda.configuration.properties.LocalProperties;

public class InitialLengthUnitsTest {

	private Map<String, String> unitsMap = ImmutableMap.of(
		"xStart", "μm",
		"yStart", "μm",
		"fastAxisStep", "nm",
		"slowAxisStep", "nm"
	);

	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testMapCannotBeNull() {
		new InitialLengthUnits(null);
	}

	@Test
	public void testUnitsSetInMap() {
		final InitialLengthUnits lengthUnits = new InitialLengthUnits(unitsMap);
		assertEquals("μm", lengthUnits.getDefaultUnit("xStart"));
		assertEquals("μm", lengthUnits.getDefaultUnit("yStart"));
		assertEquals("nm", lengthUnits.getDefaultUnit("fastAxisStep"));
		assertEquals("nm", lengthUnits.getDefaultUnit("slowAxisStep"));
	}

	@Test
	public void testDefaultToLocalProperty() {
		LocalProperties.set(GDA_INITIAL_LENGTH_UNITS, "cm");
		final InitialLengthUnits lengthUnits = new InitialLengthUnits(unitsMap);
		assertEquals("μm", lengthUnits.getDefaultUnit("xStart"));
		assertEquals("cm", lengthUnits.getDefaultUnit("zStart"));
		LocalProperties.clearProperty(GDA_INITIAL_LENGTH_UNITS);
	}

	@Test
	public void testDefaultToMillimetres() {
		final InitialLengthUnits lengthUnits = new InitialLengthUnits(unitsMap);
		assertEquals("μm", lengthUnits.getDefaultUnit("xStart"));
		assertEquals("mm", lengthUnits.getDefaultUnit("zStart"));
	}
}
