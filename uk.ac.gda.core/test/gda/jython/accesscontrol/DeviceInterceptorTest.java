/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.accesscontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import gda.device.Device;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.jython.GDAJythonInterpreter;
import gda.jython.JythonServer.JythonServerThread;
import gda.observable.IObserver;

import org.junit.Test;

/**
 * Test of DeviceInterceptor
 *
 */
public class DeviceInterceptorTest {

	/**
	 * Test of equals operator
	 * 
	 */
	@Test
	public void testEqualsObject() {
		SimpleDevice simpleDevice1 = new SimpleDevice();
		simpleDevice1.setName("SimpleDevice1");
		Device dev1 = DeviceInterceptor.newDeviceInstance(simpleDevice1);

		assertFalse(dev1.equals(null));
		assertFalse(dev1.equals(1));
		
		SimpleDevice simpleDevice2 = new SimpleDevice();
		simpleDevice2.setName("SimpleDevice2");
		Device dev2 = DeviceInterceptor.newDeviceInstance(simpleDevice2);
		
		assertFalse(dev1.equals(dev2));
		assertFalse(dev1.hashCode() == dev2.hashCode());

		simpleDevice2.setName(simpleDevice1.getName());
		assertEquals(dev1, dev2);
		assertEquals(simpleDevice1, simpleDevice2);
		assertEquals(dev1.hashCode(),dev2.hashCode());
		
	}

	/**
	 * Test of equals operator of class derived from DeviceBase
	 * 
	 */
	@Test
	public void testDeviceBase() {
		SimpleDeviceBase simpleDevice1 = new SimpleDeviceBase();
		simpleDevice1.setName("SimpleDevice1");
		Device dev1 = DeviceInterceptor.newDeviceInstance(simpleDevice1);

		assertFalse(dev1.equals(null));
		assertFalse(dev1.equals(1));
		
		SimpleDeviceBase simpleDevice2 = new SimpleDeviceBase();
		simpleDevice2.setName("SimpleDevice2");
		Device dev2 = DeviceInterceptor.newDeviceInstance(simpleDevice2);
		
		assertFalse(dev1.equals(dev2));
		assertFalse(dev1.hashCode() == dev2.hashCode());

		simpleDevice2.setName(simpleDevice1.getName());
		assertEquals(dev1, dev2);
		assertEquals(dev1.hashCode(),dev2.hashCode());

	}	
	
	/**
	 * Test of equals operator
	 * 
	 */
	@Test
	public void testArrayCopy() {
		SimpleDevice simpleDevice1 = new SimpleDevice();
		simpleDevice1.setName("SimpleDevice1");
		Device dev1 = DeviceInterceptor.newDeviceInstance(simpleDevice1);

		Device [] deviceArraySrc = new Device[]{dev1};
		Object [] deviceArrayTarget = new Object[]{null};
		System.arraycopy(deviceArraySrc, 0, deviceArrayTarget, 0, 1);
		assertEquals(dev1, deviceArrayTarget[0]);
		
	}	
	
	/**
	 * Test of equals operator in JythonServerThread
	 * 
	 * @throws Exception if the test fails
	 */
	@Test
	public void testEqualsObjectInJythonServerThread() throws Exception {
		int authorisationLevel =1;
		TestRunner runner = new TestRunner(null, null, authorisationLevel);
		
		runner.start();
		runner.join();
		if(runner.ex != null){
			throw new RuntimeException("TestRunner execution resulted in an exception", runner.ex);
		}
	}

	private static class TestRunner extends JythonServerThread {
		/**
		 * A string representing the result of the evaluated command.
		 */
		public Exception ex = null;

		/**
		 * Constructor.
		 * 
		 * @param interpreter
		 * @param command
		 * @param authorisationLevel
		 */
		public TestRunner(@SuppressWarnings("unused") GDAJythonInterpreter interpreter, @SuppressWarnings("unused") String command, int authorisationLevel) {
			this.authorisationLevel = authorisationLevel;
		}

		@Override
		public void run() {

			SimpleDevice simpleDevice1 = new SimpleDevice();
			simpleDevice1.setName("SimpleDevice1");
			Device dev1 = DeviceInterceptor.newDeviceInstance(simpleDevice1);
			try{
				dev1.setProtectionLevel(1);
				dev1.setAttribute("Test", 2);

				dev1.setProtectionLevel(2);
				dev1.setAttribute("Test", 2);
				throw new Exception("Should not have allowed me to call setAttribute");
			
			}
			catch(AccessDeniedException ex){
				//do nothing - this is correct
			}
			catch(Exception ex){
				this.ex = ex;
			}

		}
	}
}

class SimpleDevice implements Device{

	String name="";
	int protectionLevel=1;
	@Override
	public void close() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return protectionLevel;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		protectionLevel = newLevel;
		
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteIObservers() {
		// TODO Auto-generated method stub
		
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
	public void reconfigure() throws FactoryException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean equals(Object obj) {
		if( obj == null)
			return false;
		if( !(obj instanceof SimpleDevice)){
			return false;
		}
		SimpleDevice other = (SimpleDevice)obj;
		if(name == null || other.name == null)
			return false;
		return name.equals(other.name);
	}

	@Override
	public String toString() {
		return name == null ? "unknown" : name.toString();
	}

	@Override
	public int hashCode() {
		return name == null ? 42 : name.hashCode();
	}	
	
}

class SimpleDeviceBase extends DeviceBase{

	SimpleDeviceBase(){
	}
	
	@Override
	public void configure(){
		// no configuration required
	}

	@Override
	public boolean equals(Object obj) {
		if( obj == null)
			return false;
		if( !(obj instanceof SimpleDeviceBase)){
			return false;
		}
		SimpleDeviceBase other = (SimpleDeviceBase)obj;
		if(getName() == null || other.getName() == null)
			return false;
		return getName().equals(other.getName());
	}

	@Override
	public String toString() {
		return getName() == null ? "unknown" : getName().toString();
	}

	@Override
	public int hashCode() {
		return getName() == null ? 42 : getName().hashCode();
	}		
}

