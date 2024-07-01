/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.ATTRIBUTE_NAME_UNITS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_DIAMOND_SCAN;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_SPECTRUM;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXcontainer;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXshape;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.device.CommonBeamlineDevicesConfiguration;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

class MergeSampleGroupTest extends NexusTest {

	private static final class SampleNexusDevice implements INexusDevice<NXsample> {

		@Override
		public String getName() {
			return SAMPLE_DEVICE_NAME;
		}

		@Override
		public NexusObjectProvider<NXsample> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXsample sample = NexusNodeFactory.createNXsample();

			final NXbeam beam = NexusNodeFactory.createNXbeam();
			beam.setExtent(DatasetFactory.createFromObject(BEAM_EXTENT));
			beam.setIncident_energyScalar(BEAM_INCIDENT_ENERGY);
			beam.setAttribute(NXbeam.NX_INCIDENT_ENERGY, ATTRIBUTE_NAME_UNITS, UNITS_KILO_ELECTRON_VOLTS);
			beam.setIncident_polarization(DatasetFactory.createFromObject(BEAM_INCIDENT_POLARIZATION));
			beam.setIncident_wavelengthScalar(BEAM_INCIDENT_WAVELENGTH);
			beam.setAttribute(NXbeam.NX_INCIDENT_WAVELENGTH, ATTRIBUTE_NAME_UNITS, UNITS_ANGSTROM);
			sample.setBeam(beam);

			final NXcollection containersCollection = NexusNodeFactory.createNXcollection();
			sample.addGroupNode(GROUP_NAME_CONTAINERS, containersCollection);

			final NXcontainer container0 = NexusNodeFactory.createNXcontainer();
			container0.setChemical_formulaScalar(CONTAINER_0_CHEMICAL_FORMULA);
			container0.setNameScalar(CONTAINER_0_NAME);
			container0.setDensityScalar(CONTAINER_0_DENSITY);
			container0.setAttribute(NXsample.NX_DENSITY, ATTRIBUTE_NAME_UNITS, UNITS_GRAMS_PER_CUBIC_CM);
			containersCollection.addGroupNode(GROUP_NAME_CONTAINER_0, container0);

			final NXcontainer container1 = NexusNodeFactory.createNXcontainer();
			container1.setChemical_formulaScalar(CONTAINER_1_CHEMICAL_FORMULA);
			container1.setNameScalar(CONTAINER_1_NAME);
			container1.setDensityScalar(CONTAINER_1_DENSITY);
			container1.setAttribute(NXsample.NX_DENSITY, ATTRIBUTE_NAME_UNITS, UNITS_GRAMS_PER_CUBIC_CM);
			containersCollection.addGroupNode(GROUP_NAME_CONTAINER_1, container1);

			final NXshape shape = NexusNodeFactory.createNXshape();
			sample.addGroupNode(GROUP_NAME_SHAPE, shape);
			shape.setShapeScalar(SAMPLE_SHAPE_TYPE);
			shape.setShape(DatasetFactory.createFromObject(BEAM_SHAPE_SIZE));

			return new NexusObjectWrapper<>(getName(), sample);
		}
	}

	private IWritableDetector<MandelbrotModel> detector;

	private static final String SAMPLE_DEVICE_NAME = "sample";

	private static final String SAMPLE_CHEMICAL_FORMULA = "Si";
	private static final double SAMPLE_DENSITY = 2.329;
	private static final String SAMPLE_DESCRIPTION = "This is the sample";
	private static final int SAMPLE_ID = 4286741;
	private static final String SAMPLE_NAME = "Si_1mm_Vitrex";
	private static final double SAMPLE_VOLUME_FRACTION = 0.6;

	private static final String GROUP_NAME_BEAM = "beam";
	private static final double[] BEAM_EXTENT = { 1.52, 1.17 };
	private static final double BEAM_INCIDENT_ENERGY = 76.69;
	private static final double[] BEAM_INCIDENT_POLARIZATION = { 94.43, 63.15, -36.13, -5.23 };
	private static final double BEAM_INCIDENT_WAVELENGTH = 255.34;

	private static final String GROUP_NAME_SHAPE = "shape";
	private static final String SAMPLE_SHAPE_TYPE = "cylinder";
	private static final double[][] BEAM_SHAPE_SIZE = { { 43.34, 92.5 }, { 142.54, 90.0 } };

	private static final String GROUP_NAME_CONTAINERS = "containers";
	private static final String GROUP_NAME_CONTAINER_0 = "container0";
	private static final String CONTAINER_0_CHEMICAL_FORMULA = "Pb";
	private static final String CONTAINER_0_NAME = "outer container";
	private static final double CONTAINER_0_DENSITY = 21.0;
	private static final String GROUP_NAME_CONTAINER_1 = "container1";
	private static final String CONTAINER_1_CHEMICAL_FORMULA = "Si";
	private static final String CONTAINER_1_NAME = "inner container";
	private static final double CONTAINER_1_DENSITY = 1.0;

	private static final String UNITS_GRAMS_PER_CUBIC_CM = "g / cm**3";
	private static final String UNITS_KILO_ELECTRON_VOLTS = "keV";
	private static final String UNITS_ANGSTROM = "Angstrom";

	@BeforeEach
	void before() throws Exception {
		final MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);

		final CommonBeamlineDevicesConfiguration beamlineConfig = new CommonBeamlineDevicesConfiguration();
		beamlineConfig.addAdditionalDeviceName(SAMPLE_DEVICE_NAME);
		CommonBeamlineDevicesConfiguration.setInstance(beamlineConfig);

		final SampleNexusDevice sampleNexusDevice = new SampleNexusDevice();
		ServiceProvider.getService(INexusDeviceService.class).register(sampleNexusDevice);
	}

	@Test
	void testMergeSampleGroup() throws Exception {
		// this test fixes DAQ-3678 (https://jira.diamond.ac.uk/browse/DAQ-3678), where if we add metadata to the sample
		// using a NexusMetadataProvider (a ScanMetadata object in the ScanModel is converted into a NexusMetadataProvider
		// in the NexusScanModel), then also have an INexusDevice that creates a NXsample group, we ensure that
		// fully merge the contents of the new NXsample group into the existing one.

		final ScanModel scanModel = createLinearScanModel(detector, output, 5);
		addSampleMetadata(scanModel);

		final IScanDevice scanner = scanService.createScanDevice(scanModel);

		scanner.run();

		checkNexusFile(scanner);
	}

	private void addSampleMetadata(ScanModel scanModel) {
		final ScanMetadata sampleMetadata = new ScanMetadata();
		sampleMetadata.setType(MetadataType.SAMPLE);
		sampleMetadata.addField(NXsample.NX_CHEMICAL_FORMULA, SAMPLE_CHEMICAL_FORMULA);
		sampleMetadata.addField(NXsample.NX_DENSITY, SAMPLE_DENSITY);
		sampleMetadata.addField(NXsample.NX_DESCRIPTION, SAMPLE_DESCRIPTION);
		sampleMetadata.addField("id", SAMPLE_ID);
		sampleMetadata.addField(NXsample.NX_NAME, SAMPLE_NAME);
		sampleMetadata.addField(NXsample.NX_VOLUME_FRACTION, SAMPLE_VOLUME_FRACTION);

		scanModel.setScanMetadata(List.of(sampleMetadata));
	}

	private void checkNexusFile(IScanDevice scanner) throws Exception {
		final NXroot root = getNexusRoot(scanner);
		assertThat(root, is(notNullValue()));

		final NXentry entry = root.getEntry();
		assertThat(entry, is(notNullValue()));

		final String detName = detector.getName();
		assertThat(entry.getGroupNodeNames(), containsInAnyOrder(GROUP_NAME_DIAMOND_SCAN, "instrument", "sample",
				detName, detName + "_" + FIELD_NAME_SPECTRUM, detName + "_" + FIELD_NAME_VALUE));

		final NXinstrument instrument = entry.getInstrument();
		checkInstrument(instrument);

		final NXsample sample = entry.getSample();
		checkSample(sample);

		assertThat(entry.getData(detName), both(notNullValue()).and(instanceOf(NXdata.class)));
		assertThat(entry.getData(detName + "_" + FIELD_NAME_SPECTRUM), both(notNullValue()).and(instanceOf(NXdata.class)));
		assertThat(entry.getData(detName + "_" + FIELD_NAME_VALUE), both(notNullValue()).and(instanceOf(NXdata.class)));
	}

	private void checkInstrument(NXinstrument instrument) {
		assertThat(instrument, is(notNullValue()));

		// no need to check detector and positioner group further for this test
		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(detector.getName(), "xNex", "yNex"));
		final NXdetector detGroup = instrument.getDetector(detector.getName());
		assertThat(detGroup, is(notNullValue()));
		assertThat(detGroup.getDataNode(NXdetector.NX_DATA), is(notNullValue()));
		assertThat(detGroup.getDataNode(FIELD_NAME_SPECTRUM), is(notNullValue()));
		final NXpositioner xPos = instrument.getPositioner("xNex");
		assertThat(xPos.getDataNode(NXpositioner.NX_VALUE), is(notNullValue()));
		final NXpositioner yPos = instrument.getPositioner("yNex");
		assertThat(yPos.getDataNode(NXpositioner.NX_VALUE), is(notNullValue()));
	}

	private void checkSample(NXsample sample) throws DatasetException {
		assertThat(sample, is(notNullValue()));

		assertThat(sample.getDataNodeNames(), containsInAnyOrder(
				NXsample.NX_CHEMICAL_FORMULA, NXsample.NX_DESCRIPTION, NXsample.NX_DENSITY, "id",
				NXsample.NX_NAME, NXsample.NX_VOLUME_FRACTION));

		assertThat(sample.getChemical_formulaScalar(), is(equalTo(SAMPLE_CHEMICAL_FORMULA)));
		assertThat(sample.getDensityScalar(), is(equalTo(SAMPLE_DENSITY)));
		assertThat(sample.getDescriptionScalar(), is(equalTo(SAMPLE_DESCRIPTION)));
		assertThat(sample.getDataNode("id").getDataset().getSlice().getInt(), is(equalTo(SAMPLE_ID)));
		assertThat(sample.getNameScalar(), is(equalTo(SAMPLE_NAME)));
		assertThat(sample.getVolume_fractionScalar(), is(equalTo(SAMPLE_VOLUME_FRACTION)));

		assertThat(sample.getGroupNodeNames(), containsInAnyOrder(GROUP_NAME_BEAM, GROUP_NAME_SHAPE, GROUP_NAME_CONTAINERS));

		final NXbeam beam = sample.getBeam();
		assertThat(beam, is(notNullValue()));
		assertThat(beam.getDataNodeNames(), containsInAnyOrder(NXbeam.NX_EXTENT,
				NXbeam.NX_INCIDENT_ENERGY, NXbeam.NX_INCIDENT_POLARIZATION, NXbeam.NX_INCIDENT_WAVELENGTH));
		assertThat(beam.getExtent(), is(equalTo(DatasetFactory.createFromObject(BEAM_EXTENT))));
		assertThat(beam.getIncident_energyScalar(), is(equalTo(BEAM_INCIDENT_ENERGY)));
		assertThat(beam.getAttrString(NXbeam.NX_INCIDENT_ENERGY, ATTRIBUTE_NAME_UNITS),
				is(equalTo(UNITS_KILO_ELECTRON_VOLTS)));
		assertThat(beam.getIncident_wavelengthScalar(), is(equalTo(BEAM_INCIDENT_WAVELENGTH)));
		assertThat(beam.getAttrString(NXbeam.NX_INCIDENT_WAVELENGTH, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_ANGSTROM)));

		final NXcollection containersCollection = (NXcollection) sample.getGroupNode(GROUP_NAME_CONTAINERS);
		assertThat(containersCollection, is(notNullValue()));
		assertThat(containersCollection.getGroupNodeNames(), containsInAnyOrder(
				GROUP_NAME_CONTAINER_0, GROUP_NAME_CONTAINER_1));

		final NXcontainer container0 = (NXcontainer) containersCollection.getGroupNode(GROUP_NAME_CONTAINER_0);
		assertThat(container0, is(notNullValue()));
		assertThat(container0.getDataNodeNames(), containsInAnyOrder(
				NXcontainer.NX_CHEMICAL_FORMULA, NXcontainer.NX_NAME, NXcontainer.NX_DENSITY));
		assertThat(container0.getChemical_formulaScalar(), is(equalTo(CONTAINER_0_CHEMICAL_FORMULA)));
		assertThat(container0.getNameScalar(), is(equalTo(CONTAINER_0_NAME)));
		assertThat(container0.getDensityScalar(), is(equalTo(CONTAINER_0_DENSITY)));
		assertThat(container0.getAttrString(NXsample.NX_DENSITY, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_GRAMS_PER_CUBIC_CM)));

		final NXcontainer container1 = (NXcontainer) containersCollection.getGroupNode(GROUP_NAME_CONTAINER_1);
		assertThat(container1, is(notNullValue()));
		assertThat(container1.getDataNodeNames(), containsInAnyOrder(
				NXcontainer.NX_CHEMICAL_FORMULA, NXcontainer.NX_NAME, NXcontainer.NX_DENSITY));
		assertThat(container1.getChemical_formulaScalar(), is(equalTo(CONTAINER_1_CHEMICAL_FORMULA)));
		assertThat(container1.getNameScalar(), is(equalTo(CONTAINER_1_NAME)));
		assertThat(container1.getDensityScalar(), is(equalTo(CONTAINER_1_DENSITY)));
		assertThat(container1.getAttrString(NXsample.NX_DENSITY, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_GRAMS_PER_CUBIC_CM)));
	}

}
