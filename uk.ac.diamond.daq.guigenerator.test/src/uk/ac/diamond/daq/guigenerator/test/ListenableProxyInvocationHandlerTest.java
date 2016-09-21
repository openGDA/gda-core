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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.eclipse.richbeans.api.generator.IListenableProxyFactory.PropertyChangeInterface;
import org.junit.Test;

import uk.ac.diamond.daq.guigenerator.ListenableProxyInvocationHandler;

public class ListenableProxyInvocationHandlerTest {
	@Test
	public void testCallsUnderlyingMethod(){
		TestExample example = new TestExampleImpl("not bob", true);

		TestExample proxy = createProxy(example, new PropertyChangeSupport(example));
		proxy.setName("bob");

		assertThat(example.getName(), is("bob"));
	}

	@Test
	public void testCallsUnderlyingMethodAndReturnsValue(){
		TestExample example = new TestExampleImpl("bob", true);

		TestExample proxy = createProxy(example, new PropertyChangeSupport(example));

		assertThat(proxy.getName(), is("bob"));
	}

	@Test
	public void testFiresEventForSetters(){
		TestExample example = new TestExampleImpl("bob", true);

		PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(example);
		TestExample proxy = createProxy(example, propertyChangeSupport);

		proxy.setName("joe");

		assertThat(example.getName(), is("joe"));
		propertyChangeSupport.firePropertyChange("name", "bob", "joe");
	}

	@Test
	public void testFiresEventForBooleanSettersWhereGetterStartsWithIs(){
		TestExample example = new TestExampleImpl("bob", false);
		PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(example);
		RecordingPropertyChangeListener listener = new RecordingPropertyChangeListener();
		propertyChangeSupport.addPropertyChangeListener(listener);

		TestExample proxy = createProxy(example, propertyChangeSupport);

		proxy.setAwesome(true);

		assertThat(example.isAwesome(), is(true));
		PropertyChangeEvent lastEvent = listener.getLastEvent();
		assertThat(lastEvent.getPropertyName(), is("awesome"));
		assertThat(lastEvent.getOldValue(), is(false));
		assertThat(lastEvent.getNewValue(), is(true));
	}

	@Test
	public void testAddsListenersToChangeSupport(){
		TestExample example = new TestExampleImpl("bob", true);
		PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(example);
		PropertyChangeListener listener = new RecordingPropertyChangeListener();

		TestExample proxy = createProxy(example, propertyChangeSupport);

		((PropertyChangeInterface)proxy).addPropertyChangeListener(listener);

		assertThat(Arrays.asList(propertyChangeSupport.getPropertyChangeListeners()), hasItem(listener));
	}

	@Test
	public void testRemovesListenersFromChangeSupport(){
		TestExample example = new TestExampleImpl("bob", true);
		PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(example);
		PropertyChangeListener listener = new RecordingPropertyChangeListener();

		TestExample proxy = createProxy(example, propertyChangeSupport);

		((PropertyChangeInterface)proxy).removePropertyChangeListener(listener);

		assertThat(Arrays.asList(propertyChangeSupport.getPropertyChangeListeners()), not(hasItem(listener)));
	}

	@Test
	public void testDeligatesToOtherMethods(){
		TestExample example = new TestExampleImpl("bob", true);

		createProxy(example, new PropertyChangeSupport(example)).doSomething();

		assertTrue(((TestExampleImpl)example).somethingWasDone);
	}

	@SuppressWarnings("unchecked")
	private <T> T createProxy(T example, PropertyChangeSupport propertyChangeSupport) {
		InvocationHandler handler = new ListenableProxyInvocationHandler<T>(example, propertyChangeSupport);
		return (T) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[]{TestExample.class, PropertyChangeInterface.class},
				handler
			);
	}

	public interface TestExample{
		public String getName();
		public void setName(String name);
		public void doSomething();
		public boolean isAwesome();
		public void setAwesome(boolean awesome);
	}

	public class TestExampleImpl implements TestExample{
		private String name;
		private boolean awesome;
		public boolean somethingWasDone = false;

		public TestExampleImpl(String name, boolean awesome) {
			this.name = name;
			this.awesome = awesome;
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
		public void doSomething() {
			this.somethingWasDone = true;
		}
		@Override
		public boolean isAwesome() {
			return awesome;
		}
		@Override
		public void setAwesome(boolean awesome) {
			this.awesome = awesome;
		}
	}

	public class RecordingPropertyChangeListener implements PropertyChangeListener{
		private PropertyChangeEvent event;

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			this.event = event;
		}

		public PropertyChangeEvent getLastEvent(){
			return event;
		}
	}
}
