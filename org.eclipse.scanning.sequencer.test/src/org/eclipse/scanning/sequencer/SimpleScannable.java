package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.AbstractNameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * Simple implementation of IScannable, with the ability to specify delay time, for testing timeout
 */
public class SimpleScannable extends AbstractNameable implements IScannable<Double> {

	private int level;
	private double value;
	private int delay = 0; // delay time in seconds

	public SimpleScannable(int level, String name, double value) {
		this.level = level;
		setName(name);
		this.value = value;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public Double getPosition() throws ScanningException {
		return value;
	}

	@Override
	public Double setPosition(Double value, IPosition position) throws ScanningException {
		this.value = value;
		return value;
	}

	@Override
	public String toString() {
		return "SimpleScannable [level=" + level + ", name=" + getName() + ", value=" + value + "]";
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	@Override
	public void abort() throws ScanningException, InterruptedException {
		// Mock Scannable, nothing to abort.
	}
	
}
