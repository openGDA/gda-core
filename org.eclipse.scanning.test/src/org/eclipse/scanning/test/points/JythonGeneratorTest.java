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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.JythonGeneratorModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the Jython point iterator by loading its scan points and
 *
 * @author Matthew Gerring
 *
 */
public class JythonGeneratorTest {

	private static final String VALUE = "value";

	private IPointGeneratorService service;

	@Before
	public void before() {
		service = new PointGeneratorService();
	}

	@Test(expected=GeneratorException.class)
	public void emptyModel() throws Exception {

        JythonGeneratorModel model = new JythonGeneratorModel();
        service.createGenerator(model);
	}

	@Test(expected=GeneratorException.class)
	public void modulelessModel() throws Exception {

        JythonGeneratorModel model = new JythonGeneratorModel();
        model.setPath("src/org/eclipse/scanning/test/points");
        service.createGenerator(model);
	}

	@Test(expected=GeneratorException.class)
	public void classlessModel() throws Exception {

        JythonGeneratorModel model = new JythonGeneratorModel();
        model.setPath("src/org/eclipse/scanning/test/points");
        model.setModuleName("JythonGeneratorTest");
        service.createGenerator(model);
	}

	@Test(expected=GeneratorException.class)
	public void badModule() throws Exception {

        JythonGeneratorModel model = new JythonGeneratorModel();
        model.setPath("src/org/eclipse/scanning/test/points");
        model.setModuleName("fred");
        model.setClassName("FixedValueWrapper");
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        gen.size();
	}

	@Test(expected=GeneratorException.class)
	public void badClass() throws Exception {

        JythonGeneratorModel model = new JythonGeneratorModel();
        model.setPath("src/org/eclipse/scanning/test/points");
        model.setModuleName("JythonGeneratorTest");
        model.setClassName("fred");
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        gen.size();
	}

	@Test(expected=GeneratorException.class)
	public void exceptionInGenerator() throws Exception {

        JythonGeneratorModel model = new JythonGeneratorModel();
        model.setPath("src/org/eclipse/scanning/test/points");
        model.setModuleName("JythonGeneratorTest");
        model.setClassName("ExceptionGenerator");
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        gen.size();
	}

	@Test(expected=GeneratorException.class)
	public void missingMandatoryField() throws Exception {

        JythonGeneratorModel model = createFixedValueModel("x", 10, Math.PI);
        model.setPath("src/org/eclipse/scanning/test/points");
        model.setModuleName("JythonGeneratorTest");
        model.setClassName("FixedValueWrapper");
        // Validates and creates succesfully
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        assertEquals(Math.PI, gen.getFirstPoint().getValue("x"), 0.000001);
        Map<String, Object> correctlyFormedArgs = model.getJythonArguments();
        // No longer correctly formed
        correctlyFormedArgs.remove("size");
        model.setJythonArguments(correctlyFormedArgs);
        service.createGenerator(model);
	}

	@Test
	public void testSize() throws Exception {

        JythonGeneratorModel model = createFixedValueModel("x", 10, Math.PI);
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        assertNotNull(gen);
        assertEquals(10, gen.size());
	}

	@Test
	public void testValue1() throws Exception {

        JythonGeneratorModel model = createFixedValueModel("p", 3, Math.PI);
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        List<IPosition> points = gen.createPoints();
        assertEquals(3, points.size());
        assertEquals(Math.PI, points.get(1).getValue("p"), 0.000001);

	}

	@Test
	public void testValue2() throws Exception {

        JythonGeneratorModel model = createMultipliedValueModel("m", 5, 10);
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        List<IPosition> points = gen.createPoints();
        assertEquals(5, points.size());
        assertEquals(0, points.get(0).getValue("m"), 0.000001);
        assertEquals(10, points.get(1).getValue("m"), 0.000001);
        assertEquals(20, points.get(2).getValue("m"), 0.000001);
        assertEquals(40, points.get(4).getValue("m"), 0.000001);

	}

	@Test
	public void mapPositionValue() throws Exception {
		List<String> names = Arrays.asList("x0", "x1", "x2");
        JythonGeneratorModel model = createMapPositionModel(names, 5, 10);
        // Testing validity
        model.setContinuous(true);
        model.setAlternating(true);
        IPointGenerator<JythonGeneratorModel> gen = service.createGenerator(model);
        List<IPosition> points = gen.createPoints();
        assertEquals(5, points.size());
        assertEquals(0, points.get(0).getValue("x0"), 0.000001);
        assertEquals(0, points.get(0).getValue("x1"), 0.000001);
        assertEquals(0, points.get(0).getValue("x2"), 0.000001);

        assertEquals(40, points.get(4).getValue("x0"), 0.000001);
        assertEquals(40, points.get(4).getValue("x1"), 0.000001);
        assertEquals(40, points.get(4).getValue("x2"), 0.000001);

        assertEquals(null, points.get(4).get("x5")); // No such scannable created

	}


	private JythonGeneratorModel createFixedValueModel(String scannableName, int size, double value) {
		JythonGeneratorModel model = new JythonGeneratorModel();
        model.setModuleName("JythonGeneratorTest");
        model.setClassName("FixedValueWrapper");
        model.setPath("src/org/eclipse/scanning/test/points");
        model.setName(scannableName);
        model.setSize(size);
        model.addJythonArgument(VALUE, value);
        return model;
	}

	private JythonGeneratorModel createMultipliedValueModel(String scannableName, int size, double value) {
		JythonGeneratorModel model = new JythonGeneratorModel();
        model.setModuleName("JythonGeneratorTest");
        model.setClassName("MultipliedValueWrapper");
        model.setPath("src/org/eclipse/scanning/test/points");
        model.setName(scannableName);
        model.setSize(size);
        model.addJythonArgument(VALUE, value);
        return model;
	}

	private JythonGeneratorModel createMapPositionModel(List<String> scannableNames, int size, double value) {
		JythonGeneratorModel model = new JythonGeneratorModel();
		model.setModuleName("JythonGeneratorTest");
		model.setClassName("MultipliedValueWrapper");
		model.setPath("src/org/eclipse/scanning/test/points");
		model.addJythonArgument("axes", scannableNames);
        model.setSize(size);
        model.addJythonArgument(VALUE, value);
        return model;
	}

}
