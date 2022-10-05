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
package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// TODO Remove this test along with the rest of the original mscan based code

@Disabled("DAQ-2832 These tests all relate to the original mscan implementation which is end of life and will be removed soon and also really rely on pi.exec "
		+ "completing the command within a timeout (which cannot be guaranteed) for scan requests functionality which is tested elsewhere")
public class ScanRequestTest extends AbstractScanCommandsTest {

	public ScanRequestTest() {
		super(false);
	}

	@Test
	public void testGridScan() throws Exception {
		pi.exec("sr = scan_request(grid(axes=('xNex', 'yNex'), start=(0, 0), stop=(10, 10), count=(2, 2), snake=True), det=detector('mandelbrot', 0.001))");
		runAndCheck("sr", false, 30);
	}

	@Disabled("DAQ-2088 Test fails due to Exception not thrown")
	@Test
	public void testGridScanWrongAxis() {
		pi.exec("sr = scan_request(grid(axes=('x', 'y'), start=(0, 0), stop=(10, 10), count=(2, 2), snake=True), det=detector('mandelbrot', 0.001))");
		assertThrows(Exception.class, () -> runAndCheck("sr", false, 30));
	}

	@Test
	public void testGridScanNoDetector() throws Exception {
		pi.exec("sr = scan_request(grid(axes=('xNex', 'yNex'), start=(0, 0), stop=(10, 10), count=(2, 2), snake=True))");
		runAndCheck("sr", false, 30);
	}

	@Test
	public void testGridWithROIScan() throws Exception {
		pi.exec("sr = scan_request(grid(axes=('xNex', 'yNex'), start=(0.0, 1.0), stop=(1.0, 2.0), count=(3, 2), snake=False, roi=[circ(origin=(0.0, 1.0), radius=2.0)]), det=detector('mandelbrot', 0.001))");
		runAndCheck("sr", false, 30);
	}

	@Test
	public void testGridScanWithBadTimeout() {
		pi.exec("sr = scan_request(grid(axes=('xNex', 'yNex'), start=(0, 0), stop=(10, 10), count=(2, 2), snake=True), det=detector('mandelbrot', 1.2, timeout=1))");
		assertThrows(Exception.class, () -> runAndCheck("sr", false, 1));
	}

	@Test
	public void testGridScanWithGoodTimeout() throws Exception {
		pi.exec("sr = scan_request(grid(axes=('xNex', 'yNex'), start=(0, 0), stop=(10, 10), count=(2, 2), snake=True), det=detector('mandelbrot', 1.2, timeout=2))");
		runAndCheck("sr", false, 30);
	}
}
