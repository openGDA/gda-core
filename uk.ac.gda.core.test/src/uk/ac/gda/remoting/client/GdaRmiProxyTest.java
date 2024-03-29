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

package uk.ac.gda.remoting.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gda.device.Scannable;
import gda.factory.Finder;

@ExtendWith(MockitoExtension.class)
public class GdaRmiProxyTest {

	// Class under test
	private GdaRmiProxy gdaRmiProxy;

	@Mock
	private RmiProxyFactory rmiProxyFactory;

	@BeforeEach
	public void setUp() throws Exception {
		gdaRmiProxy = new GdaRmiProxy();
		Finder.removeAllFactories();
		Finder.addFactory(rmiProxyFactory);
	}

	@Test
	public void testExceptionThrownIfNameIsNotSet() throws Exception {
		assertThrows(NullPointerException.class, gdaRmiProxy::afterPropertiesSet);
	}

	@Test
	public void testGettingObjectWorks() throws Exception {
		gdaRmiProxy.setBeanName("test");

		final Scannable mockScannable = mock(Scannable.class);
		when(rmiProxyFactory.getFindable("test")).thenReturn(mockScannable);

		// Import the scannable
		gdaRmiProxy.afterPropertiesSet();

		assertThat(gdaRmiProxy.getObject(), is(sameInstance(mockScannable)));
		assertThat(gdaRmiProxy.getObject(), is(instanceOf(Scannable.class)));
		assertThat(Scannable.class.isAssignableFrom(gdaRmiProxy.getObjectType()), is(true));
	}

	@Test
	public void testGetObjectThrowsWhenObjectIsNotSetYet() throws Exception {
		// Defined by FactoryBean#getObject
		assertThrows(FactoryBeanNotInitializedException.class, gdaRmiProxy::getObject);
	}

	@Test // Defined by FactoryBean#getObjectType
	public void testGetObjectTypeReturnsNullWhenObjectIsNotSetYet() throws Exception {
		assertThat(gdaRmiProxy.getObjectType(), is(nullValue()));
	}

	@Test
	public void testIsSingletonReturnsFalse() throws Exception {
		assertThat(gdaRmiProxy.isSingleton(), is(false));
	}

	// This should throw within the timeout as this context cannot be resolved
	@Test
	@Timeout(value = 10000, unit = TimeUnit.MILLISECONDS)
	public void testDaq1945NoInfinteLoopResolvingContext() throws Exception {
		// Try to resolve a context containing 10 GdaRmiProxy but no RmiProxyFactory. Use this class to class load with
		assertThrows(BeanCreationException.class,
				() -> new ClassPathXmlApplicationContext("testSpringContext.xml", GdaRmiProxyTest.class));
	}
}
