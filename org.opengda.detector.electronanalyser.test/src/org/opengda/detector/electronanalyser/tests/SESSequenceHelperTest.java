package org.opengda.detector.electronanalyser.tests;

import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opengda.detector.electronanalyser.api.SESExcitationEnergySource;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.api.SESSequence;
import org.opengda.detector.electronanalyser.api.SESSequenceHelper;
import org.opengda.detector.electronanalyser.api.SESSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SESSequenceHelperTest {

	private static final Logger logger = LoggerFactory.getLogger(SESSequenceHelperTest.class);

	private static final String XML_SEQUENCE_FILE = SESSequenceHelperTest.class.getSimpleName() + File.separator + "xmlsequence.seq";
	private static final String JSON_SEQUENCE_FILE = SESSequenceHelperTest.class.getSimpleName() + File.separator +"jsonsequence.seq";

	private static final String REGION_NAME = "getName";
	private static final String REGION_ID = "getRegionId";
	private static final String REGION_IS_ENABLED = "isEnabled";
	private static final String REGION_LENS_MODE = "getLensMode";
	private static final String REGION_PASS_ENERGY = "getPassEnergy";
	private static final String REGION_EXCITATION_ENERGY_SOURCE = "getExcitationEnergySource";
	private static final String REGION_ACQUISITION_MODE = "getAcquisitionMode";
	private static final String REGION_SLICES = "getSlices";
	private static final String REGION_ENERGY_MODE = "getEnergyMode";
	private static final String REGION_FIX_ENERGY = "getFixEnergy";
	private static final String REGION_LOW_ENERGY = "getLowEnergy";
	private static final String REGION_HIGH_ENERGY = "getHighEnergy";
	private static final String REGION_ENERGY_STEP = "getEnergyStep";
	private static final String REGION_STEP_TIME = "getStepTime";
	private static final String REGION_FIRST_X_CHANNEL = "getFirstXChannel";
	private static final String REGION_LAST_X_CHANNEL = "getLastXChannel";
	private static final String REGION_FIRST_Y_CHANNEL = "getFirstYChannel";
	private static final String REGION_LAST_Y_CHANNEL = "getLastYChannel";
	private static final String REGION_DETECTOR_MODE = "getDetectorMode";
	private static final String REGION_TOTAL_STEPS = "getTotalSteps";
	private static final String REGION_TOTAL_TIME = "getTotalTime";
	private static final String REGION_ITERATIONS = "getIterations";

	private SESSettingsService settings;

	@BeforeEach
	public void setup() {
		settings = SetupSESSettingsTestHelper.setupFinderAndSESSettingsService();
		settings.setLegacyConversionExcitationEnergyForSESRegion(
			Map.ofEntries(
				entry(SetupSESSettingsTestHelper.SOURCE1, Arrays.asList(2100., Double.MAX_VALUE)),
				entry(SetupSESSettingsTestHelper.SOURCE2, Arrays.asList(Double.MIN_VALUE, 2100.))
			)
		);
	}

	@AfterEach
	public void tearDown() {
		SetupSESSettingsTestHelper.tearDown();
	}

	public Map<String, Map<String, Object>> getExpectedRegionValues() {
		//Use LinkedHashMap so it remembers order so always tests in same order.
		final Map<String, Object> expectedRegion1Values = new LinkedHashMap<>();
		expectedRegion1Values.put(REGION_NAME, "New_Region");
		expectedRegion1Values.put(REGION_ID, "_aQOmgPsmEe6w2YUF3bV-LA");
		expectedRegion1Values.put(REGION_IS_ENABLED, true);
		expectedRegion1Values.put(REGION_LENS_MODE, "Angular56");
		expectedRegion1Values.put(REGION_PASS_ENERGY, 5);
		expectedRegion1Values.put(REGION_EXCITATION_ENERGY_SOURCE, SetupSESSettingsTestHelper.SOURCE2);
		expectedRegion1Values.put(REGION_ACQUISITION_MODE, SESRegion.SWEPT);
		expectedRegion1Values.put(REGION_SLICES, 1);
		expectedRegion1Values.put(REGION_ENERGY_MODE, SESRegion.KINETIC);
		expectedRegion1Values.put(REGION_FIX_ENERGY, 9.);
		expectedRegion1Values.put(REGION_LOW_ENERGY, 100.);
		expectedRegion1Values.put(REGION_HIGH_ENERGY, 101.);
		expectedRegion1Values.put(REGION_ENERGY_STEP, 200.);
		expectedRegion1Values.put(REGION_STEP_TIME, 1.);
		expectedRegion1Values.put(REGION_FIRST_X_CHANNEL, 1);
		expectedRegion1Values.put(REGION_LAST_X_CHANNEL, 1000);
		expectedRegion1Values.put(REGION_FIRST_Y_CHANNEL, 101);
		expectedRegion1Values.put(REGION_LAST_Y_CHANNEL, 800);
		expectedRegion1Values.put(REGION_DETECTOR_MODE, SESRegion.ADC);
		expectedRegion1Values.put(REGION_TOTAL_STEPS, 8.);
		expectedRegion1Values.put(REGION_TOTAL_TIME, 8.);
		expectedRegion1Values.put(REGION_ITERATIONS, 1);

		//Use LinkedHashMap so it remembers order so always tests in same order.
		final Map<String, Object> expectedRegion2Values = new LinkedHashMap<>();
		expectedRegion2Values.put(REGION_NAME, "New_Region1");
		expectedRegion2Values.put(REGION_ID, "_aQOmgPsmEe6w2YUF3GV-LL");
		expectedRegion2Values.put(REGION_IS_ENABLED, false);
		expectedRegion2Values.put(REGION_LENS_MODE, "Angular45");
		expectedRegion2Values.put(REGION_PASS_ENERGY, 10);
		expectedRegion2Values.put(REGION_EXCITATION_ENERGY_SOURCE, SetupSESSettingsTestHelper.SOURCE1);
		expectedRegion2Values.put(REGION_ACQUISITION_MODE, SESRegion.FIXED);
		expectedRegion2Values.put(REGION_SLICES, 10);
		expectedRegion2Values.put(REGION_ENERGY_MODE, SESRegion.BINDING);
		expectedRegion2Values.put(REGION_FIX_ENERGY, 4900.);
		expectedRegion2Values.put(REGION_LOW_ENERGY, 4899.5615);
		expectedRegion2Values.put(REGION_HIGH_ENERGY, 4900.4385);
		expectedRegion2Values.put(REGION_ENERGY_STEP, 0.877);
		expectedRegion2Values.put(REGION_STEP_TIME, 0.882);
		expectedRegion2Values.put(REGION_FIRST_X_CHANNEL, 4);
		expectedRegion2Values.put(REGION_LAST_X_CHANNEL, 990);
		expectedRegion2Values.put(REGION_FIRST_Y_CHANNEL, 110);
		expectedRegion2Values.put(REGION_LAST_Y_CHANNEL, 795);
		expectedRegion2Values.put(REGION_DETECTOR_MODE, SESRegion.PULSE_COUNTING);
		expectedRegion2Values.put(REGION_TOTAL_STEPS, 1.);
		expectedRegion2Values.put(REGION_TOTAL_TIME, 4.41);
		expectedRegion2Values.put(REGION_ITERATIONS, 5);

		return Map.of(
			"New_Region", expectedRegion1Values,
			"New_Region1", expectedRegion2Values
		);
	}

	static Stream<Arguments> sequenceFileParameters() {
		return Stream.of(
			Arguments.of(XML_SEQUENCE_FILE, false),
			Arguments.of(JSON_SEQUENCE_FILE, true)
		);
	}

	@ParameterizedTest(name = "sequenceFile = {0}, isJsonFile = {1}")
	@MethodSource("sequenceFileParameters")
	public void sequenceFileParameters(final String sequenceFile, final boolean isJsonFile) throws Exception {
		final Map<String, Map<String, Object>> expectedRegionValues = getExpectedRegionValues();
		final String expectedElementSet = "Low";

		final String filePath = SESSequenceHelper.getDefaultFilePath() + File.separator + sequenceFile;

		assertThat(SESSequenceHelper.isFileJSONFormat(filePath), is(equalTo(isJsonFile)));
		assertThat(SESSequenceHelper.isFileXMLFormat(filePath), is(equalTo(!isJsonFile)));

		final SESSequence sequence = isJsonFile ?
			SESSequenceHelper.loadSequence(filePath) :
			SESSequenceHelper.convertSequenceFileFromXMLToJSON(filePath);

		assertThat(sequence.getElementSet(), is(equalTo(expectedElementSet)));
		assertThat(sequence.getRegions().size(), is(equalTo(expectedRegionValues.size())));

		final List<String> expectedEnabledRegionNames = expectedRegionValues.entrySet().stream()
			.filter(entry -> (boolean) entry.getValue().get(REGION_IS_ENABLED))
			.map(Entry::getKey)
			.toList();
		assertThat(sequence.getEnabledRegionNames(), is(equalTo(expectedEnabledRegionNames)));

		for (final SESRegion region : sequence.getRegions()) {
			for (final Map.Entry<String, Object> entry : expectedRegionValues.get(region.getName()).entrySet()) {
				final String function = entry.getKey();
				final Class<?> c = Class.forName(SESRegion.class.getName());
				final Method method = c.getDeclaredMethod(function);
				final Object regionValue = method.invoke(region);
				final Object expectedRegionValue = entry.getValue();
				logger.info("Testing region {} method {}. Expected region value: {}, actual region value: {}", region.getName(), function, expectedRegionValue, regionValue);
				assertThat(regionValue, is(equalTo(expectedRegionValue)));
			}
		}
		for (final SESExcitationEnergySource expectedExcitationEnergySource : settings.getSESExcitationEnergySourceList()) {
			final SESExcitationEnergySource sequenceExcitationEnergySource = sequence.getExcitationEnergySourceByName(expectedExcitationEnergySource.getName());
			assertNotNull(sequenceExcitationEnergySource);
			assertThat(sequenceExcitationEnergySource.getName(), is(equalTo(expectedExcitationEnergySource.getName())));
			assertThat(sequenceExcitationEnergySource.getScannableName(), is(equalTo(expectedExcitationEnergySource.getScannableName())));
		}
	}
}