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

package gda.scan;

import gda.device.scannable.ScannableUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.python.core.PyNone;
import org.python.core.PySequence;

/**
 *
 */
public class ScanPositionProviderFactory {
	/**
	 * @param pointsList
	 * @return ScanPositionProvider
	 */
	@SuppressWarnings("rawtypes")
	public static ScanPositionProvider create(List pointsList){
		return new ScanPositionProviderFromList(pointsList);
	}

	/**
	 * @param <T>  type of positions
	 * @param points
	 * @return ScanPositionProvider
	 * e.g. ScanPositionProviderFactory.create(new Double[]{0., 1., 2., 3.,4.,5.});
	 */
	public static  <T> ScanPositionProvider create( T [] points){
		return new ScanPositionProviderFromArray<T>(points);
	}

	/**
	 * @param regionsList
	 * @return ScanPositionProvider
	 */
	@SuppressWarnings("rawtypes")
	public static ScanPositionProvider createFromRegion(List regionsList){
		return new ScanPositionProviderFromRegionalList(regionsList);
	}


}

class ScanPositionProviderFromList implements ScanPositionProvider{
	@SuppressWarnings("rawtypes")
	List points;
	@SuppressWarnings("rawtypes")
	ScanPositionProviderFromList(List points){
		this.points = points;
	}
	@Override
	public Object get(int index) {
		return points.get(index);
	}
	@Override
	public int size() {
		return points.size();
	}
}

class ScanPositionProviderFromArray<T> implements ScanPositionProvider{
	T [] points;
	ScanPositionProviderFromArray(T [] points){
		this.points = points;
	}
	@Override
	public Object get(int index) {
		return points[index];
	}
	@Override
	public int size() {
		return points.length;
	}
}


class ScanPositionProviderFromRegionalList implements ScanPositionProvider {

	@SuppressWarnings("rawtypes")
	List points=new ArrayList();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	ScanPositionProviderFromRegionalList(List regionList){
		Iterator it = regionList.iterator();
		while(it.hasNext()){
			List l = (List)it.next();
			Object start = l.get(0);
			Object stop = l.get(1);
			Object step = l.get(2);
			ScanRegion nextRegion= new ScanRegion(start,stop,step);
			
			Vector<Object> vol=nextRegion.getPoints();
			points.addAll(vol);
		}
		
	}
	
	@Override
	public Object get(int index) {
		return points.get(index);
	}
	
	@Override
	public int size() {
		return points.size();
	}
}

class ScanRegion{
	private Object start;
	private Object stop;
	private Object step;

	private Vector<Object> points = new Vector<Object>();
	private int numberOfPoints = 0;

	ScanRegion(Object start, Object stop, Object step){
		this.start = start;
		this.stop = stop;
		this.step = step;
		// ensure step is in the right direction
		this.step = ScanBase.sortArguments(this.start, this.stop, this.step);
		
		//to calculate the number of points:
		if (stop == null || step == null) {
			numberOfPoints = 0;
		}
		
		try {
			int len = getLength(start);
			if(len != getLength(stop) || len != getLength(step)){
				numberOfPoints = 0;
			}
			
			numberOfPoints = this.getNumberSteps(len);
			numberOfPoints += 1;
			
		} catch (Exception e) {
			numberOfPoints = 0;
		}
		
		//To fill the scan points
		calculateScanPoints();		
	}

	public Vector<Object> getPoints(){
		return this.points;
	}
	/**
	 * Assuming the objects can be converted into doubles, this calculates the number of steps for the given InputLength
	 * 
	 * @return int
	 * @throws Exception
	 */
	public int getNumberSteps(int parameterSize) throws Exception {
		// the expected size of the start, stop and step objects
//		int parameterSize = theScannable.getInputNames().length;
		int numArgs = parameterSize;

		// add a small amount to values to ensure that the final point in the scan is included
		double fudgeFactor = 1e-10;

		// if there is a mismatch to the position object and the Scannable, throw an error
		if (numArgs == 1 && (start.getClass().isArray() || start instanceof PySequence)) {
			throw new Exception("Position arguments do not match size of Pseudo Device. Check size of inputNames and outputFormat arrays for this object.");
		}

		// if position objects are a single value, or if no inputNames
		if (numArgs <= 1) {

			int maxSteps = 0;
			double startValue = Double.valueOf(start.toString()).doubleValue();
			double stopValue = Double.valueOf(stop.toString()).doubleValue();
			double stepValue = Math.abs(Double.valueOf(step.toString()).doubleValue());
			if (stepValue == 0) {
				throw new Exception("step size is zero so number of points cannot be calculated");
			}
			double fudgeValue = stepValue * fudgeFactor;
			double difference = Math.abs(stopValue - startValue);
			maxSteps = (int) Math.abs((difference + Math.abs(fudgeValue)) / stepValue);
			return maxSteps;
		}

		// ELSE position objects are an array
		int maxSteps = 0;
		int minSteps = java.lang.Integer.MAX_VALUE;
		// Loop through each field
		for (int i = 0; i < numArgs; i++) {
			Double startValue = getDouble(start, i);
			Double stopValue = getDouble(stop, i);
			Double stepValue = getDouble(step, i);
			if (stepValue == null) {
				if (startValue == null && stopValue == null) {
					// stepSize is null, but this is okay with no start/stop values
					continue;
				}
				throw new Exception(
						"a step field is None/null despite there being a corresponding start and/or stop value.");
			}

			if (startValue == null || stopValue == null) {
				throw new Exception("a start or end field is None/null without a corresponding None/null step size.");
			}

			double difference = Math.abs(stopValue - startValue);
			if (stepValue == 0.) {
				if (difference < fudgeFactor) {
					// zero step value okay as there is no distance to move
					continue;
				}
				throw new Exception("a step field is zero despite there being a distance to move in that direction.");
			}

			double fudgeValue = stepValue * fudgeFactor;
			int steps = (int) Math.abs((difference + Math.abs(fudgeValue)) / stepValue);
			if (steps > maxSteps) {
				maxSteps = steps;
			}
			if (steps < minSteps) {
				minSteps = steps;
			}
		}

		if (maxSteps - minSteps > 1) {
			throw new Exception("The step-vector does not connect the start and end points within the allowed\n"
					+ "tolerance of one step: in one basis direction " + maxSteps + " steps are required, but\n"
					+ "in another only " + minSteps + " steps are required.");
		}

		return minSteps;

	}

	@SuppressWarnings("rawtypes")
	private Double getDouble(Object val, int index) {
		if (val instanceof Number[]) {
			return ((Number[]) val)[index].doubleValue();
		}
		if (val.getClass().isArray()) {
			return Array.getDouble(val, index);
		}
		if (val instanceof PySequence) {
			if (((PySequence) val).__finditem__(index) instanceof PyNone) {
				return null;
			}
			return Double.parseDouble(((PySequence) val).__finditem__(index).toString());
		}
		if (val instanceof List) {
			return Double.parseDouble(((List) val).get(index).toString());
		}
		throw new IllegalArgumentException("getDouble. Object cannot be converted to Double");
	}
	
	@SuppressWarnings("rawtypes")
	private int getLength(Object val) {
		int len=0;
		if (val instanceof Number[]) {
			len = ((Number[]) val).length;
		}
		if (val.getClass().isArray()) {
			len= Array.getLength(val);
		}
		if (val instanceof PySequence) {
			len=((PySequence)val).__len__();
		}
		if (val instanceof List) {
			len=((List)val).size();
		}
		return len;
	}
	
	/**
	 * Fill the array of points from this region setting
	 */
	public void calculateScanPoints() {
		points.add(start);
		if (numberOfPoints != 0 && step != null) {
			// loop through all points and create vector of points
			Object previousPoint = start;
			for (int i = 1; i < numberOfPoints; i++) {
				Object nextPoint = ScannableUtils.calculateNextPoint(previousPoint, step);
				points.add(nextPoint);
				previousPoint = nextPoint;
			}
		}
	}

}

