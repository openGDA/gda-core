/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.example.scannable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.CountableScannable;
import org.eclipse.scanning.api.ITerminatable;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockScannable extends CountableScannable<Number> implements ITerminatable {

	protected Number position = 0d;
	private boolean requireSleep = true;

	private boolean realisticMove = false;
	/**
	 * 1 unit/s or 0.1 in 100ms
	 */
	private double moveRate = 1; // 1 unit/s or 0.1 in 100ms

	private List<Number> values;
	private List<AbstractPosition> positions;

	public MockScannable() {
		super();
		values = new ArrayList<>();
		positions = new ArrayList<>();
	}

	public MockScannable(Number position) {
		this();
		this.position = position;
	}

	public MockScannable(String name, Number position) {
		this();
		setName(name);
		this.position = position;
	}

	public MockScannable(String name, Number position, int level) {
		this();
		setLevel(level);
		setName(name);
		this.position = position;
	}

	public MockScannable(String name, Number position, int level, boolean requireSleep) {
		this();
		this.requireSleep = requireSleep;
		setLevel(level);
		setName(name);
		this.position = position;
	}

	public MockScannable(String name, Number position, int level, String unit) {
		this();
		setLevel(level);
		setName(name);
		this.position = position;
		this.unit = unit;
		this.requireSleep = false;
	}

	private Number upper = 1000;
	private Number lower = -1000;

	@Override
	public Number getPosition() {
		return position;
	}

	@Override
	public Number setPosition(Number position) throws ScanningException {
		return setPosition(position, null);
	}

	public void setInitialPosition(Number position) {
		this.position = position;
	}

	@Override
	public Number setPosition(Number value, IPosition loc) throws ScanningException {

		int index = loc != null ? loc.getIndex(getName()) : -1;
		double val = value != null ? value.doubleValue() : Double.NaN;
		boolean ok = delegate.firePositionWillPerform(new Scalar<>(getName(), index, val));
		if (!ok)
			return this.position;

		if (value != null && position != null) {
			long waitTime = Math.abs(Math.round((val - this.position.doubleValue())) * 100);
			waitTime = Math.max(waitTime, 1);

			if (isRealisticMove()) {
				value = doRealisticMove(value, index, waitTime); // Might get cancelled while moving.
			} else if (isRequireSleep()) {
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new ScanningException("move of scannble " + getName() + " was interrupted", e);
				}
			}
		}
		this.position = value;

		values.add(value);
		positions.add((AbstractPosition) loc);

		delegate.firePositionPerformed(-1, new Scalar<>(getName(), index, val));
		return this.position;
	}

	private TerminationPreference terminate;
	private CountDownLatch latch;

	protected Number doRealisticMove(Number pos, int index, long minimumWaitTime) throws ScanningException {

		if (pos == null)
			return pos;

		try {
			latch = new CountDownLatch(1);
			terminate = null;

			Number orig = this.position;
			if (orig == null)
				orig = 0d;

			double distance = pos.doubleValue() - orig.doubleValue();
			long waitedTime = 0L;
			if (Math.abs(distance) > 0.000001) {
				double rate = getMoveRate(); // units/s
				double time = distance / rate; // Time, s, to do the move.

				// We will pretend there are 10 points in any move for notification
				double increment = distance / 10d;
				long pauseTime = Math.abs(Math.round(time / 10 * 1000)); // pause in ms

				// TODO Helpful for debugging tests System.err.println("Distance="+distance+" Time="+time+"
				// Increment="+increment);

				double currentPosition = orig.doubleValue();
				for (int i = 0; i < 10; i++) {
					if (terminate == TerminationPreference.PANIC)
						return this.position;
					// TODO Helpful for debugging tests System.err.println("Pausing for "+pauseTime+" ("+i+"/10)");
					Thread.sleep(pauseTime);
					waitedTime += pauseTime;
					currentPosition += increment;
					this.position = currentPosition;
					delegate.firePositionChanged(getLevel(), new Scalar<>(getName(), index, currentPosition));
					if (terminate == TerminationPreference.CONTROLLED)
						break;
				}
				// System.out.println("Realistic move of "+getName()+" from "+orig+" to "+currentPosition+" complete");
			}
			if (isRequireSleep() && minimumWaitTime > 0 && minimumWaitTime > waitedTime
					&& terminate != TerminationPreference.PANIC) {
				Thread.sleep(minimumWaitTime - waitedTime);
			}
			return this.position;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ScanningException("move of scannble " + getName() + " was interrupted", e);
		} finally {
			terminate = null;
			if (latch != null)
				latch.countDown();
			latch = null;
		}

	}

	@Override
	public void terminate(TerminationPreference pref) throws Exception {
		System.out.println("Terminate requested, preference " + pref);
		terminate = pref;
		if (latch != null)
			latch.await();
	}

	@Override
	public String toString() {
		return "MockScannable [level=" + getLevel() + ", name=" + getName() + ", position=" + position + "]";
	}

	public boolean isRequireSleep() {
		return requireSleep;
	}

	public void setRequireSleep(boolean requireSleep) {
		this.requireSleep = requireSleep;
	}

	public void verify(Number value, IPosition point) throws Exception {

		for (int i = 0; i < positions.size(); i++) {

			boolean equalPos = positions.get(i).equals(point, false);
			Number other = values.get(i);
			boolean identical = other == value;
			boolean equals = equalsWithinTolerance(other, value, 0.0000000001);
			if (equalPos && (identical || equals)) {
				return;
			}
		}

		throw new Exception("No call to setPosition had value=" + value + " and position=" + point);
	}

	public List<Number> getValues() {
		return values;
	}

	protected static boolean equalsWithinTolerance(Number foo, Number bar, Number tolerance) {
		if (foo == null || bar == null || tolerance == null)
			return false;
		final double a = foo.doubleValue();
		final double b = bar.doubleValue();
		final double t = tolerance.doubleValue();
		return t >= Math.abs(a - b);
	}

	private String unit = "mm";

	@Override
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public Number getMaximum() {
		return upper;
	}

	@Override
	public Number setMaximum(Number upper) {
		Number ret = this.upper;
		this.upper = upper;
		return ret;
	}

	@Override
	public Number getMinimum() {
		return lower;
	}

	@Override
	public Number setMinimum(Number lower) {
		Number ret = this.lower;
		this.lower = lower;
		return ret;
	}

	public boolean isRealisticMove() {
		return realisticMove;
	}

	public void setRealisticMove(boolean realisticMove) {
		this.realisticMove = realisticMove;
	}

	public double getMoveRate() {
		return moveRate;
	}

	public void setMoveRate(double moveRate) {
		this.moveRate = moveRate;
	}

	@Override
	public void abort() throws ScanningException, InterruptedException {
		try {
			terminate(TerminationPreference.CONTROLLED);
		} catch (Exception e) {
			throw new ScanningException("Exception while stopping scannable " + getName(), e);
		}
	}
}
