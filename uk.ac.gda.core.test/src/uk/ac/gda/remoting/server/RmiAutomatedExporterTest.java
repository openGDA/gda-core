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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.AUTO_EXPORT_RMI_PREFIX;
import static uk.ac.gda.remoting.server.RmiAutomatedExporter.RMI_PORT_PROPERTY;

import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.remoting.rmi.RmiInvocationHandler;
import org.springframework.remoting.rmi.RmiRegistryFactoryBean;
import org.springframework.util.SocketUtils;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.factory.Factory;
import gda.factory.Findable;
import gda.factory.Finder;
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
	private Factory mockFactory;

	@BeforeClass
	public static void setupClass() throws Exception {
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
		when(mockFactory.isLocal()).thenReturn(true);

		// Create local RMI registry to check the objects are exported
		RmiRegistryFactoryBean rmiRegistryFactoryBean = new RmiRegistryFactoryBean();
		rmiRegistryFactoryBean.setPort(portForTesting);
		rmiRegistryFactoryBean.afterPropertiesSet();
		rmiRegistry = rmiRegistryFactoryBean.getObject();

		Finder.getInstance().addFactory(mockFactory);
	}

	@After
	public void tearDown() {
		rmiAutoExporter.shutdown();
		Finder.getInstance().removeAllFactories();
	}

	@AfterClass
	public static void tearDownClass() {
		LocalProperties.clearProperty(RMI_PORT_PROPERTY);
	}

	@Test
	public void testNothingInContext() throws Exception {
		assertThat(rmiAutoExporter.getRemoteObjectNamesImplementingType(Findable.class.getCanonicalName()), is(empty()));

		// Check mock was interacted with correctly
		verify(mockFactory).getFindablesOfType(Findable.class);
		verify(mockFactory).isLocal();
		verifyNoMoreInteractions(mockFactory);
	}

	@Test
	public void testObjectIsExportedWithCorrectInterface() throws Exception {
		Findable testObj = new TestScannable("testObj");
		when(mockFactory.getFindable("testObj")).thenReturn(testObj);

		assertExported("testObj");
		assertExportedInterface("testObj", Scannable.class);

		// Check mock was interacted with correctly
		verify(mockFactory).getFindable("testObj");
		verify(mockFactory).isLocal();
		verifyNoMoreInteractions(mockFactory);
	}

	@Test
	public void testRbacWrappedExport() throws Exception {

		Findable findable = new TestScannable("testRbacObj");
		Findable wrapped = RbacUtils.wrapFindableWithInterceptor(findable);
		assertThat("Wrapped findable should be wrapped", RbacUtils.objectIsCglibProxy(wrapped));
		when(mockFactory.getFindable("testRbacObj")).thenReturn(wrapped);

		assertExported("testRbacObj");

		// Check mock was interacted with correctly
		verify(mockFactory).getFindable("testRbacObj");
		verify(mockFactory).isLocal();
		verifyNoMoreInteractions(mockFactory);
	}

	@Test
	public void testObjectsWithoutServiceInterfaceAreNotExported() throws Exception {

		Findable testObj =  new TestFindableWithoutServiceInterface("testObj");
		when(mockFactory.getFindable("testObj")).thenReturn(testObj);

		// Should return null indicating the object has not been exported
		assertThat(rmiAutoExporter.getRemoteObject("testObj"), is(nullValue()));

		// Check mock was interacted with correctly
		verify(mockFactory).getFindable("testObj");
		verify(mockFactory).isLocal();
		verifyNoMoreInteractions(mockFactory);
	}

	@Test(expected=NotBoundException.class) // Should throw to indicate the object is not exported
	public void testRbacWrappedExportWithoutServiceInterface() throws Exception {

		TestFindableWithoutServiceInterface findable = new TestFindableWithoutServiceInterface("testRbacObj");
		Findable wrapped = RbacUtils.wrapFindableWithInterceptor(findable);

		assertThat("Wrapped findable should be wrapped", RbacUtils.objectIsCglibProxy(wrapped));

		when(mockFactory.getFindable("testRbacObj")).thenReturn(wrapped);

		// Should return null indicating the object has not been exported
		assertThat(rmiAutoExporter.getRemoteObject("testRbacObj"), is(nullValue()));

		// Check mock was interacted with correctly
		verify(mockFactory).getFindable("testRbacObj");
		verify(mockFactory).isLocal();
		verifyNoMoreInteractions(mockFactory);

		// Should throw verifying its not in the registry
		rmiRegistry.lookup(AUTO_EXPORT_RMI_PREFIX + "testRbacObj");
	}

	@Test(expected=NotBoundException.class) // Should throw to indicate the object is not exported
	public void testRequestingMissingObjectDoesntExport() throws Exception {
		when(mockFactory.getFindable("missingObject")).thenReturn(null);

		// Should return null indicating the object has not been exported
		assertThat(rmiAutoExporter.getRemoteObject("missingObject"), is(nullValue()));

		// Check mock was interacted with correctly
		verify(mockFactory).getFindable("missingObject");
		verify(mockFactory).isLocal();
		verifyNoMoreInteractions(mockFactory);

		// Should throw verifying its not in the registry
		rmiRegistry.lookup(AUTO_EXPORT_RMI_PREFIX + "missingObject");
	}

	@Test
	public void testGettingAllFindables() throws Exception {
		Map<String, Findable> objects = new HashMap<>();
		// Add 3 objects which should be exported
		objects.put("object1", new TestScannable("object1"));
		objects.put("object2", new TestScannable("object2"));
		objects.put("object3", new TestScannable("object3"));
		// And one which shouldn't
		objects.put("not-exported-object", new TestFindableWithoutServiceInterface("not-exported-object"));

		when(mockFactory.getFindablesOfType(Findable.class)).thenReturn(objects);

		Set<String> remoteObjectNames = rmiAutoExporter.getRemoteObjectNamesImplementingType(Findable.class.getCanonicalName());

		assertThat(remoteObjectNames, containsInAnyOrder("object1", "object2", "object3"));

		// Check mock was interacted with correctly
		verify(mockFactory).getFindablesOfType(Findable.class);
		verify(mockFactory).isLocal();
		verifyNoMoreInteractions(mockFactory);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGettingAllFindablesWithTypeNotExtendingFindableThrows() throws Exception {
		rmiAutoExporter.getRemoteObjectNamesImplementingType(Map.class.getCanonicalName());
	}

	@Test
	public void testGettingAllFindablesWithTypeMissingClassReturnsEmpty() throws Exception {
		Set<String> missingClass = rmiAutoExporter.getRemoteObjectNamesImplementingType("uk.ac.diamond.daq.test.MadeupMissingClassName");
		assertThat(missingClass, is(empty()));
	}


	private void assertExported(String name) throws Exception {
		// Actually cause the export to happen
		RmiObjectInfo objectInfo = rmiAutoExporter.getRemoteObject(name);

		// Verify the remote object info
		assertThat(objectInfo, is(not(nullValue())));
		assertThat(objectInfo.getName(), is(name));
		assertThat(objectInfo.getUrl(), is(AUTO_EXPORT_RMI_PREFIX + name));

		// Verify its available on the registry
		assertThat(rmiRegistry.lookup(AUTO_EXPORT_RMI_PREFIX + name), is(not(nullValue())));
	}

	private void assertExportedInterface(String name, Class<?> clazz) throws Exception {
		// Cast should be ok we are using Spring to do the exporting
		RmiInvocationHandler remote = (RmiInvocationHandler) rmiRegistry.lookup(AUTO_EXPORT_RMI_PREFIX + name);
		assertEquals(clazz.getName(), remote.getTargetInterfaceName());
	}

	@ServiceInterface(Scannable.class)
	private static class TestScannable extends ScannableBase {
		@SuppressWarnings("unused") // Needed by RBAC
		public TestScannable() {
			this("testScannable");
		}
		public TestScannable(String name) {
			setName(name);
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}
	}

	private static class TestFindableWithoutServiceInterface extends DeviceBase {
		@SuppressWarnings("unused") // Needed by RBAC
		public TestFindableWithoutServiceInterface() {
			this("testFindable");
		}
		public TestFindableWithoutServiceInterface(String name) {
			setName(name);
		}
	}

}
