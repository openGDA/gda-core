/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.device.Scannable;
import uk.ac.diamond.daq.devices.specs.phoibos.SpecsPhoibosController;
import uk.ac.diamond.daq.devices.specs.phoibos.SpecsPhoibosSolsticeAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.SpecsPhoibosSolsticeCollectionStrategy;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;

public class SpecsPhoibosSolsticeAnalyserTest {


	private SpecsPhoibosSolsticeAnalyser analyser;
	private SpecsPhoibosController controller;
	private Scannable photonEnergyProvider;
	private SpecsPhoibosRegion region;

	@Before
	public void setup() throws Exception {
		analyser = new SpecsPhoibosSolsticeAnalyser();

		// Mock the EPICS layer
		controller = mock(SpecsPhoibosController.class);
		analyser.setController(controller);
		analyser.setCollectionStrategy(new SpecsPhoibosSolsticeCollectionStrategy());

		// Setup the BE to KE conversion\
		photonEnergyProvider = mock(Scannable.class);
		when(photonEnergyProvider.getPosition()).thenReturn(500.0);
		analyser.setPhotonEnergyProvider(photonEnergyProvider);
		analyser.setWorkFunction(4.5); // Set an example work function
		analyser.configure(); // Updates the photon energy

		region = new SpecsPhoibosRegion();
		region.setName("C1s");
		region.setAcquisitionMode("Fixed Transmission");
		region.setEndEnergy(777.0);
		region.setExposureTime(0.2);
		region.setIterations(7);
		region.setLensMode("Small Area");
		region.setPassEnergy(50.0);
		region.setPsuMode("1.5kV");
		region.setBindingEnergy(false); // Always readback from analyser KE
		region.setStartEnergy(666.0);
		region.setStepEnergy(0.5);
		region.setValues(1);
		region.setCentreEnergy(721.5);
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
		String dummyPath = "/dummy/path";
		analyser.setSequence(sequence, dummyPath);
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
		region.setStartEnergy(200.0);
		region.setEndEnergy(100.0);

		// Set the region on the analyser will cause the controller to be called to set the energy range
		analyser.setRegion(region);

		verify(controller, times(1)).setLowEnergy(100.0);
		verify(controller, times(1)).setHighEnergy(200.0);
	}

	@Test
	public void testSetIterationsInSetRegion() throws Exception {
		int iterations = 10;
		region.setIterations(iterations);
		analyser.setRegion(region);
		verify(controller, times(1)).setIterations(1);
		assertEquals(iterations, analyser.getRequestedIterations());
	}

	@Test
	public void testGetIterationCallsController() throws Exception {
		when(controller.getIterations()).thenReturn(region.getIterations());
		int newIterations = analyser.getIterations();
		assertEquals(newIterations,region.getIterations());
	}

	@Test
	public void testGetRequestedIterationCallsController() throws Exception {
		analyser.getRequestedIterations();
		verify(controller,never()).getIterations();
	}

	@Test
	public void testSetNullSequence() {
		SpecsPhoibosSolsticeCollectionStrategy mockedCollectionStrategy = mock(SpecsPhoibosSolsticeCollectionStrategy.class);
		analyser.setCollectionStrategy(mockedCollectionStrategy);
		analyser.setSequence(null, null);
		verify(mockedCollectionStrategy).setSequence(null);
	}

	@Test
	public void testGetRegionCallsController() throws Exception {
		analyser.getRegion();
		verify(controller, times(2)).getHighEnergy();
		verify(controller, times(2)).getLowEnergy();
		verify(controller, times(1)).getPsuMode();
		verify(controller, times(1)).getAcquisitionMode();
		verify(controller, times(1)).getLensMode();
		verify(controller, times(1)).getEnergyStep();
		verify(controller, times(1)).getExposureTime();
		verify(controller, times(1)).getIterations();
		verify(controller, times(1)).getPassEnergy();
		verify(controller, times(1)).getValues();
		verify(controller, never()).getCentreEnergy();
	}

	@Test
	public void testGetRegionValues() throws Exception {
		when(controller.getPsuMode()).thenReturn(region.getPsuMode());
		when(controller.getAcquisitionMode()).thenReturn(region.getAcquisitionMode());
		when(controller.getLensMode()).thenReturn(region.getLensMode());
		when(controller.getHighEnergy()).thenReturn(region.getEndEnergy());
		when(controller.getLowEnergy()).thenReturn(region.getStartEnergy());
		when(controller.getEnergyStep()).thenReturn(region.getStepEnergy());
		when(controller.getCentreEnergy()).thenReturn(region.getCentreEnergy());
		when(controller.getExposureTime()).thenReturn(region.getExposureTime());
		when(controller.getIterations()).thenReturn(region.getIterations());
		when(controller.getPassEnergy()).thenReturn(region.getPassEnergy());
		when(controller.getValues()).thenReturn(region.getValues());
		SpecsPhoibosRegion newRegion = analyser.getRegion();
		assertNotEquals(newRegion,region);
		assertNotEquals(newRegion.getName(), region.getName());
		region.setName(analyser.getCurrentRegionName());
		assertEquals(newRegion, region);
	}

	@Test
	public void testValidateSequenceWithValidRegion() throws Exception {
		// Create valid region
		SpecsPhoibosRegion region  = new SpecsPhoibosRegion();
		SpecsPhoibosSequence sequence = new SpecsPhoibosSequence();
		sequence.addRegion(region);
		when(controller.getScanValidityStatus()).thenReturn("NO_ALARM");
		SpecsPhoibosSequenceValidation validationErrors = analyser.validateSequence(sequence);
		assertTrue(validationErrors.isValid());
	}

	@Test
	public void testValidateSequenceWithInvalidRegion() throws Exception {
		// Create invalid region
		SpecsPhoibosRegion region  = new SpecsPhoibosRegion();
		SpecsPhoibosSequence sequence = new SpecsPhoibosSequence();
		sequence.addRegion(region);
		when(controller.getScanValidityStatus()).thenReturn("INVALID");
		SpecsPhoibosSequenceValidation validationErrors = analyser.validateSequence(sequence);
		assertFalse(validationErrors.isValid());
	}
}
