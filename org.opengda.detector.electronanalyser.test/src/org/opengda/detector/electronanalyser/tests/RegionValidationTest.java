/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.tests;

import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opengda.detector.electronanalyser.api.SESExcitationEnergySource;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.api.SESSequence;
import org.opengda.detector.electronanalyser.api.SESSequenceHelper;
import org.opengda.detector.electronanalyser.lenstable.IRegionValidator;
import org.opengda.detector.electronanalyser.lenstable.RegionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import uk.ac.diamond.daq.pes.api.AnalyserEnergyRangeConfiguration;

public class RegionValidationTest {

	private static final Logger logger = LoggerFactory.getLogger(RegionValidationTest.class);

	private static final String SEQUENCE_FILE_TO_TEST = RegionValidationTest.class.getSimpleName() + File.separator + "region_valid_testing.seq";
	private static final String ANALYSER_TABLE_DIRECTORY = "./testfiles/analysertables";
	private static final String ANALYSER_TABLE_PATH = ANALYSER_TABLE_DIRECTORY + File.separator + RegionValidationTest.class.getSimpleName() + File.separator + "high_elementset_table.txt";
	private static final String ELEMENT_SET = "High";

	private static IRegionValidator regionValidator;

	@BeforeAll
	public static void setup() throws IOException {
		SetupSESSettingsTestHelper.setupFinderAndSESSettingsService();
		final AnalyserEnergyRangeConfiguration energyRange = new AnalyserEnergyRangeConfiguration(ANALYSER_TABLE_PATH);
		regionValidator = new RegionValidator("regionvalidator", energyRange);
	}

	@AfterAll
	public static void tearDown() {
		SetupSESSettingsTestHelper.tearDown();
	}

	public Map<String, Boolean> getExpectedRegionValidation() {
		return Map.ofEntries(
			entry("Kinetic_valid1", true),
			entry("Kinetic_valid2", true),
			entry("Kinetic_invalid1", false),
			entry("Kinetic_invalid2", false),
			entry("Binding_valid1", true),
			entry("Binding_valid2", true),
			entry("Binding_invalid1", false),
			entry("Binding_invalid2", false),
			entry("invalidLensMode1", false),
			entry("invalidLensMode2", false)
		);
	}

	public static List<Arguments> regionExcitationEnergyParameters() throws Exception {
		final String filePath = SESSequenceHelper.getDefaultFilePath() + File.separator + SEQUENCE_FILE_TO_TEST;
		final SESSequence sequence = SESSequenceHelper.loadSequence(filePath);
		//Sync scannables with sequence file saved positions.
		for (final SESExcitationEnergySource excitationEnergySource : sequence.getExcitationEnergySources()) {
			final Scannable scannableExcitationEnergySource = excitationEnergySource.getScannable();
			scannableExcitationEnergySource.asynchronousMoveTo(excitationEnergySource.getValue());
		}
		final List<Arguments> arguments = new ArrayList<>();
		for (final SESRegion region : sequence.getRegions()) {
			final double excitationEnergy = sequence.getExcitationEnergySourceByRegion(region).getValue();
			arguments.add(Arguments.of(region, excitationEnergy));
		}
		return arguments;
	}

	@ParameterizedTest(name = "region = {0}, excitationEnergy = {1}")
	@MethodSource("regionExcitationEnergyParameters")
	public void testSequenceRegionsAreCorrectlyValidated(SESRegion region, double excitationEnergy) {
		final Map<String, Boolean> expectedRegionValues = getExpectedRegionValidation();

		final boolean isRegionValid = regionValidator.isValidRegion(region, ELEMENT_SET, excitationEnergy);
		final boolean expectedIsRegionValid = expectedRegionValues.get(region.getName());
		logger.info("Testing region {}. Expected value: {}, Actual value: {}", region.getName(), expectedIsRegionValid, isRegionValid);
		assertThat(isRegionValid, is(equalTo(expectedIsRegionValid)));
	}
}
