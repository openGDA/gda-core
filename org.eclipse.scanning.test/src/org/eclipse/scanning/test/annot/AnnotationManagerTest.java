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
package org.eclipse.scanning.test.annot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.LevelComparator;
import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * Test
 * 1. Basic counts
 * 2. Inheritance
 * 3. Injected arguments, including services, ScanInformation and IPosition instances.
 * 4. Large call size performance per call cycle.
 * 5. Calling order test (deviceA before deviceB etc.)
 *
 * @author Matthew Gerring
 *
 */
public class AnnotationManagerTest {

	private static IPointGeneratorService pservice;

	@BeforeAll
	public static void createGeneratorService() {
		pservice = new PointGeneratorService();
	}

	private AnnotationManager annotationManager;

	// Test devices
	private SimpleDevice simpleDdevice;
	private CountingDevice countingDevice;
	private ExtendedCountingDevice extCountingDevice;
	private InjectionDevice injectionDevice;
	private InvalidInjectionDevice invalidInjectionDevice;

	@BeforeEach
	public void before() throws Exception {
		final Map<Class<?>, Object> testServices = new HashMap<>();
		testServices.put(IPointGeneratorService.class,  new PointGeneratorService());
		testServices.put(IScannableDeviceService.class, new MockScannableConnector(null));
		testServices.put(IRunnableDeviceService.class,  new RunnableDeviceServiceImpl((IScannableDeviceService)testServices.get(IScannableDeviceService.class)));
		annotationManager = new AnnotationManager(null, testServices);

		simpleDdevice   = new SimpleDevice();
		countingDevice   = new CountingDevice();
		extCountingDevice   = new ExtendedCountingDevice();
		injectionDevice   = new InjectionDevice();
		invalidInjectionDevice = new InvalidInjectionDevice();
		annotationManager.addDevices(simpleDdevice, countingDevice, extCountingDevice, injectionDevice, invalidInjectionDevice);
	}

	@AfterEach
	public void after() {
		annotationManager.dispose();
	}

	@Test
	public void countSimple() throws Exception {
		annotationManager.invoke(ScanStart.class);
		annotationManager.invoke(ScanStart.class);
		annotationManager.invoke(ScanStart.class);
		annotationManager.invoke(ScanStart.class);
		annotationManager.invoke(ScanStart.class);
		assertEquals(simpleDdevice.getCount(), 5);
	}

	@Test(expected=ScanningException.class)
	public void countConfigureNoScanInfo() throws Exception {
		annotationManager.invoke(PreConfigure.class);
		assertEquals(1, countingDevice.getCount("configure"));
	}

	@Test
	public void countConfigure() throws Exception {

		ScanInformation info = mock(ScanInformation.class);
		try {
			annotationManager.addContext(info);
			annotationManager.invoke(PreConfigure.class);
			assertEquals(1, countingDevice.getCount("configure"));
		} finally {
			annotationManager.removeContext(info);
		}
	}


	@Test
	public void countInherited() throws Exception {

		annotationManager.invoke(ScanStart.class);
		for (int i = 0; i < 5; i++) cycle(i);
		annotationManager.invoke(ScanEnd.class);

		assertEquals(1, countingDevice.getCount("prepareVoltages"));
		assertEquals(1, countingDevice.getCount("dispose"));
		assertEquals(5, extCountingDevice.getCount("prepare"));  // Points done.

		assertEquals(1, extCountingDevice.getCount("prepareVoltages"));
		assertEquals(1, extCountingDevice.getCount("moveToNonObstructingLocation"));
		assertEquals(5, extCountingDevice.getCount("prepare"));  // Points done.
		assertEquals(5, extCountingDevice.getCount("checkNextMoveLegal"));  // Points done.
		assertEquals(5, extCountingDevice.getCount("notifyPosition"));
		assertEquals(1, extCountingDevice.getCount("dispose")); //
	}

	private void cycle(int i) throws Exception {
		annotationManager.invoke(LevelStart.class);
		annotationManager.invoke(PointStart.class, new Point(i, i*10, i, i*20));
		annotationManager.invoke(PointEnd.class, new Point(i, i*10, i, i*20));
		annotationManager.invoke(LevelEnd.class);
	}

	@Test
	public void simpleInject() throws Exception {
		annotationManager.invoke(PointStart.class, new Point(0, 10, 0, 20));
		assertEquals(1, extCountingDevice.getPositions().size());

		IPosition firstPoint = extCountingDevice.getPositions().get(0);
		if (firstPoint==null) throw new Exception("The manager failed to inject a position!");
		assertTrue(firstPoint.equals(new Point(0, 10, 0, 20)));
	}

	@Test
	public void scanPointGeneratorInject() throws Exception {
		ScanInformation info = mock(ScanInformation.class);
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel();
		model.setBoundingBox(new BoundingBox(0,0,1,1));
		IPointGenerator<TwoAxisGridPointsModel> gen = pservice.createGenerator(model);
		annotationManager.invoke(PreConfigure.class, gen, info);
		assertEquals(gen, injectionDevice.getPointGenerator());
	}

	@Test
	public void scanInfoInject() throws Exception {
		ScanInformation info = mock(ScanInformation.class);

		annotationManager.invoke(ScanStart.class);
		assertEquals(null, extCountingDevice.getScanInformation());

		annotationManager.addContext(info);
		annotationManager.invoke(ScanStart.class);
		assertTrue(extCountingDevice.getScanInformation()!=null);
	}

	@Test
	public void somePointsInject() throws Exception {
		annotationManager.invoke(ScanStart.class);
		for (int i = 0; i < 5; i++) cycle(i);

		assertEquals(5, extCountingDevice.getPositions().size());
		for (IPosition p : extCountingDevice.getPositions()) {
			if (p==null) throw new Exception("The manager failed to inject a position!");
		}

		assertEquals(1, extCountingDevice.getServices().size());
		for (IRunnableDeviceService p : extCountingDevice.getServices()) {
			if (p==null) throw new Exception("The manager failed to inject a IRunnableDeviceService!");
		}

		annotationManager.invoke(ScanEnd.class);
		assertEquals(0, extCountingDevice.getPositions().size());
	}

	@Test
	public void complexInject() throws Exception {
		annotationManager.invoke(PointStart.class, new Point(0, 10, 0, 20));
		checkCalls(1, injectionDevice, "method1");
		checkCalls(1, injectionDevice, "method2");
		checkCalls(1, injectionDevice, "method3");
		checkCalls(1, injectionDevice, "method4");
		checkCalls(1, injectionDevice, "method5");
		checkCalls(1, injectionDevice, "method6");

		annotationManager.invoke(ScanEnd.class);
		checkCalls(0, injectionDevice, "method1");
	}

	@Test
	public void complexMultipleInjects() throws Exception {
		annotationManager.invoke(ScanStart.class);
		for (int i = 0; i < 5; i++) cycle(i);

		checkCalls(5, injectionDevice, "method1");
		checkCalls(5, injectionDevice, "method2");
		checkCalls(5, injectionDevice, "method3");
		checkCalls(5, injectionDevice, "method4");
		checkCalls(5, injectionDevice, "method5");
		checkCalls(5, injectionDevice, "method6");

		annotationManager.invoke(ScanEnd.class);
		checkCalls(0, injectionDevice, "method1");
	}

	@Test(expected=Exception.class)
	public void checkNoDevicesError() throws Exception {
		AnnotationManager m = new AnnotationManager();
		m.addDevices();
	}

	@Test(expected=IllegalArgumentException.class)
	public void checkRepeatedTypes() throws Exception {
		try {
			AnnotationManager m = new AnnotationManager();
	        m.addDevices(new RepeatedTypeDevice());
		} catch(Exception ne) {
			System.out.println(ne.getMessage());
			throw ne;
		}
	}

	private void checkCalls(int size, InjectionDevice device, String methodName) {
		if (size < 1) {
			assertTrue(device.getArguments(methodName)==null);
			return;
		}
		assertEquals(size, device.getArguments(methodName).size());

		Class<?>[] methodClasses = getFirstMethodArgs(device, methodName);
		for (Object[] oa : device.getArguments(methodName)) {
			assertTrue(oa.length>0);
			for (int i = 0; i < oa.length; i++) {
				assertTrue(oa[i]!=null);
				assertTrue(getClasses(oa[i]).contains(methodClasses[i]));
			}
		}
	}

	@Test
	public void invalidComplexInject() throws Exception {

		annotationManager.invoke(PointStart.class, new Point(0, 10, 0, 20));

		checkCalls(1, invalidInjectionDevice, "validMethod1");
		checkCalls(1, invalidInjectionDevice, "validMethod2");

		List<Object[]> oa = invalidInjectionDevice.getArguments("invalidMethod1");
		assertTrue(oa.get(0)[0]==null); // Couldn't find that

		oa = invalidInjectionDevice.getArguments("invalidMethod2");
		assertTrue(oa.get(0)[0]==null); // Couldn't find that
		assertTrue(oa.get(0)[1]==null); // Couldn't find that
		assertTrue(oa.get(0)[2]==null); // Couldn't find that

		oa = invalidInjectionDevice.getArguments("invalidMethod3");
		assertTrue(oa.get(0)[0]!=null);
		assertTrue(oa.get(0)[1]==null); // Couldn't find that

		oa = invalidInjectionDevice.getArguments("invalidMethod4");
		assertTrue(oa.get(0)[0]!=null);
		assertTrue(oa.get(0)[1]!=null);
		assertTrue(oa.get(0)[2]==null); // Couldn't find that

		oa = invalidInjectionDevice.getArguments("invalidMethod5");
		assertTrue(oa.get(0)[0]!=null);
		assertTrue(oa.get(0)[1]==null); // Couldn't find that
		assertTrue(oa.get(0)[2]!=null);

		oa = invalidInjectionDevice.getArguments("invalidMethod6");
		assertTrue(oa.get(0)[0]!=null);
		assertTrue(oa.get(0)[1]!=null);
		assertTrue(oa.get(0)[2]!=null);
		assertTrue(oa.get(0)[3]==null); // Couldn't find that
		assertTrue(oa.get(0)[4]==null); // Couldn't find that
		assertTrue(oa.get(0)[5]==null); // Couldn't find that

		annotationManager.invoke(ScanEnd.class);
		checkCalls(0, invalidInjectionDevice, "validMethod1");
	}

	@Test
	public void checkSimpleOrder() throws Exception {
		AnnotationManager m = new AnnotationManager();

		List<OrderedDevice> devices = new ArrayList<>();
		for (int i = 0; i < 100; i++) devices.add(new OrderedDevice("device"+i));

		m.addDevices(devices);
		m.invoke(PointStart.class, new Point(0, 10, 0, 20));

		final List<String> orderedNames = devices.stream().map(x -> x.getName()).collect(Collectors.toList());
		final List<String> names = OrderedDevice.getCalledNames();

		assertTrue(orderedNames.equals(names));

		m.invoke(ScanEnd.class);
	}

	@Test
	public void checkOrderByLevel() throws Exception {

		AnnotationManager m = new AnnotationManager();

		// We add them not by level
		List<OrderedDevice> devices = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			OrderedDevice d = new OrderedDevice("device"+i);
			d.setLevel(i%10); // Devices in different level order...
			devices.add(d);
		}

		m.addDevices(devices);

		// We invoke them
		m.invoke(PointStart.class, new Point(0, 10, 0, 20));

		// We sort them by level
		Collections.sort(devices, new LevelComparator());
		final List<String> orderedNames = devices.stream().map(x -> x.getName()).collect(Collectors.toList());
		final List<String> names = OrderedDevice.getCalledNames();

		// The called names should have been sorted by level in the first place.
		assertTrue(orderedNames.equals(names));

		m.invoke(ScanEnd.class);
	}

	@Test
	public void checkOrderByCallThenByLevel() throws Exception {
		AnnotationManager m = new AnnotationManager();

		// We add them not by level
		List<OrderedDevice> fds = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			OrderedDevice d = new OrderedDevice("fd"+i);
			d.setLevel(i%2); // Devices in different level order...
			fds.add(d);
		}
		m.addDevices(fds);

		List<OrderedDevice> sds = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			OrderedDevice d = new OrderedDevice("sd"+i);
			d.setLevel(i%2); // Devices in different level order...
			sds.add(d);
		}
		m.addDevices(sds);

		// We invoke them
		m.invoke(PointStart.class, new Point(0, 10, 0, 20));

		final List<String> orderedNames = new ArrayList<String>(20);
		Collections.sort(fds, new LevelComparator<OrderedDevice>());
		orderedNames.addAll(fds.stream().map(x -> x.getName()).collect(Collectors.toList()));
		Collections.sort(sds, new LevelComparator<OrderedDevice>());
		orderedNames.addAll(sds.stream().map(x -> x.getName()).collect(Collectors.toList()));

		final List<String> names = OrderedDevice.getCalledNames();
		assertTrue(orderedNames.equals(names));

		m.invoke(ScanEnd.class);
	}

	@Test
	public void checkPerformancePerCycle() throws Exception {
		final int size = 1000;

		AnnotationManager m = new AnnotationManager();

		// We add them not by level
		for (int i = 0; i < size; i++) {
			ExtendedCountingDevice d = new ExtendedCountingDevice();
			d.setLevel(i%2); // Devices in different level order...
			m.addDevices(d);
		}

		long start = System.currentTimeMillis();
		annotationManager.invoke(ScanStart.class);
		for (int i = 0; i < size; i++) cycle(i);
		annotationManager.invoke(ScanEnd.class);
		long end = System.currentTimeMillis();

		long time = (end-start)/size;
		System.out.println("Each cycle took "+time+"ms. We ran '"+size+"' devices with '"+size+"' cycles.");
		assertTrue(time<10); // These cycles must be fast
	}

	private Class<?>[] getFirstMethodArgs(InjectionDevice device, String methodName) {
		for (Method method : device.getClass().getMethods()) {
			if (method.getName().equals(methodName)) return method.getParameterTypes();
		}
		return null;
	}

	private Collection<Class<?>> getClasses(Object object) {
		final Class<?> clazz = object.getClass();

		final Collection<Class<?>> classes = new HashSet<>();
		classes.add(clazz);
		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> class1 : interfaces)  classes.add(class1);

		// TODO Currently only support one level deep
		classes.add(clazz.getSuperclass());
		interfaces = clazz.getSuperclass().getInterfaces();
		for (Class<?> class1 : interfaces)  classes.add(class1);

		return classes;
	}

}
