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

import static org.eclipse.scanning.jython.JythonInterpreterManager.getBundleLocation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.jython.JythonInterpreterManager;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.eclipse.scanning.test.utilities.scan.mock.DummyOperationBean;
import org.python.util.PythonInterpreter;


public abstract class AbstractJythonTest extends BrokerTest{

	protected static PythonInterpreter pi;

	public AbstractJythonTest() {
		super();
	}
	public AbstractJythonTest(boolean start) {
		super(start);
	}

	protected static IMarshallerService createMarshaller() {
		return new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new ScanningTestClassRegistry(DummyOperationBean.class)),
				Arrays.asList(new PointsModelMarshaller())
				);
	}

	static {
		JythonInterpreterManager.setupSystemState();
		try {
			JythonInterpreterManager.addPath(getBundleLocation("org.eclipse.scanning.command") + "/scripts");
		pi = new PythonInterpreter();
		pi.exec("from mapping_scan_commands import *");
		pi.exec("from mapping_scan_commands import _instantiate");
		pi.exec("from org.eclipse.scanning.example.detector import MandelbrotModel");
		pi.set("my_scannable", new MockScannable("fred", 10));
		pi.set("another_scannable", new MockScannable("bill", 3));
		pi.exec("mandelbrot = lambda t: ('mandelbrot',"
			+	"_instantiate(MandelbrotModel,"
			+	"{'exposureTime': t, 'name': 'mandelbrot'}))");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
