/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.remoting.server;

import static gda.factory.corba.util.EventService.USE_JMS_EVENTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.AUTO_EXPORT_RMI_PREFIX;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.rmi.RmiInvocationHandler;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;
import org.springframework.util.SocketUtils;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.factory.Findable;
import gda.jython.accesscontrol.RbacUtils;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This test automated RMI exporting by actually exporting services locally then checking they can be found and what
 * interfaces are used.
 * <p>
 * Note: This is not a pure unit test it uses sockets for this reason it tries to be nice by finding an available port.
 *
 * @author James Mudd
 */
public class RmiAutomatedExporterTest {

	private RmiAutomatedExporter rmiAutoExporter;
	private Registry rmiRegistry;
	private static int portForTesting;

	@Mock
	private ApplicationContext appContext;

	@BeforeClass
	public static void setupClass() throws Exception {
		LocalProperties.set(USE_JMS_EVENTS, "true"); // Test using JMS events, prevents Corba exceptions
		// Need to find a free port as this test might be running simultaneously on the same machine
		portForTesting = SocketUtils.findAvailableTcpPort(1099, 10000);
		// Set the property this is used by the RmiAutomatedExporter
		LocalProperties.set(RMI_PORT_PROPERTY, Integer.toString(portForTesting));
		LocalProperties.set(LocalProperties.GDA_ACCESS_CONTROL_ENABLED, "true");
	}

	@Before
	public void setUpTest() throws Exception {
		rmiAutoExporter = new RmiAutomatedExporter();
		MockitoAnnotations.initMocks(this);

		// Create local RMI registry to check the objects are exported
		RmiRegistryFactoryBean rmiRegistryFactoryBean = new RmiRegistryFactoryBean();
		rmiRegistryFactoryBean.setPort(portForTesting);
		rmiRegistryFactoryBean.afterPropertiesSet();
		rmiRegistry = rmiRegistryFactoryBean.getObject();
	}

	@After
	public void tearDown() {
		rmiAutoExporter.shutdown();
	}

	@AfterClass
	public static void tearDownClass() {
		LocalProperties.clearProperty(USE_JMS_EVENTS);
		LocalProperties.clearProperty(RMI_PORT_PROPERTY);
	}

	@Test
	public void testNothingInContext() throws Exception {
		rmiAutoExporter.setApplicationContext(appContext);
		rmiAutoExporter.afterPropertiesSet();
	}

	@Test
	public void testObjectIsExportedWithCorrectInterface() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		objects.put("testObj", new TestScannable("testObj"));
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		rmiAutoExporter.afterPropertiesSet();

		assertExported("testObj");
		assertExportedInterface("testObj", Scannable.class);
	}

	@Test
	public void testRbacWrappedExport() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		Findable findable = new TestScannable("testRbacObj");
		Findable wrapped = RbacUtils.wrapFindableWithInterceptor(findable);
		assertThat("Wrapped findable should be wrapped", RbacUtils.objectIsCglibProxy(wrapped));
		objects.put("testRbacObj", wrapped);
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		rmiAutoExporter.afterPropertiesSet();

		assertExported("testRbacObj");
	}

	@Test(expected=NotBoundException.class) // Should throw to indicate the object is not exported
	public void testObjectsWithoutServiceInterfaceAreNotExported() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		objects.put("testObj", new TestFindableWithoutServiceInterface("testObj"));
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		rmiAutoExporter.afterPropertiesSet();

		assertExported("testObj");
	}

	@Test(expected=NotBoundException.class) // Should throw to indicate the object is not exported
	public void testRbacWrappedExportWithoutServiceInterface() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		TestFindableWithoutServiceInterface findable = new TestFindableWithoutServiceInterface("testRbacObj");
		Findable wrapped = RbacUtils.wrapFindableWithInterceptor(findable);
		assertThat("Wrapped findable should be wrapped", RbacUtils.objectIsCglibProxy(wrapped));
		objects.put("testRbacObj", wrapped);
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		rmiAutoExporter.afterPropertiesSet();

		assertExported("testRbacObj");
	}

	@Test(expected=NotBoundException.class) // Should throw to indicate the object is not exported
	public void testLocalObjectsAreNotExported() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		TestScannable localScannable = new TestScannable("testObj");
		localScannable.setLocal(true);
		objects.put("testObj", localScannable);
		when(appContext.getBeansOfType(Findable.class)).thenReturn(objects);

		rmiAutoExporter.setApplicationContext(appContext);
		rmiAutoExporter.afterPropertiesSet();

		assertExported("testObj");
	}

	private void assertExported(String name) throws Exception {
		assertNotNull(rmiRegistry.lookup(AUTO_EXPORT_RMI_PREFIX + name));
	}

	private void assertExportedInterface(String name, Class<?> clazz) throws Exception {
		// Cast should be ok we are using Spring to do the exporting
		RmiInvocationHandler remote = (RmiInvocationHandler) rmiRegistry.lookup(AUTO_EXPORT_RMI_PREFIX + name);
		assertEquals(clazz.getName(), remote.getTargetInterfaceName());
	}

	@ServiceInterface(Scannable.class)
	private static class TestScannable extends ScannableBase {
		private String name;

		public TestScannable() { // Needed by RBAC
			this("testScannable");
		}
		public TestScannable(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}
	}

	private static class TestFindableWithoutServiceInterface extends DeviceBase {
		public TestFindableWithoutServiceInterface() { // Needed by RBAC
			this("testFindable");
		}
		public TestFindableWithoutServiceInterface(String name) {
			setName(name);
		}
	}

}
