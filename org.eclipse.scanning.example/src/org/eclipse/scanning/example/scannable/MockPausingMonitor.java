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
package org.eclipse.scanning.example.scannable;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * Designed to monitor topup (pretty badly, just conceptually).
 * On a step divisible by ten, will force a wait
 * until imaginary topup value is reached.
 *
 * @author Matthew Gerring
 *
 */
public class MockPausingMonitor extends MockScannable {

	public MockPausingMonitor(String string, double d, int i) {
		super(string,d,i);
	}

	@Override
	public Number setPosition(Number position, IPosition loc) throws ScanningException {

		final int step = loc.getStepIndex();
		if (step%10==0) { // We wait
			System.out.println("Waiting for imaginary topup for 10ms ");
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ScanningException(e);
			}
			System.out.println("Bean current is now stable again... ");
		}
		return super.setPosition(position, loc);
	}

}
