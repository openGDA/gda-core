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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static gda.configuration.properties.LocalProperties.GDA_ACTIVEMQ_BROKER_URI;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.factory.Factory;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.scanning.BeamScannable;

public class BeamPositionPlotterTest {

	private BeamPositionPlotter plotter;
	private MappingStageInfo mapInfo;
	private IRegion mockRegion = mock(IRegion.class);
	private IPlottingService plotService = mock(IPlottingService.class);
	private IEventService eventService = mock(IEventService.class);
	private IScannableDeviceService deviceService = mock(IScannableDeviceService.class);

	@SuppressWarnings("unchecked")
	private IPlottingSystem<Object> mockMapPlotServ = mock(IPlottingSystem.class);

	private double xPos = 0.1;
	private double yPos = 0.4;
	private double beamDim = 0.6;

	private Scannable xAxisScan= new DummyScannable("stage_x", xPos);
	private Scannable yAxisScan = new DummyScannable("stage_y", yPos);
	private BeamScannable beamSize;
	private Factory mockFactory = mock(Factory.class);

	private boolean setUp = false;

	@Before
	public void setUp() throws Exception {
		if (!setUp) {
			LocalProperties.set(GDA_ACTIVEMQ_BROKER_URI, "DummyURI");
			when(mockFactory.getFindable("stage_x")).thenReturn(xAxisScan);
			when(mockFactory.getFindable("stage_y")).thenReturn(yAxisScan);
			when(eventService.createRemoteService(any(), eq(IScannableDeviceService.class))).thenReturn(deviceService);

			when(plotService.getPlottingSystem("Map")).thenReturn(mockMapPlotServ);
			when(mockMapPlotServ.getRegion(BeamPositionPlotter.POSITION_MARKER_NAME)).thenReturn(mockRegion);
			Finder.getInstance().addFactory(mockFactory);
			setUp = true;
		}
		mapInfo = new MappingStageInfo();
		mapInfo.setPlotXAxisName("stage_x");
		mapInfo.setPlotYAxisName("stage_y");
		plotter = new BeamPositionPlotter();
		plotter.setMappingStageInfo(mapInfo);
		plotter.setPlottingService(plotService);
		plotter.setEventService(eventService);
		mapInfo.setBeamSize("beam");

		beamSize = new BeamScannable();
		beamSize.setBeamSize(beamDim);
		// Bounded wildcard...
		doReturn(beamSize).when(deviceService).getScannable(any());

	}

	@After
	public void tearDown() {
		Finder.getInstance().removeAllFactories();
		LocalProperties.clearProperty(GDA_ACTIVEMQ_BROKER_URI);
	}

	@Test
	public void plotWithBeamSize() {
		plotter.init();
		verify(mockRegion).setROI(new CircularROI(beamDim, xPos, yPos));
	}

	@Test
	public void plotWithoutABeamsizeSet() {
		mapInfo.setBeamSize(null);
		plotter.init();
		verify(mockRegion, never()).setROI(any());
	}

	@Test
	public void plotWithBeamsizeUnfound() throws ScanningException {
		beamSize = null;
		// Bounded wildcard...
		doReturn(beamSize).when(deviceService).getScannable(any());
		plotter.init();
		verify(mockRegion, never()).setROI(any());
	}

	@Test
	public void plotWithoutNullBeamsizeThenSetBeamSize() {
		mapInfo.setBeamSize(null);
		plotter.init();
		mapInfo.setBeamSize("beam");
		verify(mockRegion).setROI(new CircularROI(beamDim, xPos, yPos));
	}

}
