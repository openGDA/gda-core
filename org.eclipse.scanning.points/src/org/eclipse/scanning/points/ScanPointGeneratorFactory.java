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

package org.eclipse.scanning.points;

import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyObject;

/**
 * Based on JythonObjectFactory
 * See: http://www.jython.org/jythonbook/en/1.0/JythonAndJavaIntegration.html#more-efficient-version-of-loosely-coupled-object-factory
 */
public class ScanPointGeneratorFactory {

	/**
	 * Call to load Jython asynchronously to avoid the
	 * long wait time that happens when points are first generated.
	 *
	 * Call this method to load jython in a daemon thread such that
	 * when it is first used, for instance in the UI, it will execute
	 * fast because the interpreter has classloaded.
	 */
	public static void init() {

		final Thread background = new Thread() {
			@Override
			public void run() {
				// Loading one causes Jython to class load.
				ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();
			}
		};
		background.setDaemon(true);
		background.setName("Jython loader thread");    // Always name threads.
		background.setPriority(Thread.MIN_PRIORITY+2); // Background but some urgency more than least
		background.start();
	}


	// This class compiles Jython objects and maps them to an IPointGenerator so they can be
	// used easily in Java. More specifically, it creates the Jython ScanPointGenerator interface
	// classes found in the scripts folder of this package (org.eclipse.scanning.points)

	// These are the constructors for each Jython SPG interface. To add a new one just replace,
	// for example, "JArrayGenerator" with your new class and give the constructor a new name
	// like "<YourClass>Factory"
    public static JythonObjectFactory<PPointGenerator> JOneAxisLineGeneratorFactory() {
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JLineGenerator1D");
    }

	public static JythonObjectFactory<PPointGenerator> JTwoAxisLineGeneratorFactory() {
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JLineGenerator2D");
    }

    public static JythonObjectFactory<PPointGenerator> JOneAxisArrayGeneratorFactory() {
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JArrayGenerator");
    }

	public static JythonObjectFactory<PPointGenerator> JStaticGeneratorFactory() {
		return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JStaticPointGenerator");
	}

	public static JythonObjectFactory<PPointGenerator> JTwoAxisSpiralGeneratorFactory() {
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JSpiralGenerator");
    }

    public static JythonObjectFactory<PPointGenerator> JTwoAxisLissajousGeneratorFactory() {
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JLissajousGenerator");
    }

    public static JythonObjectFactory<PPointGenerator> JConcatGeneratorFactory(){
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JConcatGenerator");
    }

    public static JythonObjectFactory<PPointGenerator> JZipGeneratorFactory(){
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JZipGenerator");
    }

    public static JythonObjectFactory<PPointGenerator> JCompoundGeneratorFactory() {
        return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JCompoundGenerator");
    }

    public static JythonObjectFactory<PPointGenerator> JPyDictionaryGeneratorFactory() {
    	return new JythonObjectFactory<>(PPointGenerator.class, "jython_spg_interface", "JPyDictionaryGenerator");
    }

    public static JythonObjectFactory<PyObject> JRandomOffsetMutatorFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JRandomOffsetMutator");
    }

    public static JythonObjectFactory<PyObject> JCircularROIFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JCircularROI");
    }

    public static JythonObjectFactory<PyObject> JEllipticalROIFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JEllipticalROI");
    }

    public static JythonObjectFactory<PyObject> JPointROIFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JPointROI");
    }

    public static JythonObjectFactory<PyObject> JPolygonalROIFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JPolygonalROI");
    }

    public static JythonObjectFactory<PyObject> JRectangularROIFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JRectangularROI");
    }

    public static JythonObjectFactory<PyObject> JSectorROIFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JSectorROI");
    }

    public static JythonObjectFactory<PyObject> JExcluderFactory() {
        return new JythonObjectFactory<>(PyObject.class, "jython_spg_interface", "JExcluder");
    }

}