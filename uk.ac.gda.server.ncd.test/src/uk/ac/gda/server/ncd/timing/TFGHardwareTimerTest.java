/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.timing;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gda.device.timer.Etfg;
import uk.ac.gda.server.ncd.timing.data.TFGCoreConfiguration;
import uk.ac.gda.server.ncd.timing.data.TFGGroupConfiguration;

public class TFGHardwareTimerTest {
	private static final List<Double> thresholds = new ArrayList<>(Arrays.asList(Double.NaN, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.NaN, Double.NaN, Double.NaN,
			Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0.0 ));

	private static final List<Double> debounce = new ArrayList<>(Arrays.asList(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ));
	private static final int inversion = 192;
	private static final int drive = 0;
	private static final int startMethod = 0;
	private static final boolean extInhibit = false;
	private static final int cycles = 1;
	// Frame set
	private static final int deadPort = 4;
	private static final int livePort = 255;
	private static final int deadPause = 0;
	private static final int livePause = 0;

	private static final double minimumExposure = 0.003;
	private static final double minimumDelay = 0.001;
	private static final double fastShutterOpeningTime = 0.2;

	@Mock
	private Etfg tfg;

	private TFGHardwareTimer tfgHardwareTimer;

	@Before
	public void setup () {
		MockitoAnnotations.initMocks(this);
		
		TFGCoreConfiguration tfgCoreConfiguration = new TFGCoreConfiguration();
		tfgCoreConfiguration.setThresholds(thresholds);
		tfgCoreConfiguration.setDebounce(debounce);
		tfgCoreConfiguration.setInversion(inversion);
		tfgCoreConfiguration.setDrive(drive);
		tfgCoreConfiguration.setStartMethod(startMethod);
		tfgCoreConfiguration.setExtInhibit(extInhibit);
		tfgCoreConfiguration.setCycles(cycles);

		TFGGroupConfiguration tfgGroupConfiguration = new TFGGroupConfiguration ();
		tfgGroupConfiguration.setFrameCount(1);
		tfgGroupConfiguration.setDeadTime(0.1);
		tfgGroupConfiguration.setLiveTime(0.1);
		tfgGroupConfiguration.setDeadPort(deadPort);
		tfgGroupConfiguration.setLivePort(livePort);
		tfgGroupConfiguration.setDeadPause(deadPause);
		tfgGroupConfiguration.setLivePause(livePause);

		tfgHardwareTimer = new TFGHardwareTimer(tfg, tfgCoreConfiguration, tfgGroupConfiguration, 
				minimumExposure, minimumDelay, fastShutterOpeningTime);

		when(tfg.getMaximumFrames()).thenReturn(255);
	}

	@Test
	public void configureTimer_initialise () throws Exception {
		tfgHardwareTimer.configureTimer(10,  10,  10);

		verify(tfg).setAttribute("Debounce", debounce);
		verify(tfg).setAttribute("Threshold", thresholds);
		verify(tfg).setAttribute("Inversion", inversion);
		verify(tfg).setAttribute("Drive", drive);
		verify(tfg).setAttribute("Start-Method", startMethod);
		verify(tfg).setAttribute("Ext-Inhibit", extInhibit);
		verify(tfg).setCycles(cycles);
		
		assertTrue("Minuium Delay", tfgHardwareTimer.getMinimumDelay() == minimumDelay);
	}
	
	@Test
	public void configureTimer_addSingleExposure () throws Exception {
		ArgumentCaptor<Integer> frameCountCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Double> requestedDeadTimeCapture = ArgumentCaptor.forClass(Double.class);
		ArgumentCaptor<Double> requestedLiveTimeCapture = ArgumentCaptor.forClass(Double.class);
		ArgumentCaptor<Integer> deadPortCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> livePortCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> deadPauseCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> livePauseCapture = ArgumentCaptor.forClass(Integer.class);

		tfgHardwareTimer.configureTimer(1, minimumExposure * 2, minimumDelay * 2);

		verify(tfg, times(1)).addFrameSet(frameCountCapture.capture(), requestedDeadTimeCapture.capture(),
				requestedLiveTimeCapture.capture(), deadPortCapture.capture(),
				livePortCapture.capture(), deadPauseCapture.capture(), livePauseCapture.capture());
		
		assertTrue("Frames", frameCountCapture.getValue() == 1);
		assertTrue("Exposure length", requestedLiveTimeCapture.getValue() == minimumExposure * 2 * 1000);
		assertTrue("Minimum Delay not applied", requestedDeadTimeCapture.getValue() == fastShutterOpeningTime * 1000);
		assertTrue("Dead Port", deadPortCapture.getValue() == deadPort);
		assertTrue("Live Port", livePortCapture.getValue() == livePort);
		assertTrue("Dead Pause", deadPauseCapture.getValue() == deadPause);
		assertTrue("Live Pause", livePauseCapture.getValue() == livePause);
	}

	@Test
	public void configureTimer_addMultipleExposures () throws Exception {
		ArgumentCaptor<Integer> frameCountCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Double> requestedDeadTimeCapture = ArgumentCaptor.forClass(Double.class);
		ArgumentCaptor<Double> requestedLiveTimeCapture = ArgumentCaptor.forClass(Double.class);
		ArgumentCaptor<Integer> deadPortCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> livePortCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> deadPauseCapture = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> livePauseCapture = ArgumentCaptor.forClass(Integer.class);

		tfgHardwareTimer.configureTimer(3, minimumExposure * 2, minimumDelay * 2);
 
		verify(tfg, times(2)).addFrameSet(frameCountCapture.capture(), requestedDeadTimeCapture.capture(),
				requestedLiveTimeCapture.capture(), deadPortCapture.capture(),
				livePortCapture.capture(), deadPauseCapture.capture(), livePauseCapture.capture());
		
		assertTrue("Group 0 - Frames", frameCountCapture.getAllValues().get(0) == 1);
		assertTrue("Group 0 - Exposure", requestedLiveTimeCapture.getAllValues().get(0) == minimumExposure * 2 * 1000);
		assertTrue("Group 0 - Fast Shutter Delay not applied", requestedDeadTimeCapture.getAllValues().get(0) == fastShutterOpeningTime * 1000);
		assertTrue("Group 0 - Dead Port", deadPortCapture.getAllValues().get(0) == deadPort);
		assertTrue("Group 0 - Live Port", livePortCapture.getAllValues().get(0) == livePort);
		assertTrue("Group 0 - Dead Pause", deadPauseCapture.getAllValues().get(0) == deadPause);
		assertTrue("Group 0 - Live Pause", livePauseCapture.getAllValues().get(0) == livePause);
		
		assertTrue("Group 1 - Frames", frameCountCapture.getAllValues().get(1) == 2);
		assertTrue("Group 1 - Exposure", requestedLiveTimeCapture.getAllValues().get(1) == minimumExposure * 2 * 1000);
		assertTrue("Group 1 - Minimum Delay not applied", requestedDeadTimeCapture.getAllValues().get(1) == minimumDelay * 2 * 1000);
		assertTrue("Group 1 - Dead Port", deadPortCapture.getAllValues().get(1) == deadPort);
		assertTrue("Group 1 - Live Port", livePortCapture.getAllValues().get(1) == livePort);
		assertTrue("Group 1 - Dead Pause", deadPauseCapture.getAllValues().get(1) == deadPause);
		assertTrue("Group 1 - Live Pause", livePauseCapture.getAllValues().get(1) == livePause);
	}

	@Test (expected = HardwareTimerException.class)
	public void configureTimer_noExposures () throws Exception {
		tfgHardwareTimer.configureTimer(0, minimumExposure * 2, minimumDelay * 2);
	}

	@Test (expected = HardwareTimerException.class)
	public void configureTimer_exposureTooShort () throws Exception {
		tfgHardwareTimer.configureTimer(3, minimumExposure / 2, minimumDelay * 2);
	}

	@Test (expected = HardwareTimerException.class)
	public void configureTimer_delayTooShort () throws Exception {
		tfgHardwareTimer.configureTimer(3, minimumExposure * 2, minimumDelay / 2);
	}

	@Test
	public void configureTimer_delayTooShortButOnlyOneFrame () throws Exception {
		tfgHardwareTimer.configureTimer(1, minimumExposure * 2, minimumDelay / 2);
	}

	@Test
	public void configureTimer_previousFramesCleared () throws Exception {
		tfgHardwareTimer.configureTimer(1, minimumExposure * 2, minimumDelay * 2);
		tfgHardwareTimer.configureTimer(3, minimumExposure * 2, minimumDelay * 2);

		verify(tfg, times(2)).clearFrameSets();
		verify(tfg, times(3)).addFrameSet(anyInt(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt(), anyInt());
	}
}
