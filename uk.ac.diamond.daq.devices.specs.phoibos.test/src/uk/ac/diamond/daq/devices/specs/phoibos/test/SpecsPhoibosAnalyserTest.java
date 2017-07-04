package uk.ac.diamond.daq.devices.specs.phoibos.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.device.Scannable;
import uk.ac.diamond.daq.devices.specs.phoibos.SpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.SpecsPhoibosCollectionStrategy;
import uk.ac.diamond.daq.devices.specs.phoibos.SpecsPhoibosController;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;

public class SpecsPhoibosAnalyserTest {

	private SpecsPhoibosAnalyser analyser;
	private SpecsPhoibosController controller;
	private Scannable photonEnergyProvider;

	@Before
	public void setup() throws Exception {
		analyser = new SpecsPhoibosAnalyser();

		// Mock the EPICS layer
		controller = mock(SpecsPhoibosController.class);
		analyser.setController(controller);
		analyser.setCollectionStrategy(new SpecsPhoibosCollectionStrategy());

		// Setup the BE to KE conversion
		photonEnergyProvider = mock(Scannable.class);
		when(photonEnergyProvider.getPosition()).thenReturn(500.0);
		analyser.setPhotonEnergyProvider(photonEnergyProvider);
		analyser.setWorkFunction(4.5); // Set an example work function
		analyser.configure(); // Updates the photon energy
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetSequenceDoesntAcceptASequenceWithDuplicateNames() {
		SpecsPhoibosSequence sequence = new SpecsPhoibosSequence();
		// Add 2 identical regions
		SpecsPhoibosRegion region1 = new SpecsPhoibosRegion();
		SpecsPhoibosRegion region2 = new SpecsPhoibosRegion();
		region2.setName("Region2"); // Change the name so we can get it into the sequence

		sequence.addRegion(region1);
		sequence.addRegion(region2);

		// With the reference we still hold set both names the same
		region1.setName("Test");
		region2.setName("Test");

		// This should throw when the sequence copy constructor is used
		analyser.setSequence(sequence);
	}

	@Test
	public void testEnergyAxis() throws Exception {
		when(controller.getLowEnergy()).thenReturn(50.0);
		when(controller.getHighEnergy()).thenReturn(70.0);
		when(controller.getEnergyChannels()).thenReturn(201);

		double[] energyAxis = analyser.getEnergyAxis();

		assertThat(energyAxis.length, is(equalTo(201)));
		assertThat(energyAxis[0], is(equalTo(50.0)));
		assertThat(energyAxis[200], is(equalTo(70.0)));
	}

	@Test
	public void testYAxis() throws Exception {
		when(controller.getStartY()).thenReturn(-10.0);
		when(controller.getEndY()).thenReturn(10.0);
		when(controller.getSlices()).thenReturn(5);

		double[] yAxis = analyser.getYAxis();

		assertThat(yAxis.length, is(equalTo(5)));
		assertThat(yAxis[0], is(equalTo(-8.0)));
		assertThat(yAxis[4], is(equalTo(8.0)));
		// Check the axis midpoint
		assertThat(yAxis[2], is(equalTo(0.0)));
	}

	@Test
	public void testBEtoKEConversion() {
		// KE = hv - BE - wf
		// 500 - 400 - 4.5 = 95.5
		assertThat(analyser.toKineticEnergy(400.0), is(equalTo(95.5)));

		// 500 - 333.3 - 4.5 = 162.2
		assertThat(analyser.toKineticEnergy(333.3), is(equalTo(162.2)));

		// 500 - (-3) - 4.5 = 498.5
		assertThat(analyser.toKineticEnergy(-3.0), is(equalTo(498.5)));
	}

	@Test
	public void testKEtoBEConversion() {
		// BE = hv - KE - wf
		// 500 - 400 - 4.5 = 95.5
		assertThat(analyser.toBindingEnergy(95.5), is(equalTo(400.0)));

		// 500 - 333.3 - 4.5 = 162.2
		assertThat(analyser.toBindingEnergy(162.2), is(equalTo(333.3)));

		// 500 - (-3) - 4.5 = 498.5
		assertThat(analyser.toBindingEnergy(498.5), is(equalTo(-3.0)));
	}

	@Test
	public void testSettingRegionWithStartEnergyGreaterThanEndEnergy() throws Exception {
		SpecsPhoibosRegion region = new SpecsPhoibosRegion();
		region.setStartEnergy(200.0);
		region.setEndEnergy(100.0);

		// Set the region on the analyser will cause the controller to be called to set the energy range
		analyser.setRegion(region);

		verify(controller, times(1)).setLowEnergy(100.0);
		verify(controller, times(1)).setHighEnergy(200.0);
	}

	@Test
	public void testLoadingSequenceFromAbsolutePath() {
		String sequencePath = this.getClass().getResource("test_sequence.seq").getFile();
		analyser.setSequenceFile(sequencePath);
	}

}
