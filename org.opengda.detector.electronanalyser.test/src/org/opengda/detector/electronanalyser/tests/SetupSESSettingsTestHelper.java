package org.opengda.detector.electronanalyser.tests;

import java.util.Arrays;

import org.opengda.detector.electronanalyser.api.SESConfigExcitationEnergySource;
import org.opengda.detector.electronanalyser.api.SESSettings;
import org.opengda.detector.electronanalyser.api.SESSettingsService;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.scannable.DummyScannable;
import gda.factory.Factory;
import gda.factory.Finder;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class SetupSESSettingsTestHelper {

	public static final String SOURCE1 = "source1";
	public static final String SOURCE2 = "source2";
	public static final String SOURCE1_SCANNABLE = "dcmenergyEv";
	public static final String SOURCE2_SCANNABLE = "pgmenergy";
	private static final String SEQUENCE_PATH = "./testfiles/sequencefiles";
	private static final Factory factory = TestHelpers.createTestFactory();

	private SetupSESSettingsTestHelper() {}

	public static SESSettingsService setupFinderAndSESSettingsService() {
		Finder.addFactory(factory);

		LocalProperties.set("gda.ses.electronanalyser.seq.dir", SEQUENCE_PATH);

		final DummyScannable dcmenergy = new DummyScannable(SOURCE1_SCANNABLE);
		getFactory().addFindable(dcmenergy);
		final DummyScannable pgmenergy = new DummyScannable(SOURCE2_SCANNABLE);
		getFactory().addFindable(pgmenergy);

		final SESSettings settings = new SESSettings("SESSettings");
		final SESConfigExcitationEnergySource source1 = new SESConfigExcitationEnergySource(SOURCE1, dcmenergy.getName());
		final SESConfigExcitationEnergySource source2 = new SESConfigExcitationEnergySource(SOURCE2, pgmenergy.getName());
		settings.setSESConfigExcitationEnergySourceList(Arrays.asList(source1, source2));

		ServiceProvider.setService(SESSettingsService.class, settings);

		return settings;
	}

	public static void tearDown() {
		ServiceProvider.removeService(SESSettingsService.class);
		Finder.removeAllFactories();
	}

	public static Factory getFactory() {
		return factory;
	}

}