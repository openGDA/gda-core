/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

// this text is in file org.gda.tango/src/gda/device/package-info.java
/**
 * org.gda.tango <code>gda.device</code> in is a set of code for talking to Tango devices
 * 
 *      <p>
 *      <code>gda.device.TangoDevice</code> - interface which matches the methods used from the Tango class DeviceProxy
 *      <code>gda.device.TangoDeviceProxy</code> - delegates to a TangoDevice implemenation. has helpers functions for 
 *      that translates from Tango classes to Java native classes.
 *      
 *      <code>gda.device.impl.TangoDeviceImpl</code> - real implementation that talks to DeviceProxy
 *      <code>gda.device.impl.DummyTangoDeviceImpl</code> - dummy implementation that handles its own database for attribute values
 *      <code>gda.device.impl.TangoDeviceLogger</code> - implementation that delegates to another implementation and logs all calls
 *      <code>gda.device.impl.TangoDeviceLogger</code> - implementation that delegates to another implementation and logs all calls
 *      
 *      <code>gda.device.base.Base</code> - interface for a base class that has methods that all tango device support. 
 *      <code>gda.device.base.impl.BaseImpl</code> - implemenation that uses TangoDeviceProxy. Provides access to TangoDeviceProxy for derived classes
 *      
 *      <code>gda.device.lima.LimaCCD</code> - interface for Lima devices
 *      <code>gda.device.lima.impl.LimaCCDImpl</code> - extends BaseImplfor access to TangoDeviceproxy and implements Lima
 *      <code>gda.device.lima.impl.DummyLimaTangoDevice</code> - extends DummyTangoDeviceImpl and makes use LimaCCD to make the combined object respond to
 *         correctly to StartAcq command and getStatus. Will create image files from a supplied set by copying to destination. 
 *       To make use of DummyLimaTangoDevice simply create it and use in construction of the LimaCCDImpl.
 *      
 */
package gda.device;