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
package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.eclipse.scanning.points.CompoundGenerator;
import org.eclipse.scanning.points.PPointGenerator;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.junit.jupiter.api.Test;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;

public class ScanPointGeneratorFactoryTest {

    @Test
    public void testJLineGeneratorFactory1D() {
        JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

        PPointGenerator gen = lineGeneratorFactory.createObject("x", "mm", 1.0, 5.0, 5);
		Iterator<IPosition> iterator = gen.iterator();

        List<Object> expectedPoints = new ArrayList<Object>();
	    expectedPoints.add(new Scalar<>("x", 0, 1.0));
	    expectedPoints.add(new Scalar<>("x", 1, 2.0));
	    expectedPoints.add(new Scalar<>("x", 2, 3.0));
	    expectedPoints.add(new Scalar<>("x", 3, 4.0));
	    expectedPoints.add(new Scalar<>("x", 4, 5.0));

	    final int expectedSize = expectedPoints.size();
        assertEquals(expectedSize, gen.getSize());
        assertEquals(1, gen.getRank());
        assertArrayEquals(new int[] { expectedSize }, gen.getShape());

	    int index = 0;
        while (iterator.hasNext()){

            Object point = iterator.next();
            assertEquals(expectedPoints.get(index), point);

		index++;
        }
        assertEquals(expectedSize, index);
    }

    @Test
    public void testJLineGeneratorFactory2D() {
        JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLineGeneratorFactory();

        PyList names = new PyList(Arrays.asList(new String[] {"X", "Y"}));
        PyList units = new PyList(Arrays.asList(new String[] {"mm", "mm"}));
        double[] start = {1.0, 2.0};
        double[] stop = {5.0, 10.0};

        PPointGenerator gen = lineGeneratorFactory.createObject(names, units, start, stop, 5);
		Iterator<IPosition> iterator = gen.iterator();

        List<Object> expectedPoints = new ArrayList<Object>();
	    expectedPoints.add(new Point("X", 0, 1.0, "Y", 0, 2.0, 0, false));
	    expectedPoints.add(new Point("X", 1, 2.0, "Y", 1, 4.0, 1, false));
	    expectedPoints.add(new Point("X", 2, 3.0, "Y", 2, 6.0, 2, false));
	    expectedPoints.add(new Point("X", 3, 4.0, "Y", 3, 8.0, 3, false));
	    expectedPoints.add(new Point("X", 4, 5.0, "Y", 4, 10.0, 4, false));

	    final int expectedSize = expectedPoints.size();
        assertEquals(expectedSize, gen.getSize());
        assertEquals(1, gen.getRank());
        assertArrayEquals(new int[] { expectedSize }, gen.getShape());

	    int index = 0;
        while (iterator.hasNext()){  // Just get first few points
            Object point = iterator.next();
            assertEquals(expectedPoints.get(index), point);
		index++;
        }
        assertEquals(expectedSize, index);
    }

    @Test
    public void testJArrayGeneratorFactory() {
        JythonObjectFactory<PPointGenerator> arrayGeneratorFactory = ScanPointGeneratorFactory.JOneAxisArrayGeneratorFactory();

        double[] arrayPoints = new double[] {1.0, 2.0, 3.0, 4.0, 5.0};

        PPointGenerator gen = arrayGeneratorFactory.createObject("x", "mm", arrayPoints);
		Iterator<IPosition> iterator = gen.iterator();

        List<Object> expectedPoints = new ArrayList<Object>();
	    expectedPoints.add(new Scalar<>("x", 0, 1.0));
	    expectedPoints.add(new Scalar<>("x", 1, 2.0));
	    expectedPoints.add(new Scalar<>("x", 2, 3.0));
	    expectedPoints.add(new Scalar<>("x", 3, 4.0));
	    expectedPoints.add(new Scalar<>("x", 4, 5.0));

	    final int expectedSize = expectedPoints.size();
        assertEquals(expectedSize, gen.getSize());
        assertEquals(1, gen.getRank());
        assertArrayEquals(new int[] { expectedSize }, gen.getShape());

	    int index = 0;
        while (iterator.hasNext()){
            Object point = iterator.next();
            assertEquals(expectedPoints.get(index), point);
		index++;
        }
        assertEquals(expectedSize, index);
    }

    @Test
    public void testJSpiralGeneratorFactory() {
        JythonObjectFactory<PPointGenerator> spiralGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisSpiralGeneratorFactory();

        PyList names = new PyList(Arrays.asList(new String[] {"X", "Y"}));
        PyList centre = new PyList(Arrays.asList(new Double[] {0.0, 0.0}));
        double radius = 1.5;
        double scale = 1.0;
        boolean alternate = false;

        PPointGenerator gen = spiralGeneratorFactory.createObject(names, "mm", centre, radius, scale, alternate);
		Iterator<IPosition> iterator = gen.iterator();

        List<Object> expectedPoints = new ArrayList<Object>();
	    expectedPoints.add(new Point("X", 0, 0.23663214944574582, "Y", 0, -0.3211855677650875, 0, false));
	    expectedPoints.add(new Point("X", 1, -0.6440318266552169, "Y", 1, -0.25037538922751695, 1, false));
	    expectedPoints.add(new Point("X", 2, -0.5596688286164636, "Y", 2, 0.6946549630820702, 2, false));
	    expectedPoints.add(new Point("X", 3, 0.36066957248394327, "Y", 3, 0.9919687803189761, 3, false));
	    expectedPoints.add(new Point("X", 4, 1.130650533568409, "Y", 4, 0.3924587351155914, 4, false));
	    expectedPoints.add(new Point("X", 5, 1.18586065489788, "Y", 5, -0.5868891557832875, 5, false));
	    expectedPoints.add(new Point("X", 6, 0.5428735608675326, "Y", 6, -1.332029488076613, 6, false));
	    expectedPoints.add(new Point("X", 7, -0.43197069612785155, "Y", 7, -1.4834842311481606, 7, false));

	    final int expectedSize = 8;
        assertEquals(expectedSize, gen.getSize());
        assertEquals(1, gen.getRank());
        assertArrayEquals(new int[] { expectedSize }, gen.getShape());

	    int index = 0;
	    while (iterator.hasNext()) {  // Just get first few points
            Object point = iterator.next();
            assertEquals(expectedPoints.get(index), point);
		index++;
        }
        assertEquals(expectedSize, index);
    }

    @Test
    public void testJLissajousGeneratorFactory() {
        JythonObjectFactory<PPointGenerator> lissajousGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLissajousGeneratorFactory();

        PyDictionary box = new PyDictionary();
        box.put("width", 1.5);
        box.put("height", 1.5);
        box.put("centre", new double[] {0.0, 0.0});

        PyList names = new PyList(Arrays.asList(new String[] {"X", "Y"}));
        int numLobes = 2;
        int numPoints = 500;

        PPointGenerator gen = lissajousGeneratorFactory.createObject(names, "mm", box, numLobes, numPoints);
		Iterator<IPosition> iterator = gen.iterator();

        List<Object> expectedPoints = new ArrayList<Object>();
	    expectedPoints.add(new Point("X", 0, 0.0, "Y", 0, 0.0, 0, false));
	    expectedPoints.add(new Point("X", 1, 0.01884757158250311, "Y", 1, 0.028267637002450906, 1, false));
	    expectedPoints.add(new Point("X", 2, 0.03768323863482717, "Y", 2, 0.05649510414594954, 2, false));
	    expectedPoints.add(new Point("X", 3, 0.05649510414594954, "Y", 3, 0.08464228865511125, 3, false));
	    expectedPoints.add(new Point("X", 4, 0.07527128613841116, "Y", 4, 0.1126691918405678, 4, false));
	    expectedPoints.add(new Point("X", 5, 0.0939999251732282, "Y", 5, 0.14053598593929345, 5, false));

        assertEquals(numPoints, gen.getSize());
        assertEquals(1, gen.getRank());
        assertArrayEquals(new int[] { numPoints }, gen.getShape());

	    int index = 0;
        while (iterator.hasNext() && index < 6){  // Just test first few points
            Object point = iterator.next();
            assertEquals(expectedPoints.get(index), point);
		index++;
        }
    }

    @Test
    public void testJCompoundGeneratorFactoryWithRaster() {

		JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		PPointGenerator outerLine = lineGeneratorFactory.createObject("y", "mm", 0.0, 5.0, 2);

		PPointGenerator innerLine = lineGeneratorFactory.createObject("x", "mm", 1.0, 3.0, 3, true);

		JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();

		PPointGenerator[] generators = {outerLine, innerLine};
        PyObject[] excluders = CompoundGenerator.getExcluders(Arrays.asList(new ScanRegion(new RectangularROI(0,0,5,5,0), Arrays.asList("x", "y"))));
        PyObject[] mutators = {};

        PPointGenerator gen = compoundGeneratorFactory.createObject(generators, excluders, mutators);
		Iterator<IPosition> iterator = gen.iterator();

        List<Object> expectedPoints = new ArrayList<Object>();
	    expectedPoints.add(new Point(0, 1.0, 0, 0.0, 0));
	    expectedPoints.add(new Point(1, 2.0, 0, 0.0, 1));
	    expectedPoints.add(new Point(2, 3.0, 0, 0.0, 2));
	    expectedPoints.add(new Point(2, 3.0, 1, 5.0, 3));
	    expectedPoints.add(new Point(1, 2.0, 1, 5.0, 4));
	    expectedPoints.add(new Point(0, 1.0, 1, 5.0, 5));

	    final int expectedSize = expectedPoints.size();
        assertEquals(expectedSize, gen.getSize());
        assertEquals(2, gen.getRank());
        assertArrayEquals(new int[] { 2, 3 }, gen.getShape());

	    int index = 0;
        while (iterator.hasNext()){
            Object point = iterator.next();
            assertEquals(expectedPoints.get(index), point);
		index++;
        }
        assertEquals(6, index);
    }

    @Test
    public void testJCompoundGeneratorFactoryWithMutatedRaster() {
        JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

        PPointGenerator line1 = lineGeneratorFactory.createObject("y", "mm", 2.0, 10.0, 5);

        PPointGenerator line2 = lineGeneratorFactory.createObject("x", "mm", 1.0, 5.0, 5);

        JythonObjectFactory<?> randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();

        int seed = 10;

        PyList axes = new PyList(Arrays.asList(new String[] {"y", "x"}));

        PyDictionary maxOffset = new PyDictionary();
        maxOffset.put("x", 0.5);
        maxOffset.put("y", 0.5);

		PyObject randomOffset = (PyObject) randomOffsetMutatorFactory.createObject(seed, axes, maxOffset);

        JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();

        PPointGenerator[] generators = {line1, line2};
        PyObject[] excluders = CompoundGenerator.getExcluders(Arrays.asList(
			new ScanRegion(new RectangularROI(0,0,5,10,0), Arrays.asList("x", "y"))));
        PyObject[] mutators = {randomOffset};


        PPointGenerator gen = compoundGeneratorFactory.createObject(generators, excluders, mutators);
		Iterator<IPosition> iterator = gen.iterator();

		List<IPosition> vanillaPoints = new ArrayList<IPosition>(10);
		vanillaPoints.add(new Point("x", 0, 1.0, "y", 0, 2.0));
		vanillaPoints.add(new Point("x", 1, 2.0, "y", 0, 2.0));
		vanillaPoints.add(new Point("x", 2, 3.0, "y", 0, 2.0));
		vanillaPoints.add(new Point("x", 3, 4.0, "y", 0, 2.0));
		vanillaPoints.add(new Point("x", 4, 5.0, "y", 0, 2.0));
		vanillaPoints.add(new Point("x", 0, 1.0, "y", 1, 4.0));
		vanillaPoints.add(new Point("x", 1, 2.0, "y", 1, 4.0));
		vanillaPoints.add(new Point("x", 2, 3.0, "y", 1, 4.0));
		vanillaPoints.add(new Point("x", 3, 4.0, "y", 1, 4.0));
		vanillaPoints.add(new Point("x", 4, 5.0, "y", 1, 4.0));

	    final int[] expectedShape = new int[] { 5, 5 };
	    final int expectedSize = expectedShape[0] * expectedShape[1];
        assertEquals(expectedSize, gen.getSize());
        assertEquals(2, gen.getRank());
        assertArrayEquals(expectedShape, gen.getShape());

		Iterator<IPosition> itV = vanillaPoints.iterator();
		while (iterator.hasNext() && itV.hasNext()) {
			IPosition point = iterator.next();
			IPosition vp = itV.next();
			assertEquals(vp.getIndices(), point.getIndices());
			assertEquals(vp.getNames(), point.getNames());
			for (String axis : vp.getNames()) {
				assertEquals(vp.getValue(axis), point.getValue(axis), (double) maxOffset.get(axis));
			}
		}
		assertFalse(itV.hasNext());
	}
}
