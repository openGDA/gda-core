/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.analysis.datastructure;

import gda.analysis.datastructure.event.DataChangeEvent;
import gda.analysis.datastructure.event.DataChangeObservable;
import gda.analysis.datastructure.event.DataChangeObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Jama.Matrix;

/**
 * Data storage vector I originally created something more complex which overrode add methods to update the min max on
 * the fly but in the end I thought ended with a monster class. So I've scrapped that and opted for simplicity.
 */
public class DataVector extends Vector<Double> implements java.io.Serializable, DataChangeObservable {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(DataVector.class);

	/**
	 * The dimensions of the data set
	 */
	private int[] dimensions;

	/**
	 * Max length of the data set..... If you append a point to the data set and its greater than maxDataSetSize a point
	 * is removed from the the start of the list...... This can be useful for continous monitoring of some data which
	 * you don't neccesarily want to store.....
	 */
	private int maxDataSetSize = Integer.MAX_VALUE;

	/**
	 * boolean determining if you can extend the the dataset above the size its initialized at
	 */
	private boolean fixedSize = false;

	/** use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = -2767605614048989439L;

	/** Storage for registered change listeners. */
	private List<DataChangeObserver> listeners;

	/** A flag that controls whether or not changes are notified. */
	private boolean notify = true;

	/**
	 * Constructs an empty GenericDataVector with the specified initial capacity and capacity increment.
	 * 
	 * @param dimensions
	 * @exception IllegalArgumentException
	 *                if the specified initial capacity is negative
	 */
	public DataVector(int... dimensions) {
		super();

		if (dimensions.length > 0) {
			this.fixedSize = true;
			this.dimensions = dimensions;
			this.setMaxSize(dimensions);
		} else {
			this.dimensions = new int[] { 0 };
			this.fixedSize = false;
			this.maxDataSetSize = Integer.MAX_VALUE;
		}
		createEmptyList(this.dimensions);
	}

	/**
	 * @param data
	 */
	public DataVector(double[] data) {
		super();
		this.dimensions = new int[] { data.length };
		this.fixedSize = true;
		createListFromArray(data);
	}

	/**
	 * @param length
	 * @param data
	 */
	public DataVector(int length, double[] data) {
		super();
		logger
				.warn("Using DataVector(int length,double[] data) is not recomended unless the size of the Vector you want to create is less than the size of the data, use DataVector(double[] data) instead");
		this.dimensions = new int[] { length };
		this.fixedSize = true;
		createListFromArray(data);
	}

	/**
	 * @param width
	 * @param height
	 * @param data
	 */
	public DataVector(int width, int height, double[] data) {
		super();
		this.dimensions = new int[] { width, height };
		this.fixedSize = true;
		createListFromArray(data);
	}

	/**
	 * @param width
	 * @param height
	 * @param depth
	 * @param data
	 */
	public DataVector(int width, int height, int depth, double[] data) {
		super();
		this.dimensions = new int[] { width, height, depth };
		this.fixedSize = true;
		createListFromArray(data);
	}

	/**
	 * @param inputMatrix
	 */
	public DataVector(Matrix inputMatrix) {
		super();
		double arrayvals[][] = inputMatrix.getArray();
		int dims[] = new int[] { arrayvals.length, arrayvals[1].length };
		this.dimensions = dims;
		this.fixedSize = true;
		createListFromArray(arrayvals);

	}

	/**
	 * @param dimensions
	 */
	public void setMaxSize(int... dimensions) {
		int length = dimensions[0];
		for (int i = 1; i < dimensions.length; i++)
			length *= dimensions[i];
		this.maxDataSetSize = length;
	}

	/**
	 * Create an empty data set to start with
	 * 
	 * @param dimensions
	 *            The size of the array to set to zero
	 */
	public void createEmptyList(int... dimensions) {
		int length = dimensions[0];
		for (int i = 1; i < dimensions.length; i++)
			length *= dimensions[i];
		for (int i = 0; i < length; i++)
			this.add(0.0);
	}

	/**
	 * Creates the datavector from a single dimentional array of doubles
	 * 
	 * @param data
	 *            the data to put in the 1D array
	 */
	public void createListFromArray(double[] data) {
		for (int i = 0; i < data.length; i++)
			this.add(data[i]);
	}

	/**
	 * Creates the datavector from a two dimentional array of doubles
	 * 
	 * @param data
	 *            the 2D array which holds teh data to be put into the datavector;
	 */
	public void createListFromArray(double[][] data) {
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				this.add(data[j][i]);
	}

	/**
	 * Create an empty data set to start with
	 * 
	 * @param data
	 */
	public void createListFromArray(double[][][] data) {
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[0].length; j++)
				for (int k = 0; k < data[0][0].length; k++)
					this.add(data[i][j][k]);
	}

	/**
	 * Returns the flag that controls whether or not change events are sent to registered listeners.
	 * 
	 * @return A boolean.
	 */
	@Override
	public boolean getNotify() {
		return this.notify;
	}

	/**
	 * Sets the flag that controls whether or not change events are sent to registered listeners.
	 * 
	 * @param notify
	 *            the new value of the flag.
	 */
	@Override
	public void setNotify(boolean notify) {
		if (this.notify != notify) {
			this.notify = notify;
			fireDataChanged();
		}
	}

	/**
	 * Registers an object with this series, to receive notification whenever the series changes.
	 * <P>
	 * Objects being registered must implement the {@link DataChangeObserver} interface.
	 * 
	 * @param listener
	 *            the listener to register.
	 */
	@Override
	public synchronized void addChangeListener(DataChangeObserver listener) {
		if (listeners == null)
			listeners = new ArrayList<DataChangeObserver>();
		this.listeners.add(listener);
	}

	/**
	 * Deregisters an object, so that it not longer receives notification whenever the series changes.
	 * 
	 * @param listener
	 *            the listener to deregister.
	 */
	@Override
	public synchronized void removeChangeListener(DataChangeObserver listener) {
		this.listeners.remove(listener);
	}

	/**
	 * General method for signalling to registered listeners that the series has been changed.
	 */
	public synchronized void fireDataChanged() {
		if (this.notify) {
			notifyListeners(new DataChangeEvent(this));
		}
	}

	/**
	 * Sends a change event to all registered listeners.
	 * 
	 * @param event
	 *            contains information about the event that triggered the notification.
	 */
	@Override
	public synchronized void notifyListeners(DataChangeEvent event) {
		if (listeners != null) {
			for (Iterator<DataChangeObserver> itr = listeners.iterator(); itr.hasNext();) {
				DataChangeObserver ist = itr.next();
				ist.dataChanged(event);
			}
		}
	}

	/**
	 * @return Dimensions of the data vector
	 */
	public synchronized int[] getDimensions() {
		return this.dimensions;
	}

	/**
	 * Return the min of the data set
	 * 
	 * @return The min of the data set
	 */
	public synchronized double getMin() {
		double min = Double.MAX_VALUE;
		for (Double x : this) {
			min = Math.min(x, min);
		}
		return min;
	}

	/**
	 * @return The max of the data set
	 */
	public synchronized double getMax() {
		double max = Double.MIN_VALUE;
		for (Double x : this) {
			max = Math.max(x, max);
		}
		return max;
	}

	/**
	 * @return The max of the data set
	 */
	public synchronized double getMean() {
		double sum = 0.0;
		for (Double x : this) {
			sum += x;
		}
		return sum / this.size();
	}

	/**
	 * @return The rms of the data set
	 */
	public synchronized double getRMS() {
		double sum = 0.0;
		for (Double x : this) {
			sum += x * x;
		}
		return Math.sqrt(sum / this.size());
	}

	/**
	 * Takes variable arguments.... The data is stored in a 1D list. In 2D to find point (nx,ny) we need to convert the
	 * 2D indices to 1D Again in 3D (nx,ny,nz) is converted to 1D.
	 * 
	 * @param n
	 * @return The index in the 1D List containing the data.
	 */
	public synchronized int get1DIndex(int... n) {
		if (n.length > this.dimensions.length)
			throw new IllegalArgumentException("No of index parameters greater than dimensions of data\t" + n.length
					+ "\t" + this.dimensions.length);
		// Throw exception......
		int index = 0;
		for (int i = 0; i < n.length; i++) {
			double var = 1.0;
			for (int j = 0; j < i; j++) {
				var *= dimensions[j];
			}
			index += n[i] * var;
		}

		return index;

	}

	/**
	 * Fixed data size means if we insert an element we remove {@inheritDoc}
	 * 
	 * @see java.util.Vector#insertElementAt(java.lang.Object, int)
	 */
	@Override
	public synchronized void insertElementAt(Double obj, int index) {
		if (this.size() > 0 && (this.size() == this.maxDataSetSize || this.fixedSize == true)) {
			super.remove(0);
		}
		super.insertElementAt(obj, index);
		fireDataChanged();

	}

	/**
	 * Fixed data size means if we insert an element we remove {@inheritDoc}
	 * 
	 * @see java.util.Vector#insertElementAt(java.lang.Object, int)
	 */
	@Override
	// public synchronized boolean add(Double obj)
	// {
	// if (this.size() == this.maxDataSetSize)
	// {
	// super.remove(0);
	// }
	// return super.add(obj);
	// }
	/**
	 * Fixed data size means if we insert an element we remove
	 * 
	 * @param obj
	 * @return
	 * @see java.util.Vector#insertElementAt(java.lang.Object, int)
	 */
	public synchronized boolean add(Double obj) {
		if (this.size() == this.maxDataSetSize && this.fixedSize == true) {
			super.remove(0);
		}
		if (this.fixedSize == false) {
			this.dimensions[0]++;
		}

		super.add(obj);
		fireDataChanged();
		return true;
	}

	/**
	 * Fixed data size means if we insert an element we remove
	 * 
	 * @param obj
	 * @return boolean
	 * @see java.util.Vector#insertElementAt(java.lang.Object, int)
	 */
	public synchronized boolean add(Integer obj) {
		if (this.size() == this.maxDataSetSize && this.fixedSize == true) {
			super.remove(0);
		}
		super.add(obj.doubleValue());
		fireDataChanged();
		return true;

	}

	@Override
	public synchronized void addElement(Double obj) {
		if (this.size() == this.maxDataSetSize && this.fixedSize == true) {
			super.remove(0);
		}
		super.addElement(obj);
		fireDataChanged();
	}

	/**
	 * Get the data at the indices given by n
	 * 
	 * @param n
	 *            variable length parameter so for 1d .... getData(5); (index along dimension 1) for 2d ....
	 *            getData(5,6); (index along dimensions 1 & 2) for 3d .... getData(5,6,1); (index dimensions 1 & 2 & 3)
	 * @return the data at index n
	 */
	public synchronized double getIndex(int... n) {
		return this.get(get1DIndex(n));
	}

	/**
	 * Sets the value of the data at the specified point to value
	 * 
	 * @param value
	 *            The double which the point will be changed to
	 * @param n
	 *            The point in the Vector to change
	 */
	public synchronized void setIndex(double value, int... n) {
		this.set(get1DIndex(n), value);
	}

	/**
	 * Get the data at the indices given by n
	 * 
	 * @param n
	 *            variable length parameter so for 1d .... getData(5); (index along dimension 1) for 2d ....
	 *            getData(5,6); (index along dimensions 1 & 2) for 3d .... getData(5,6,1); (index dimensions 1 & 2 & 3)
	 * @return the data at index n
	 */
	public DataVector getSubset(int... n) {
		int nl = n.length;
		int m = n.length / 2;
		if (nl % 2.0 != 0.0) {
			logger.error(this + " needs an even number of arguments to get a subset of the data");
			throw new IllegalArgumentException("Need even no. of arguments to get a subset of the data");
		}
		if (m != this.dimensions.length) {
			logger.error(this + " need to be passed the corect number of indicies, you passed it " + m
					+ " pairs of bounds, and it needs " + this.dimensions.length + " pairs");
			throw new IllegalArgumentException("No of indices does not match data dimensions you passed it " + m
					+ " pairs of bounds, and it needs " + this.dimensions.length + " pairs");
		}

		int[] startIndices = new int[m];
		int[] endIndices = new int[m];
		for (int i = 0; i < m; i++) {
			startIndices[i] = n[i];
			endIndices[i] = n[i + m];
		}

		int[] difference = new int[m];
		for (int i = 0; i < m; i++) {
			difference[i] = Math.abs(endIndices[i] - startIndices[i]) + 1;
		}
		DataVector result = new DataVector(difference);

		// set up the vectors needed to do this

		int relative[] = new int[m];
		int absolute[] = new int[m];
		int direction[] = new int[m];

		for (int i = 0; i < m; i++) {
			relative[i] = startIndices[i];
			absolute[i] = 0;
			direction[i] = (int) Math.signum(endIndices[i] - startIndices[i]);
		}

		// now preform the loop
		int finished = 0;

		while (finished == 0) {
			// write the value from the relative position of this datavector
			// to the
			// actual position in the final vector.
			result.setIndex(this.getIndex(relative), absolute);

			// now move the count on one position
			relative[0] = relative[0] + direction[0];
			absolute[0]++;

			// round on any that need to be
			int carry = 0;
			for (int j = 0; j < m; j++) {
				relative[j] = relative[j] + carry * direction[j];
				absolute[j] = absolute[j] + carry;
				if (absolute[j] > difference[j] - 1) {
					relative[j] = startIndices[j];
					absolute[j] = 0;
					carry = 1;
				} else {
					carry = 0;
				}
			}

			// if we are carrying something over, then we are at the end of
			// the
			// transfer
			if (carry == 1) {
				finished = 1;
			}

		}

		// int start = Math.min(get1DIndex(startIndices),
		// get1DIndex(endIndices));
		// int end = Math.max(get1DIndex(startIndices), get1DIndex(endIndices));
		// for (int i = start; i < end; i++)
		// {
		// result.set(i - start, this.get(i));
		// }

		return result;
	}

	/*
	 * public PyObject __finditem__(PyObject key) { logger.debug("finditem running"); // System.out.println("finditem"); //
	 * Convert the key into an array of PyObjects. PyObject indices[] = (key instanceof PyTuple) ? ((PyTuple) key).list :
	 * new PyObject[] {key}; // First pass: determine the size of the new dimensions. int nDimensions =
	 * dimensions.length, ellipsisLength = 0, axis = 0; for (int i = 0; i < indices.length; i++) { PyObject index =
	 * indices[i]; if (index instanceof PyEllipsis) { if (ellipsisLength > 0) continue; ellipsisLength =
	 * dimensions.length - (indices.length - i - 1 + axis); for (int j = i + 1; j < indices.length; j++) if (indices[j]
	 * instanceof PyNone) ellipsisLength++; if (ellipsisLength < 0) throw Py.IndexError("too many indices"); axis +=
	 * ellipsisLength; } else if (index instanceof PyNone) nDimensions++; else if (index instanceof PyInteger) {
	 * nDimensions--; axis++; } else if (index instanceof PySlice) axis++; else throw Py.ValueError("invalid index"); }
	 * if (axis > dimensions.length) throw Py.ValueError("invalid index"); // Second pass: now generate the dimensions.
	 * int newAxis = 0, oldAxis = 0; int[] newDimensions = new int[nDimensions]; for (int i = 0; i < indices.length;
	 * i++) { PyObject index = indices[i]; if (index instanceof PyEllipsis) { if (ellipsisLength > 0) { for (int j = 0;
	 * j < ellipsisLength; j++) { newDimensions[newAxis + j] = dimensions[oldAxis + j]; } oldAxis += ellipsisLength;
	 * newAxis += ellipsisLength; ellipsisLength = 0; } } else if (index instanceof PyNone) { newDimensions[newAxis] =
	 * 1; newAxis++; } else if (oldAxis >= dimensions.length) throw Py.IndexError("too many dimensions"); else if (index
	 * instanceof PyInteger) { oldAxis++; } else if (index instanceof PySlice) { PySlice slice = (PySlice) index; int
	 * sliceStep = getStep(slice.step, dimensions[oldAxis]); int sliceStart = getStart(slice.start, sliceStep,
	 * dimensions[oldAxis]); int sliceStop = getStop(slice.stop, sliceStart, sliceStep, dimensions[oldAxis]); if
	 * (sliceStep > 0) newDimensions[newAxis] = 1 + (sliceStop - sliceStart - 1) / sliceStep; else
	 * newDimensions[newAxis] = 1 - (sliceStart - sliceStop - 1) / sliceStep; oldAxis++; newAxis++; } else throw
	 * Py.ValueError("illegal index"); } // Tack any extra indices onto the end. for (int i = 0; i < nDimensions -
	 * newAxis; i++) { newDimensions[newAxis + i] = dimensions[oldAxis + i]; } return Py.java2py(new
	 * DataVector(newDimensions)); } public PyObject __getitem__(PyObject key) { logger.debug("get item runing"); //
	 * System.out.println("get item"); PyObject ret = this.__finditem__(key); if (ret == null) throw
	 * Py.KeyError(key.toString()); return ret; } /* /** Convert negative indices to positive and throw exception if
	 * index out of range.
	 */
	protected int fixIndex(int index, int axis) {
		if (index < 0)
			index += dimensions[axis];
		if (index < 0 || index >= dimensions[axis])
			throw Py.IndexError("index out of range");

		return index;
	}

	/**
	 * pulled out of PySequence
	 * 
	 * @param index
	 * @param defaultValue
	 * @return int
	 */
	private static final int getJythonIndex(PyObject index, int defaultValue) {
		if (index == Py.None || index == null)
			return defaultValue;
		if (!(index instanceof PyInteger))
			throw Py.TypeError("slice index must be int");
		return ((PyInteger) index).getValue();
	}

	/* Should go in PySequence */
	protected static final int getStart(PyObject s_start, int step, int length) {
		int start;
		if (step < 0) {
			start = getJythonIndex(s_start, length - 1);
			if (start < -1)
				start = length + start;
			if (start < -1)
				start = -1;
			if (start > length - 1)
				start = length - 1;
		} else {
			start = getJythonIndex(s_start, 0);
			if (start < 0)
				start = length + start;
			if (start < 0)
				start = 0;
			if (start > length)
				start = length;
		}
		return start;
	}

	protected static final int getStop(PyObject s_stop, int start, int step, int length) {
		int stop;
		if (step < 0) {
			stop = getJythonIndex(s_stop, 1);
			if (stop < -1)
				stop = length + stop;
			if (stop < -1)
				stop = -1;
			if (stop > length - 1)
				stop = length - 1;
		} else {
			stop = getJythonIndex(s_stop, length);
			if (stop < 0)
				stop = length + stop;
			if (stop < 0)
				stop = 0;
			if (stop > length)
				stop = length;
		}
		if ((stop - start) * step < 0)
			stop = start;
		return stop;
	}

	protected static final int getStep(PyObject s_step, int length) {
		int step;
		step = getJythonIndex(s_step, 1);
		if (step > length) {
			step = length - 1;
		}
		return step;
	}

	/**
	 * @return A jama matrix version of the datavector
	 */
	public Matrix getJamaMatrix() {
		try {
			return new Matrix(this.doubleMatrix());
		} catch (IllegalArgumentException e) {
			logger.error("DataVector needs to be passed the a 2 dimentional Vector, you passed it a "
					+ this.dimensions.length + " size Vector");
			throw new IllegalArgumentException(
					"DataVector needs to be passed the a 2 dimentional Vector, you passed it a "
							+ this.dimensions.length + " size Vector");
		}
	}

	/**
	 * @return double[]
	 */
	public synchronized double[] doubleArray() {

		double[] result = new double[this.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = this.get(i);
		}
		return result;
	}

	/**
	 * @return double[][]
	 */
	public synchronized double[][] doubleMatrix() {

		// only return if its a 2D dataVector
		if (this.dimensions.length != 2) {
			logger.error("DataVector needs to be passed the a 2 dimentional Vector, you passed it a "
					+ this.dimensions.length + " size Vector");
			throw new IllegalArgumentException(
					"DataVector needs to be passed the a 2 dimentional Vector, you passed it a "
							+ this.dimensions.length + " size Vector");
		}

		double[][] result = new double[this.dimensions[0]][this.dimensions[1]];
		for (int i = 0; i < this.dimensions[0]; i++) {
			for (int j = 0; j < this.dimensions[1]; j++) {
				result[i][j] = this.getIndex(i, j);
			}
		}

		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataVector test = new DataVector(200);
		DataVector test2 = test.getSubset(1, 3);
		logger.debug("test\t" + test2.size());
		// System.out.println("test\t" + test2.size());
	}

	// Additions

	/**
	 * Overriding the '+' opporator, so that datavectors can be added together <br>
	 * using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[1,2,3,4])<br>
	 *  print b<br>
	 *  <b>c = a+b</b><br>
	 *  print c<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[2.0, 4.0, 6.0, 8.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Datavector after the '+' sign
	 * @return DataVector1D containg the element by element summation of the this object with the object passed
	 */
	public Object __add__(DataVector other) {
		return DataVectorMath.sum(this, other);
	}

	/**
	 * Overriding the '+' opporator, so that all elements of a datavector can be <br>
	 * added to a single double value <br>
	 * using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>b = a+2.0</b><br>
	 *  print b<br><br>
	 *  </code> this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[3.0, 4.0, 5.0, 6.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Double after the '+' sign
	 * @return DataVector1D containg the element by element summation of the this object with the double passed
	 */
	public Object __add__(double other) {

		return DataVectorMath.sum(this, other);

	}

	/**
	 * Overriding the '+=' opporator, so that datavectors can be added together <br>
	 * using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[1,2,3,4])<br>
	 *  print b<br>
	 *  <b>a+=b</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[2.0, 4.0, 6.0, 8.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Datavector1D after the '+=' sign
	 * @return DataVector1D containg the element by element summation of the this object with the object passed
	 */
	public Object __iadd__(DataVector other) {

		return DataVectorMath.sum(this, other);

	}

	/**
	 * Overriding the '+=' opporator, so that a double value can be added to all<br>
	 * elements of the original datavector, using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>a+=5.0</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[6.0, 7.0, 8.0, 9.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Double value after the '+=' sign
	 * @return DataVector1D containg the element by element summation of the this object with the double passed
	 */
	public Object __iadd__(double other) {

		return DataVectorMath.sum(this, other);

	}

	// Subtractions

	/**
	 * Overriding the '-' opporator, so that datavectors can be subtracted <br>
	 * using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[2,3,4,5])<br>
	 *  print b<br>
	 *  <b>c = a-b</b><br>
	 *  print c<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [2.0, 3.0, 4.0, 5.0]<br>
	 *  <b>[-1.0, -1.0, -1.0, -1.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Datavector1D after the '-' sign
	 * @return DataVector1D containg the element by element subtraction of the this object with the object passed
	 */
	public Object __sub__(DataVector other) {

		return DataVectorMath.sub(this, other);

	}

	/**
	 * Overriding the '-' opporator, so that all elements of a datavector can have <br>
	 * a single double value subtracted from them using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>b = a-1.0</b><br>
	 *  print b<br><br>
	 *  </code> this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[0.0, 1.0, 2.0, 3.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Double after the '-' sign
	 * @return DataVector1D containg the element by element subtraction of the this object with the double passed
	 */
	public Object __sub__(double other) {

		return DataVectorMath.sub(this, other);
	}

	/**
	 * Overriding the '-=' opporator, so that datavectors can be subtracted<br>
	 * using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[1,1,2,2])<br>
	 *  print b<br>
	 *  <b>a-=b</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [1.0, 1.0, 2.0, 2.0]<br>
	 *  <b>[0.0, 1.0, 1.0, 2.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Datavector1D after the '-=' sign
	 * @return DataVector1D containg the element by element subtraction of the this object with the object passed
	 */
	public Object __isub__(DataVector other) {

		return DataVectorMath.sub(this, other);

	}

	/**
	 * Overriding the '-=' opporator, so that a double value can be subtracted from all<br>
	 * elements of the original datavector, using the following syntax<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>a-=2.0</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[-1.0, 0.0, 1.0, 2.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The Double value after the '-=' sign
	 * @return DataVector1D containg the element by element subtraction of the this object with the double passed
	 */
	public Object __isub__(double other) {

		return DataVectorMath.sub(this, other);
	}

	// Division

	/**
	 * Overriding the '/' opporator, so that one vector can be divided element by <br>
	 * element by annother<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[1,2,3,4])<br>
	 *  print b<br>
	 *  <b>c=a/b</b><br>
	 *  print c<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[1.0, 1.0, 1.0, 1.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '/' sign
	 * @return DataVector1D containg the element by element division of the this object with the object passed
	 */
	public Object __div__(DataVector other) {

		return DataVectorMath.div(this, other);
	}

	/**
	 * Overriding the '/' opporator, so that one vector can be divided element by <br>
	 * element by a Double<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>b=a/2.0</b><br>
	 *  print b<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[0.5, 1.0, 1.5, 2.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The double value after the '/' sign
	 * @return DataVector1D containg the element by element division of the this object with the Double passed
	 */
	public Object __div__(double other) {
		return DataVectorMath.div(this, other);
	}

	/**
	 * Overriding the '/=' opporator, so that one vector can be divided element by <br>
	 * element by annother<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[1,2,3,4])<br>
	 *  print b<br>
	 *  <b>c
	 *  a/=b</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[1.0, 1.0, 1.0, 1.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '/=' sign
	 * @return DataVector1D containg the element by element division of the this object with the object passed
	 */
	public Object __idiv__(DataVector other) {
		return DataVectorMath.div(this, other);
	}

	/**
	 * Overriding the '/=' opporator, so that one vector can be divided element by <br>
	 * element by a Double<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>a/=2.0</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[0.5, 1.0, 1.5, 2.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The double value after the '/=' sign
	 * @return DataVector1D containg the element by element division of the this object with the Double passed
	 */
	public Object __idiv__(double other) {
		return DataVectorMath.div(this, other);
	}

	// multiplication (element by element)

	/**
	 * Overriding the '*' opporator, so that one vector can be multiplied element by <br>
	 * element by annother<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[1,2,3,4])<br>
	 *  print b<br>
	 *  <b>c=a*b</b><br>
	 *  print c<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[1.0, 4.0, 9.0, 16.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '*' sign
	 * @return DataVector1D containg the element by element multiplication of the this object with the object passed
	 */
	public Object __mul__(DataVector other) {

		return DataVectorMath.mul(this, other);
	}

	/**
	 * Overriding the '*' opporator, so that one vector can be multiplied element by <br>
	 * element by a Double<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>b=a*2.0</b><br>
	 *  print b<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[2.0, 4.0, 6.0, 8.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The double value after the '*' sign
	 * @return DataVector1D containg the element by element multiplication of the this object with the Double passed
	 */
	public Object __mul__(double other) {
		return DataVectorMath.mul(this, other);
	}

	/**
	 * Overriding the '*=' opporator, so that one vector can be multiplied element by <br>
	 * element by annother<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  b = DataVector(1,4,[1,2,3,4])<br>
	 *  print b<br>
	 *  <b>c
	 *  a*=b</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[1.0, 4.0, 9.0, 16.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '*=' sign
	 * @return DataVector1D containg the element by element multiplication of the this object with the object passed
	 */
	public Object __imul__(DataVector other) {

		return DataVectorMath.mul(this, other);

	}

	/**
	 * Overriding the '/*=' opporator, so that one vector can be multiplied element by <br>
	 * element by a Double<br>
	 * <br>
	 * <code>
	 *  a = DataVector(1,4,[1,2,3,4])<br>
	 *  print a<br>
	 *  <b>a*=2.0</b><br>
	 *  print a<br>
	 *  </code><br>
	 * this gives the responce<br>
	 * <br>
	 * <code>
	 *  [1.0, 2.0, 3.0, 4.0]<br>
	 *  <b>[2.0, 4.0, 6.0, 8.0]</b><br>
	 *  </code><br>
	 * 
	 * @param other
	 *            The double value after the '*=' sign
	 * @return DataVector1D containg the element by element multiplication of the this object with the Double passed
	 */
	public Object __imul__(double other) {

		return DataVectorMath.mul(this, other);
	}

	// Logical Opporators

	/**
	 * overriding the "==" opporator.<br>
	 * <br>
	 * This returns true if the magnitudes of the 2 vectors are identical <br>
	 * <br>
	 * <i><b>Warning!! as this is using floating point accurasy, two processed <br>
	 * vecors are very unlikely to be equal, so be carefull using this!</b></i><br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '==' sign
	 * @return PyInteger, containing 1 for true, and 0 for false.
	 */
	public Object __eq__(DataVector other) {

		double V1 = 0.0;
		double V2 = 0.0;

		for (int i = 0; i < this.size(); i++) {
			V1 = V1 + this.getIndex(i) * this.getIndex(i);
			V2 = V2 + other.getIndex(i) * other.getIndex(i);

		}

		if (V1 == V2) {
			PyInteger output = new PyInteger(1);
			return output;
		}
		PyInteger out2 = new PyInteger(0);
		return out2;
	}

	/**
	 * overriding the "!=" opporator.<br>
	 * <br>
	 * This returns false if the magnitudes of the 2 vectors are identical <br>
	 * <br>
	 * <i><b>Warning!! as this is using floating point accurasy, two processed <br>
	 * vecors are very unlikely to be equal, so be carefull using this!</b></i><br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '!=' sign
	 * @return PyInteger, containing 1 for true, and 0 for false.
	 */
	public Object __ne__(DataVector other) {

		double V1 = 0.0;
		double V2 = 0.0;

		for (int i = 0; i < this.size(); i++) {
			V1 = V1 + this.getIndex(i) * this.getIndex(i);
			V2 = V2 + other.getIndex(i) * other.getIndex(i);

		}

		if (V1 != V2) {
			PyInteger output = new PyInteger(1);
			return output;
		}
		PyInteger out2 = new PyInteger(0);
		return out2;
	}

	/**
	 * overriding the ">" opporator.<br>
	 * <br>
	 * This returns true if the first vecotors is greater than the magnitude of the second vector<br>
	 * <br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '>' sign
	 * @return PyInteger, containing 1 for true, and 0 for false.
	 */
	public Object __gt__(DataVector other) {

		double V1 = 0.0;
		double V2 = 0.0;

		for (int i = 0; i < this.size(); i++) {
			V1 = V1 + this.getIndex(i) * this.getIndex(i);
			V2 = V2 + other.getIndex(i) * other.getIndex(i);

		}

		if (V1 > V2) {
			PyInteger output = new PyInteger(1);
			return output;
		}
		PyInteger out2 = new PyInteger(0);
		return out2;
	}

	/**
	 * overriding the "<" opporator.<br>
	 * <br>
	 * This returns false if the first vecotors is greater than the magnitude of the second vector<br>
	 * <br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '<' sign
	 * @return PyInteger, containing 1 for true, and 0 for false.
	 */
	public Object __lt__(DataVector other) {

		double V1 = 0.0;
		double V2 = 0.0;

		for (int i = 0; i < this.size(); i++) {
			V1 = V1 + this.getIndex(i) * this.getIndex(i);
			V2 = V2 + other.getIndex(i) * other.getIndex(i);

		}

		if (V1 < V2) {
			PyInteger output = new PyInteger(1);
			return output;
		}
		PyInteger out2 = new PyInteger(0);
		return out2;
	}

	/**
	 * overriding the ">=" opporator.<br>
	 * <br>
	 * This returns true if the first vecotor is greater than or equal to the magnitude of the second vector<br>
	 * <br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '>=' sign
	 * @return PyInteger, containing 1 for true, and 0 for false.
	 */
	public Object __ge__(DataVector other) {

		double V1 = 0.0;
		double V2 = 0.0;

		for (int i = 0; i < this.size(); i++) {
			V1 = V1 + this.getIndex(i) * this.getIndex(i);
			V2 = V2 + other.getIndex(i) * other.getIndex(i);

		}

		if (V1 >= V2) {
			PyInteger output = new PyInteger(1);
			return output;
		}
		PyInteger out2 = new PyInteger(0);
		return out2;
	}

	/**
	 * overriding the "<=" opporator.<br>
	 * <br>
	 * This returns true if the first vecotors is less than or equal to the magnitude of the second vector<br>
	 * <br>
	 * 
	 * @param other
	 *            The DataVector1D value after the '<=' sign
	 * @return PyInteger, containing 1 for true, and 0 for false.
	 */
	public Object __le__(DataVector other) {

		double V1 = 0.0;
		double V2 = 0.0;

		for (int i = 0; i < this.size(); i++) {
			V1 = V1 + this.getIndex(i) * this.getIndex(i);
			V2 = V2 + other.getIndex(i) * other.getIndex(i);

		}

		if (V1 <= V2) {
			PyInteger output = new PyInteger(1);
			return output;
		}
		PyInteger out2 = new PyInteger(0);
		return out2;
	}

	@Override
	public synchronized String toString() {

		String Out = "";

		int Dimentions[] = this.getDimensions();

		Out = Out + "DataVector Dimentions are [" + Dimentions[0];

		for (int i = 1; i < Dimentions.length; i++) {
			Out = Out + "," + Dimentions[i];
		}

		Out = Out + "]\n";

		int outputDimentions[] = new int[Dimentions.length];

		// if the datavectoer is too big, clip the size of the data so its
		// possible to display it on the screen easily

		int clipped = 0;

		for (int i = 0; i < Dimentions.length; i++) {
			if (Dimentions[i] > 6) {
				clipped = 1;
				outputDimentions[i] = 6;
			} else {
				outputDimentions[i] = Dimentions[i];
			}
		}

		if (clipped == 1) {
			Out = Out + "DataVector Output clipped in size\n";
		}

		// First get the dimentionality of the vector
		if (outputDimentions.length == 1) {

			Out += "[" + this.getIndex(0);

			for (int i = 1; i < outputDimentions[0]; i++) {
				Out += "," + this.getIndex(i);
			}

			Out += "]\n";

		}

		if (outputDimentions.length == 2) {

			for (int h = 0; h < outputDimentions[1]; h++) {

				Out += "|\t" + this.getIndex(0, h);

				for (int w = 1; w < outputDimentions[0]; w++) {

					Out += ",\t" + this.getIndex(w, h);
				}

				Out += " |\n";
			}

		}

		if (outputDimentions.length == 3) {

			for (int w = 0; w < outputDimentions[0]; w++) {

				Out += "\t---";
			}
			Out += " \n";

			for (int d = 0; d < outputDimentions[2]; d++) {

				for (int h = 0; h < outputDimentions[1]; h++) {

					Out += "|\t" + this.getIndex(0, h, d);

					for (int w = 1; w < outputDimentions[0]; w++) {

						Out += ",\t" + this.getIndex(w, h, d);
					}

					Out += " |\n";
				}

				for (int w = 0; w < outputDimentions[0]; w++) {

					Out += "\t---";
				}
				Out += " \n";

			}

		}

		if (outputDimentions.length > 3) {

			Out += "<" + outputDimentions[0];

			for (int i = 1; i < outputDimentions.length; i++) {
				Out += outputDimentions[i] + ",";
			}

			Out += ">\n";

			Out += "[" + this.get(0);

			for (int i = 1; i < this.size(); i++) {
				Out += "," + this.get(i);
			}

			Out += "]";

		}

		return Out;

	}

}
