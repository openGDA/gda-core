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

package uk.ac.diamond.daq.guigenerator.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.Test;

import uk.ac.diamond.daq.guigenerator.ListenableProxyFactory;
import uk.ac.diamond.daq.guigenerator.ListenableProxyInvocationHandler;

public class ListenableProxyFactoryTest {
	@Test
	public void testCreatesProxyOfSameType(){
		TestExample proxy = createProxy(TestExample.class);
		assertThat(proxy, is(instanceOf(TestExample.class)));
	}

	@Test
	public void testCreatesThatImplementsPropertyChangeListeningAddAndRemove() throws Exception{
		TestExample proxy = createProxy(TestExample.class);
		assertThat(proxy.getClass().getMethod("addPropertyChangeListener", PropertyChangeListener.class), notNullValue());
		assertThat(proxy.getClass().getMethod("removePropertyChangeListener", PropertyChangeListener.class), notNullValue());
	}

	@Test
	public void testUsesTheListeningHandler() throws Exception{
		TestExample proxy = createProxy(TestExample.class);
		assertThat(Proxy.getInvocationHandler(proxy), is(instanceOf(ListenableProxyInvocationHandler.class)));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testFailsToMockMethodsWithGenericTypes(){
		createProxy(GenericTestExample.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T createProxy(Class<T> clazz) {
		InvocationHandler nullHandler = (o,m,a)->{return null;};
		T deligate = (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{clazz}, nullHandler);

		return new ListenableProxyFactory().createProxyFor(deligate, clazz);
	}

	public interface TestExample{
	}
	public interface GenericTestExample{
		public List<String> getList();
		public void setList(List<String> list);
	}
}
