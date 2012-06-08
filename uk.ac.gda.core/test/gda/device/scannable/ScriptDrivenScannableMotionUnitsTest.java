/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.junit.Assert.*;
import gda.device.DeviceException;
import gda.device.motor.DummyMotor;
import gda.jython.ICommandRunner;
import gda.observable.IObserver;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class ScriptDrivenScannableMotionUnitsTest {

	ScannableMotor scannableMotor;
	ScriptDrivenScannableMotionUnits scannableUnderTest;
	ScannableStatus status;
	
	@Before
	public void setUp() throws Exception {
		scannableUnderTest = new ScriptDrivenScannableMotionUnits();
		scannableUnderTest.setCommandFormat("%5.5g");
		DummyMotor dummyMotor = new DummyMotor();
		dummyMotor.setName("dummyMotor");
		dummyMotor.configure();
		scannableMotor = new ScannableMotor();
		scannableMotor.setMotor(dummyMotor);
		scannableMotor.setName("scannableMotor");
		scannableMotor.setLowerGdaLimits(0.);
		scannableMotor.configure();
		scannableUnderTest.setScannable(scannableMotor);
		scannableUnderTest.setCommandRunner(new ICommandRunner() {
			
			@Override
			public boolean runsource(String command, String source) {
				return false;
			}
			
			@Override
			public void runScript(File script, String sourceName) {
			}
			
			@Override
			public void runCommand(String command, String scanObserver) {
			}
			
			//mimic runCommand in jython server which always spawns a new thread to run the command
			@Override
			public void runCommand(final String command) {
				Thread th = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Double newPos = Double.valueOf(command);
						try {
							scannableMotor.moveTo(newPos);
						} catch (DeviceException e) {
							e.printStackTrace();
						}
					}
				});
				th.start();
			}
			
			@Override
			public String locateScript(String scriptToRun) {
				return null;
			}
			
			@Override
			public String evaluateCommand(String command) {
				Double newPos = Double.valueOf(command);
				try {
					scannableMotor.moveTo(newPos);
					return "";
				} catch (DeviceException e) {
					return e.getMessage();
				}
			}
		});
		scannableUnderTest.afterPropertiesSet();
		scannableUnderTest.addIObserver(new IObserver() {
			
			@Override
			public void update(Object source, Object arg) {
				if ( arg instanceof ScannableStatus){
					status = (ScannableStatus)arg;
					System.out.println( "Status = " + status.getStatus());
				}
			}
		});
		
	}

	@Test
	public void testMoveTo() throws DeviceException {
		scannableUnderTest.moveTo(1.0f);
		assertFalse(scannableUnderTest.isBusy());
		assertEquals(ScannableStatus.IDLE, status.getStatus());
	}

	@Test
	public void testMoveToOutsideLimits() {
		try{
			scannableUnderTest.moveTo(-1.0f);
			fail("Limit violation not raised");
		} catch (DeviceException e){
			//do nothing
		}
	}
	
	@Test
	public void testAsynchronousMoveTo() throws DeviceException, InterruptedException {
		scannableUnderTest.asynchronousMoveTo(new Double[]{1.0d, 2.0d});
		Thread.sleep(50);
		assertTrue(scannableUnderTest.isBusy());
		assertEquals(ScannableStatus.BUSY, status.getStatus());
		long timeAtStart = System.currentTimeMillis();
		while( status.getStatus() == (ScannableStatus.BUSY)){
			Thread.sleep(50);
		}
		long timeAtEnd = System.currentTimeMillis();
		long timeTaken = timeAtEnd - timeAtStart;
		System.out.println( "TimeTaken = " + timeTaken);
		assertTrue( timeTaken > 950);
		assertTrue( timeTaken < 1100);
	}

	@Test
	public void testAsynchronousMoveToOutsideLimits() throws DeviceException {
		scannableUnderTest.asynchronousMoveTo(new Double[]{-1.0d, 2.0d});
	}

	
}

