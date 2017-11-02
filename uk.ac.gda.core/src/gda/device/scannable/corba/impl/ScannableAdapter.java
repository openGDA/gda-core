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

package gda.device.scannable.corba.impl;

import java.io.Serializable;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyArray;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySlice;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.corba.CorbaScannable;
import gda.device.scannable.corba.CorbaScannableHelper;
import gda.factory.corba.util.NetService;

/**
 * Client-side implementation of distributed Scannable objects
 */
public class ScannableAdapter extends DeviceAdapter implements Scannable {

	private static final Logger logger = LoggerFactory.getLogger(ScannableAdapter.class);

	private CorbaScannable corbaScannable;

	/**
	 * Constructor. Calls DeviceAdapter contructor.
	 *
	 * @param obj
	 * @param name
	 * @param netService
	 */
	public ScannableAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {

		super(obj, name, netService);
		corbaScannable = CorbaScannableHelper.narrow(obj);
	}

	@Override
	public void asynchronousMoveTo(java.lang.Object position) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				if (position instanceof PyObject) {
					position = ScannableUtils.convertToJava((PyObject) position);
				}
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) position);
				corbaScannable.asynchronousMoveTo(any);
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atPointEnd() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atPointEnd();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atPointStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atPointStart();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atScanLineEnd();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atScanEnd() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atScanEnd();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atScanStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atScanStart();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atScanLineStart();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atLevelMoveStart();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atLevelStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atLevelStart();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atLevelEnd() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atLevelEnd();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String[] getExtraNames() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaScannable.getExtraNames();
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
		return null;
	}

	@Override
	public String[] getInputNames() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaScannable.getInputNames();
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
		return null;
	}

	@Override
	public int getLevel() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaScannable.getLevel();
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
		return 0;
	}

	@Override
	public java.lang.Object getPosition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaScannable.getPosition();
				return any.extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaScannable.isBusy();
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void moveTo(java.lang.Object position) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				if (position instanceof PyObject) {
					position = ScannableUtils.convertToJava((PyObject) position);
				}
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) position);
				corbaScannable.moveTo(any);
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setExtraNames(String[] names) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.setExtraNames(names);
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setInputNames(String[] names) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.setInputNames(names);
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void setLevel(int level) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.setLevel(level);
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.stop();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.waitWhileBusy();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String[] getOutputFormat() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaScannable.getOutputFormat();
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
		return null;
	}

	@Override
	public String checkPositionValid(java.lang.Object position) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				if (position instanceof PyObject) {
					position = ScannableUtils.convertToJava((PyObject) position);
				}
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) position);
				org.omg.CORBA.Any returnMessage = corbaScannable.checkPositionValid(any);
				Object returnValue = returnMessage.extract_Value();
				if (returnValue instanceof NullString){
					return null;
				}
				return (String) returnValue;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public boolean isAt(java.lang.Object position) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) position);
				return corbaScannable.isAt(any);
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		return true;
	}

	@Override
	public void setOutputFormat(String[] names) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.setOutputFormat(names);
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	@Deprecated
	public void atEnd() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atEnd();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	@Deprecated
	public void atStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atStart();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	/**
	 * {@inheritDoc} Default behaviour is to return getName() : getPosition.toString()
	 *
	 * @see gda.device.Scannable#toString()
	 */
	@Override
	public String toString() {
		try {
			return ScannableUtils.getFormattedCurrentPosition(this);
		} catch (Exception e) {
			logger.warn("{}: exception while getting position. ", getName(), e);
			return valueUnavailableString();
		}
	}

	/**
	 * @see org.python.core.PyObject#__call__()
	 * @return the position of this object as a native or array of natives
	 */
	@Override
	public PyObject __call__() {
		Object position;
		try {
			position = getPosition();
		} catch (Exception e) {
			logger.info("{}: exception while getting position. ", getName(), e);
			return new PyString(getName());
		}

		// if no input or extra names
		if (position == null) {
			return null;
		}

		// convert internal value into a PyObject
		if (position instanceof Double) {
			return new PyFloat((Double) position);
		} else if (position instanceof Integer) {
			return new PyFloat((Integer) position);
		} else if (position instanceof Short) {
			return new PyFloat((Short) position);
		} else if (position instanceof Float) {
			return new PyFloat((Float) position);
		} else if (position instanceof Long) {
			return new PyFloat((Long) position);
		} else if (position instanceof String) {
			return new PyString((String) position);
		} else if (position instanceof PyObject) {

			return (PyObject) (position);
		}
		// if its an array, return an array of floats or strings
		else if (position.getClass().isArray()) {
			try {
				if (position instanceof String[]) {
					double[] currentPosition = ScannableUtils.getCurrentPositionArray(this);
					PyArray pycurrentPosition = new PyArray(PyString.class, currentPosition.length);

					for (double element : currentPosition) {
						pycurrentPosition.__add__(new PyFloat(element));
					}

					return pycurrentPosition;

				}
				// else
				double[] currentPosition = ScannableUtils.getCurrentPositionArray(this);
				PyArray pycurrentPosition = new PyArray(PyFloat.class, currentPosition.length);

				for (int i = 0; i < currentPosition.length; i++) {
					pycurrentPosition.__setitem__(i, new PyFloat(currentPosition[i]));
				}

				return pycurrentPosition;
			} catch (Exception e) {
				logger.info("{}: exception while converting array of positions to a string. ",
						getName(),
						e);
				return this.__str__();
			}
		}
		// at the very least, create a String and return that
		else {
			return this.__str__();
		}
	}

	/**
	 * @see org.python.core.PyObject#__call__(org.python.core.PyObject)
	 * @param new_position
	 * @return a pretty print string representation of this object
	 */
	@Override
	public PyObject __call__(PyObject new_position) {
		try {
			moveTo(new_position);
			return new PyString("Move complete: " + this.toString());
		} catch (Exception e) {
			return new PyString("Move failed for " + getName() + " " + e.getMessage());
		}
	}

	/**
	 * @return the size of input names. This is intentional as slicing should only work over input parameters. (A design
	 *         request)
	 */
	@Override
	public int __len__() {
		return getInputNames().length;
	}

	// methods to allow interaction with Matrices
	/**
	 * @param index
	 *            a number or a PySlice object
	 * @return the part of the objects array of position as defined by index
	 */
	@Override
	public PyObject __getitem__(PyObject index) {
		double[] currentPosition;
		try {
			currentPosition = ScannableUtils.getCurrentPositionArray(this);
		} catch (Exception e) {
			logger.info("{}: exception while converting array of positions to a string. ", getName(), e);
			return null;
		}

		if (index instanceof PyInteger) {
			if (((PyInteger) index).getValue() < __len__()) {
				return new PyFloat(currentPosition[((PyInteger) index).getValue()]);
			}
		} else if (index instanceof PySlice) {
			// only react if the command was [0] or [:]
			PySlice slice = (PySlice) index;

			int start, stop, step;

			// start
			if (slice.start instanceof PyNone) {
				start = 0;
			} else {
				start = ((PyInteger) slice.start).getValue();
			}

			// stop
			if (slice.stop instanceof PyNone) {
				stop = this.__len__() - 1;
			} else {
				stop = ((PyInteger) slice.stop).getValue();
			}

			// step
			step = ((PyInteger) slice.step).getValue();

			int numberElements = 0;
			for (int i = start; i <= stop; i += step) {
				numberElements++;
			}

			PyArray output = new PyArray(PyFloat.class, numberElements);
			int j = 0;
			for (int i = start; i <= stop; i += step) {
				output.__setitem__(j, new PyFloat(currentPosition[i]));
				j++;
			}

			return output;

		}
		return null;
	}

	/**
	 * Jython method to return string description of the object
	 *
	 * @return the result of the toString method
	 */
	@Override
	public PyString __str__() {
		return new PyString(toString());
	}

	/**
	 * Jython method to return a string representation of the object
	 *
	 * @return the result of the toString method
	 */
	@Override
	public PyString __repr__() {
		return __str__();
	}

	/**
	 * @return PyString -the name of the object
	 */
	public PyString __doc__() {
		return new PyString(getName());
	}

	@Override
	public PyObject __eq__(PyObject other) {
		try {
			String othername = (String) other.getClass().getMethod("getName", (Class<?>[]) null).invoke(other,
					(Object[]) null);

			// if the classes and names are the same then the objects must be
			// the same
			if (this.getClass() == other.getClass() && this.getName().compareTo(othername) == 0) {
				return new PyInteger(1);
			}
		} catch (Exception e) {
			return new PyInteger(0);
		}
		return new PyInteger(0);
	}

	@Override
	public boolean equals(Object ob_other) {

		// if its a PyObject and __eq__ returns true then assume the same
		if (ob_other != null) {
			if (ob_other == this) {
				return true;
			}
			if (ob_other instanceof PyObject) {
				PyInteger eqTest = (PyInteger) __eq__((PyObject) ob_other);
				return (eqTest.getValue() == 1);
			}
		}
		return false;
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannable.atCommandFailure();
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String toFormattedString() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaScannable.toFormattedString();
			} catch (COMM_FAILURE cf) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannable = CorbaScannableHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				logger.warn("Exception formatting Corba scannable {}", getName(), ex);
				return valueUnavailableString();
			}
		}
		logger.warn("Communication failure: retry failed");
		return valueUnavailableString();
	}

	/**
	 * Name/value string that can be used by to[Formatted]String() when getting current value/position fails
	 */
	protected String valueUnavailableString() {
		return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
	}

}
