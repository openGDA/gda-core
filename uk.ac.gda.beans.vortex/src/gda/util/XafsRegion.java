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

package gda.util;

import java.text.NumberFormat;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single region of a scan.
 */

public class XafsRegion {
	private static final Logger logger = LoggerFactory.getLogger(XafsRegion.class);

	private String name;

	private double start;

	private double end;

	private double increment;

	private double time;

	private int steps;

	private String displayUnits = null;

	private String internalUnits = null;

	private boolean lastRegion = true;

	private int internalUnitsDecimalPlaces = 1;

	// integers which can be used to set and get the various values
	/** */
	public static final int NAME = 0;

	/** */
	public static final int START = 1;

	/** */
	public static final int END = 2;

	/** */
	public static final int INCREMENT = 3;

	/** */
	public static final int TIME = 4;

	/** */
	public static final int STEPS = 5;

	/**
	 * Constructor to create a XAFS Experiment Region from a String produced by the toString() method.
	 * 
	 * @param string
	 */
	public XafsRegion(String string) {
		StringTokenizer strtok = new StringTokenizer(string, " ");
		name = strtok.nextToken();
		// This loop is needed because the name is allowed to contain spaces
		// (e.g.
		// "Pre Edge").
		while (strtok.countTokens() > 4) {
			name = name + " " + strtok.nextToken();
		}
		start = Double.valueOf(strtok.nextToken());
		increment = Double.valueOf(strtok.nextToken());
		end = Double.valueOf(strtok.nextToken());
		time = Double.valueOf(strtok.nextToken());
		calculateNumberOfSteps();
		displayUnits = Converter.KEV;
		internalUnits = Converter.MDEG;
	}

	/**
	 * Constructor to create a XAFS Experiment Region for Angle Scan
	 * 
	 * @param name
	 *            region name
	 * @param start
	 *            start value
	 * @param end
	 *            end value
	 * @param increment
	 *            increment value
	 * @param time
	 *            the time spent for each increment
	 * @param steps
	 *            the experiment point number
	 * @param displayUnits
	 *            the region's displayUnits
	 */
	public XafsRegion(String name, double start, double end, double increment, double time, int steps,
			String displayUnits) {
		this(name, start, end, increment, time, steps, Converter.MDEG, displayUnits);
	}

	/**
	 * Constructor to create a XAFS Experiment Region for Angle Scan
	 * 
	 * @param name
	 *            region name
	 * @param start
	 *            start value
	 * @param end
	 *            end value
	 * @param increment
	 *            increment value
	 * @param time
	 *            the time spent for each increment
	 * @param steps
	 *            the experiment point number
	 * @param internalUnits
	 *            the region's internalUnits
	 * @param displayUnits
	 *            the region's displayUnits
	 */
	public XafsRegion(String name, double start, double end, @SuppressWarnings("unused") double increment, double time,
			int steps, String internalUnits, String displayUnits) {

		this.internalUnits = internalUnits;
		this.displayUnits = displayUnits;
		this.name = name;
		this.start = convertFromDisplayUnits(start);
		this.end = convertFromDisplayUnits(end);
		this.steps = steps;
		this.time = time;
		calculateIncrement();
	}

	/**
	 * Converts from display units to internal units
	 * 
	 * @param value
	 *            the value too convert
	 * @return the converted value
	 */
	private double convertFromDisplayUnits(double value) {
		return (Formatter.getFormattedDouble(Converter.convert(value, displayUnits, internalUnits), internalUnits));
	}

	/**
	 * Converts from internal units to display units
	 * 
	 * @param value
	 *            the value too convert
	 * @return the converted value
	 */
	private double convertToDisplayUnits(double value) {
		return (Formatter.getFormattedDouble(Converter.convert(value, internalUnits, displayUnits), displayUnits));
	}

	/**
	 * Sets a value.
	 * 
	 * @param value
	 *            new values (as a String)
	 * @param valueNumber
	 *            which value to set
	 */
	public void setValue(Object value, int valueNumber) {

		switch (valueNumber) {
		case START:
			setStart(((Double) value).doubleValue());
			break;
		case END:
			setEnd(((Double) value).doubleValue());
			break;
		case INCREMENT:
			setIncrement(((Double) value).doubleValue());
			break;
		case TIME:
			setTime(((Double) value).doubleValue());
			break;
		case STEPS:
			setNumberOfSteps(((Integer) value).intValue());
			break;
		default:
			logger.error("Error in XafsRegion setValue");
			break;
		}
	}

	/**
	 * Gets a value
	 * 
	 * @param valueNumber
	 *            which value to get
	 * @return either a Double or an Integer
	 */
	public Object getValue(int valueNumber) {
		Object value = null;

		switch (valueNumber) {
		case NAME:
			value = name;
			break;
		case START:
			value = new Double(getStart());
			break;
		case END:
			value = new Double(getEnd());
			break;
		case INCREMENT:
			value = new Double(getIncrement());
			break;
		case TIME:
			value = new Double(getTime());
			break;
		case STEPS:
			value = new Integer(getNumberOfSteps());
			break;
		default:
			logger.error("Error in XafsRegion setValue");
			break;
		}

		return value;
	}

	/**
	 * Sets the start value
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setStart(double newValue) {
		logger.debug("setStart converting " + newValue + " " + displayUnits);
		start = convertFromDisplayUnits(newValue);
		logger.debug("to " + start + " " + internalUnits);
		calculateNumberOfSteps();
	}

	/**
	 * Gets the start value
	 * 
	 * @return start value
	 */
	public double getStart() {
		double rtrn = convertToDisplayUnits(start);
		logger.debug("getStart converting " + start + " " + internalUnits);
		logger.debug(" to " + rtrn + " " + displayUnits);
		return rtrn;
	}

	/**
	 * Sets the end value
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setEnd(double newValue) {
		end = convertFromDisplayUnits(newValue);
		logger.debug("setEnd newValue is " + newValue);
		logger.debug("setEnd end is " + end);
		calculateNumberOfSteps();
	}

	/**
	 * Gets the end value
	 * 
	 * @return end value
	 */
	public double getEnd() {
		return (convertToDisplayUnits(end));
	}

	/**
	 * Sets the increment
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setIncrement(double newValue) {
		increment = convertFromDisplayUnits(newValue);
		calculateNumberOfSteps();
	}

	/**
	 * Gets the increment
	 * 
	 * @return increment
	 */
	public double getIncrement() {
		double rtrn;

		if (displayUnits.equals(internalUnits)) {
			rtrn = Formatter.getFormattedDouble(increment, internalUnits);
		} else {
			rtrn = (convertToDisplayUnits(end) - convertToDisplayUnits(start)) / steps;
			rtrn = Formatter.getFormattedDouble(rtrn, displayUnits);
		}
		return (rtrn);
	}

	/**
	 * Sets the name
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setName(String newValue) {
		name = newValue;
	}

	/**
	 * Sets the time
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setTime(double newValue) {
		time = newValue;
	}

	/**
	 * Gets the time value
	 * 
	 * @return time value
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Gets the time value
	 * 
	 * @return time value
	 */
	public double getTotalTime() {
		if (lastRegion) {
			return (time * (steps + 1));
		}
		return (time * steps);
	}

	/**
	 * Sets the number of steps
	 * 
	 * @param newValue
	 *            the new value
	 */
	public void setNumberOfSteps(int newValue) {
		steps = newValue < 0 ? -newValue : newValue;
		if (steps == 0)
			steps = 1;
		calculateIncrement();
	}

	/**
	 * Gets the number of steps
	 * 
	 * @return the number of steps
	 */
	public int getNumberOfSteps() {
		return steps;
	}

	/**
	 * Gets the region displayUnits
	 * 
	 * @return region displayUnits
	 */
	public String getDisplayUnits() {
		return (displayUnits);
	}

	/**
	 * sets the region displayUnits
	 * 
	 * @param newValue
	 *            new displayUnitss
	 */
	public void setDisplayUnits(String newValue) {
		logger.debug("XafsRegion setDisplayUnits called with" + newValue);
		displayUnits = newValue;
	}

	/**
	 * Calculates the number of steps
	 */
	private void calculateNumberOfSteps() {
		if (increment != 0.0) {
			logger.debug(" changing steps from " + steps);
			steps = (int) ((end - start) / increment);
			// If steps comes out negative change signs to make steps
			// positive and
			// increment negative (fix for bug #184).
			if (steps < 0) {
				steps = -steps;
				increment = -increment;
			}
			logger.debug(" to " + steps);
		}
	}

	/**
	 * Calculates the increment
	 */
	private void calculateIncrement() {
		// FIXME: this should calculate the increment in internalUnits,
		// change it to the nearest value allowed and then also alter
		// either start of end to fit.
		if (steps != 0) {
			logger.debug(" changing increment from " + increment);
			increment = ((end - start) / steps);
			double roundedIncrement = increment * Math.pow(10.0, internalUnitsDecimalPlaces);
			roundedIncrement = Math.rint(roundedIncrement);
			roundedIncrement /= 10.0;
			double roundedStart = end - steps * roundedIncrement;
			start = roundedStart;
			logger.debug(" to " + increment);
		}
	}

	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(1);
		String string = "";
		string = name + " " + Formatter.getFormattedString(start, internalUnits);
		string += " " + Formatter.getFormattedString(increment, internalUnits);
		string += " " + Formatter.getFormattedString(end, internalUnits);
		string += " " + time;
		return string;
	}

	/**
	 * @param newValue
	 */
	public void setLastRegion(boolean newValue) {
		lastRegion = newValue;
	}

	/**
	 * @return the number of points in the region
	 */
	public int getNumberOfPoints() {
		int numberOfPoints = steps;

		if (lastRegion) {
			numberOfPoints += 1;
		}

		return numberOfPoints;
	}
}
