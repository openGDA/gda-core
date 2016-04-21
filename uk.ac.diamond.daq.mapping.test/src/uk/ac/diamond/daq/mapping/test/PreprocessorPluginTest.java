/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import uk.ac.diamond.daq.mapping.impl.MandelbrotPreprocessor;
import uk.ac.diamond.daq.mapping.impl.MappingStagePreprocessor;
import uk.ac.gda.core.GDACoreActivator;

public class PreprocessorPluginTest {

	private static final String CONFIGURED_X_MOTOR_NAME = "x_motor";
	private static final String CONFIGURED_Y_MOTOR_NAME = "y_motor";

	private BundleContext bundleContext;
	private ServiceRegistration<IPreprocessor> mandelbrotRegistration;
	private ServiceRegistration<IPreprocessor> mappingRegistration;

	private ScanServlet scanServlet;

	@Before
	public void setUp() throws Exception {
		bundleContext = GDACoreActivator.getBundleContext();

		// Set up the preprocessors. Normally this would be done by the Spring config.
		MandelbrotPreprocessor mandelbrotPreprocessor = new MandelbrotPreprocessor();
		mandelbrotPreprocessor.setRealAxisName(CONFIGURED_X_MOTOR_NAME);
		mandelbrotPreprocessor.setImaginaryAxisName(CONFIGURED_Y_MOTOR_NAME);
		mandelbrotRegistration = bundleContext.registerService(IPreprocessor.class, mandelbrotPreprocessor, null);

		MappingStagePreprocessor mappingStagePreprocessor = new MappingStagePreprocessor();
		mappingStagePreprocessor.setActiveFastScanAxis(CONFIGURED_X_MOTOR_NAME);
		mappingStagePreprocessor.setActiveSlowScanAxis(CONFIGURED_Y_MOTOR_NAME);
		mappingRegistration = bundleContext.registerService(IPreprocessor.class, mappingStagePreprocessor, null);

		scanServlet = new ScanServlet();
	}

	@After
	public void tearDown() throws Exception {
		scanServlet = null;
		mandelbrotRegistration.unregister();
		mappingRegistration.unregister();

		// Ensure all the preprocessors have been removed
		assertNull(bundleContext.getServiceReference(IPreprocessor.class));
		bundleContext = null;
	}

	@Test
	public void axisNamesShouldBeReplaced() throws Exception {
		ScanRequest<?> scanRequest = new ScanRequest<>();
		MandelbrotModel mandelbrotModel = new MandelbrotModel();
		mandelbrotModel.setRealAxisName("stage_x");
		mandelbrotModel.setImaginaryAxisName("stage_y");
		scanRequest.putDetector("mandelbrot_detector", mandelbrotModel);

		GridModel gridModel = new GridModel();
		gridModel.setFastAxisName("stage_x");
		gridModel.setSlowAxisName("stage_y");
		scanRequest.setModels(gridModel);

		ScanBean scanBean = new ScanBean();
		scanBean.setName("PreprocessorPluginTest scan");
		scanBean.setScanRequest(scanRequest);

		// Ensure the names are different to begin with
		assertNotEquals(CONFIGURED_X_MOTOR_NAME, gridModel.getFastAxisName());
		assertNotEquals(CONFIGURED_Y_MOTOR_NAME, gridModel.getSlowAxisName());
		assertNotEquals(CONFIGURED_X_MOTOR_NAME, mandelbrotModel.getRealAxisName());
		assertNotEquals(CONFIGURED_Y_MOTOR_NAME, mandelbrotModel.getImaginaryAxisName());

		// This next call is potentially very fragile. At the moment, it seems we can get away with calling
		// createProcess() without most of the scanning-related services or ActiveMQ available, and the servlet still
		// preprocesses the request and returns without throwing exceptions. But that could well change in future!
		scanServlet.createProcess(scanBean, null);

		// Check the names have been correctly changed by the preprocessors
		assertEquals(CONFIGURED_X_MOTOR_NAME, gridModel.getFastAxisName());
		assertEquals(CONFIGURED_Y_MOTOR_NAME, gridModel.getSlowAxisName());
		assertEquals(CONFIGURED_X_MOTOR_NAME, mandelbrotModel.getRealAxisName());
		assertEquals(CONFIGURED_Y_MOTOR_NAME, mandelbrotModel.getImaginaryAxisName());
	}
}
