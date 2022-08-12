/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.spring;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.Serializable;
import java.util.Dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;

import uk.ac.diamond.daq.osgi.OsgiService;
import uk.ac.gda.core.GDACoreActivator;

@RunWith(MockitoJUnitRunner.class)
public class OsgiServiceBeanHandlerTest {

	// Class under test
	OsgiServiceBeanHandler osgiServiceBeanHandler;

	@Mock
	BundleContext mockContext;

	@BeforeEach
	public void setup() throws Exception {

		// Set mock in bundle context
		GDACoreActivator activator = new GDACoreActivator(); // Make an instance to call method that sets static field!
		activator.start(mockContext); // Inject the mock

		osgiServiceBeanHandler = new OsgiServiceBeanHandler();
	}

	@Test
	public void testBeanWithoutAnnotationDoesntAlterContext() throws Exception {
		osgiServiceBeanHandler.processBean("test", new NoOsgiServiceInterface());
		verifyNoInteractions(mockContext);
	}

	@Test
	public void testBeanWithAnnotationIsPutInContext() throws Exception {
		WithOsgiServiceInterface service = new WithOsgiServiceInterface();

		osgiServiceBeanHandler.processBean("test", service);

		// First verify the calls used to determine if the object is already registered
		verify(mockContext).getServiceReferences(Serializable.class, null);

		// Then verify the service is registered
		verify(mockContext).registerService(eq(Serializable.class.getCanonicalName()), same(service), ArgumentMatchers.<Dictionary<String,Object>>any());
		verifyNoMoreInteractions(mockContext);
	}

	@Test(expected=BeanNotOfRequiredTypeException.class)
	public void testBeanWithIncorrectAnnotationThrows() throws Exception {
		WithIncorrectOsgiServiceInterface service = new WithIncorrectOsgiServiceInterface();

		osgiServiceBeanHandler.processBean("test", service);
	}

	@Test
	public void testBeanAlreadyInContextIsNotAdded() throws Exception {
		WithOsgiServiceInterface service = new WithOsgiServiceInterface();

		// Add it to the service register it already got in there some other way
		@SuppressWarnings("unchecked") // Generics can't be handled
		ServiceReference<Serializable> mockServiceReference = mock(ServiceReference.class);

		// Verify no more interaction specifically that nothing was added to the context
		verifyNoMoreInteractions(mockContext, mockServiceReference);
	}

	private class NoOsgiServiceInterface {}

	@OsgiService(Serializable.class)
	private class WithOsgiServiceInterface implements Serializable {}

	@OsgiService(Cloneable.class)
	private class WithIncorrectOsgiServiceInterface implements Serializable {}
}
