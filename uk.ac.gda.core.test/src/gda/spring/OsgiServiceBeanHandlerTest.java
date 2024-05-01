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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;

import uk.ac.diamond.daq.osgi.OsgiService;

@ExtendWith(MockitoExtension.class)
public class OsgiServiceBeanHandlerTest {

	// Class under test
	OsgiServiceBeanHandler osgiServiceBeanHandler;

	@Mock
	BundleContext mockContext;

	@Mock
	MockedStatic<FrameworkUtil> frameworkMock;

	@Mock
	Bundle mockBundle;

	@BeforeEach
	public void setup() throws Exception {
		frameworkMock.when(() -> FrameworkUtil.getBundle(OsgiServiceBeanHandler.class))
		.thenReturn(mockBundle);
		when(mockBundle.getBundleContext()).thenReturn(mockContext);

		// Now instantiate class under test
		osgiServiceBeanHandler = new OsgiServiceBeanHandler();
	}

	@Test
	public void testBeanWithoutAnnotationDoesntAlterContext() throws Exception {
		osgiServiceBeanHandler.postProcessAfterInitialization(new NoOsgiServiceInterface(), "test");
		verifyNoInteractions(mockContext);
	}

	@Test
	public void testBeanWithAnnotationIsPutInContext() throws Exception {
		WithOsgiServiceInterface service = new WithOsgiServiceInterface();

		osgiServiceBeanHandler.postProcessAfterInitialization(service, "test");

		// First verify the calls used to determine if the object is already registered
		verify(mockContext).getServiceReferences(Serializable.class, null);

		// Then verify the service is registered
		verify(mockContext).registerService(eq(Serializable.class.getCanonicalName()), same(service), ArgumentMatchers.<Dictionary<String,Object>>any());
		verifyNoMoreInteractions(mockContext);
	}

	@Test
	public void testBeanWithIncorrectAnnotationThrows() throws Exception {
		WithIncorrectOsgiServiceInterface service = new WithIncorrectOsgiServiceInterface();
		assertThrows(BeanNotOfRequiredTypeException.class, () -> osgiServiceBeanHandler.postProcessAfterInitialization(service, "test"));
	}

	@Test
	public void testBeanAlreadyInContextIsNotAdded() throws Exception {
		WithOsgiServiceInterface service = new WithOsgiServiceInterface();

		// Add it to the service register it already got in there some other way
		@SuppressWarnings("unchecked") // Generics can't be handled
		ServiceReference<Serializable> mockServiceReference = mock(ServiceReference.class);
		when(mockContext.getServiceReferences(Serializable.class, null)).thenReturn(Arrays.asList(mockServiceReference));
		when(mockContext.getService(mockServiceReference)).thenReturn(service);

		osgiServiceBeanHandler.postProcessAfterInitialization(service, "test");

		// Verify no more interaction specifically that nothing was added to the context
		verifyNoMoreInteractions(mockContext, mockServiceReference);
	}

	private class NoOsgiServiceInterface {}

	@OsgiService(Serializable.class)
	private class WithOsgiServiceInterface implements Serializable {}

	@OsgiService(Cloneable.class)
	private class WithIncorrectOsgiServiceInterface implements Serializable {}
}
