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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.motor.DummyMotor;
import gda.jython.ICommandRunner;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.observable.IObserver;

/**
 * The way that these tests work, with a custom implementation of {@code ICommandRunner}, is quite sneaky. So here's
 * some explanation:
 *
 * <p>Usually a {@code ScriptDrivenScannableMotionUnits} ({@code SDSMU}) object works like this:
 * <ul>
 * <li>The {@code commandFormat} would be something like {@code "SCANNABLENAME(%5.5g)"}.</li>
 * <li>Both {@code SDSMU.moveTo} and {@code SDSMU.asynchronousMoveTo} build a command by calling {@code String.format}
 *     with the {@code commandFormat} and {@code position},
 *   resulting in something like {@code "SCANNABLENAME(12.34)"}.</li>
 * <li>{@code SDSMU.moveTo} calls {@code commandRunner.evaluateCommand} to execute that command, and expects the
 *     response to be an empty string or {@code "None"}.</li>
 * <li>{@code SDSMU.asynchronousMoveTo} calls {@code commandRunner.runCommand} to execute the command, and doesn't wait
 *     for anything.</li>
 * </ul>
 *
 * <p>For this test, the {@code commandFormat} is set to {@code "%5.5g"}.
 * <ul>
 * <li>The commands built by {@code SDSMU} are therefore just numbers, e.g. {@code "12.34"}.</li>
 * <li>The custom implementations of {@code evaluateCommand} and {@code runCommand} both expect to get just a
 *     number.</li>
 * <li>Both methods then call {@code moveTo} on the underlying motor with that numerical position.</li>
 * <li>{@code evaluateCommand} returns an empty string, to make {@code SDSMU.moveTo} think that no error has
 *     occurred.</li>
 * </ul>
 */
public class ScriptDrivenScannableMotionUnitsTest {

	private ScannableMotor scannableMotor;
	private ScriptDrivenScannableMotionUnits scannableUnderTest;
	private ScannableStatus status;

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
			public boolean runsource(String command) {
				return false;
			}

			@Override
			public CommandThreadEvent runScript(File script) {
				return null;
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
					System.out.println( "Status = " + status);
				}
			}
		});

	}

	@Test
	public void testMoveTo() throws DeviceException {
		scannableUnderTest.moveTo(1.0f);
		assertFalse(scannableUnderTest.isBusy());
		// assertEquals(ScannableStatus.IDLE, status.getStatus());  GDA-5083 this assert intermittently fails
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
		scannableUnderTest.moveTo(0);
		scannableUnderTest.asynchronousMoveTo(1.0d);
		Thread.sleep(50);
		assertTrue(scannableUnderTest.isBusy());
		assertEquals(ScannableStatus.BUSY, status);
		long timeAtStart = System.currentTimeMillis();
		while( status == ScannableStatus.BUSY){
			Thread.sleep(50);
		}
		long timeAtEnd = System.currentTimeMillis();
		long timeTaken = timeAtEnd - timeAtStart;
		System.out.println( "TimeTaken = " + timeTaken);
		assertTrue( timeTaken > 950);
		// assertTrue( timeTaken < 1100);  this assert can sometimes fail if the test machine is busy, so remove
	}

	@Test
	public void testAsynchronousMoveToOutsideLimits() throws DeviceException {
		scannableUnderTest.asynchronousMoveTo(-1.0d);
	}


}

