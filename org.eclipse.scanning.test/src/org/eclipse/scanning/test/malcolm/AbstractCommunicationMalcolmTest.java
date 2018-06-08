/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.malcolm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * TODO DAQ-1004 see comment in superclass
 */
@RunWith(Parameterized.class)
public abstract class AbstractCommunicationMalcolmTest extends AbstractMalcolmTest {

	private static final int REPEAT_COUNT = 1;
	private static final int MESSAGE_GRACE = 500;

	@Parameterized.Parameters
	public static List<Object[]> data() {
	    return Arrays.asList(new Object[REPEAT_COUNT][0]);
	}

	@Test
	public void testBasicRunPausableDevice() throws Exception {
		basicRun(device);
	}

	private void basicRun(IMalcolmDevice zebra) throws MalcolmDeviceException, Exception {

		configure(zebra, 10);
		zebra.run(null); // blocks until finished

		final DeviceState state = zebra.getDeviceState();

		if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");
	}

	@Test
	public void testStartAndStopEventsPausableDevice() throws Exception {
		startAndStopEvents(device);
	}

	private void startAndStopEvents(IMalcolmDevice zebra) throws MalcolmDeviceException, InterruptedException, Exception {

		final List<MalcolmEvent> beans = new ArrayList<MalcolmEvent>(IMAGE_COUNT);
		zebra.addMalcolmListener(new IMalcolmEventListener() {
			@Override
			public void eventPerformed(MalcolmEvent event) {
				System.out.println(event.isScanStart());
				System.out.println(event.isScanEnd());
				if (event.isScanEnd() || event.isScanStart()) {
				    beans.add(event);
			}
			}
		});

		configure(zebra, IMAGE_COUNT);
		zebra.run(null); 						// blocks until finished
		Thread.sleep(MESSAGE_GRACE);		// allow for messaging delays

		if (beans.size()!=2) throw new Exception("Scan start and end not encountered!");

		final DeviceState state = zebra.getDeviceState();

		if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");
	}


	@Test
	public void testMalcolmEventsPausableDevice() throws Exception {
		runMalcolmEvents(device);
	}

	private void runMalcolmEvents(IMalcolmDevice zebra) throws Exception {

		final boolean[] scanHasStarted = {false};

		final List<MalcolmEvent> beans = new ArrayList<MalcolmEvent>(IMAGE_COUNT);
		zebra.addMalcolmListener(new IMalcolmEventListener() {
			@Override
			public void eventPerformed(MalcolmEvent event) {
				if (event.isScanStart()) {
					scanHasStarted[0] = true;
				}
				if (MalcolmUtil.isScanning(event) && scanHasStarted[0]) {
				    beans.add(event);
			}
			}
		});

		configure(zebra, IMAGE_COUNT);
		zebra.run(null); 						// blocks until finished
		Thread.sleep(MESSAGE_GRACE);		// allow for messaging delays

		// There is one extra event as the state is set to Running before scan start
		if (beans.size()!=IMAGE_COUNT) {
			throw new Exception("Unexpected number of images written! Expected: "+IMAGE_COUNT+" got "+beans.size());
		}

		final DeviceState state = zebra.getDeviceState();

		if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");
	}

	@Test
	public void testAbortIdleRunnableDevice() throws Throwable {

		try {
			final IMalcolmDevice     zebra = (IMalcolmDevice) service.getRunnableDevice("zebra");
			zebra.abort();
		} catch (Exception expected) {
			return;
		}
		throw new Exception(DeviceState.READY+" did not throw an exception on aborting!");
	}

	@Test
	public void testAbortIdlePausableDevice() throws Throwable {

		try {
			final IMalcolmDevice     zebra = (IMalcolmDevice) service.getRunnableDevice("zebra");
			zebra.abort();
		} catch (Exception expected) {
			return;
		}
		throw new Exception(DeviceState.READY+" did not throw an exception on aborting!");
	}

}
