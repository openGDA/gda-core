/*-
 * Copyright © 2015 Diamond Light Source Ltd.
 */

package gda.scan;

import gda.device.Scannable;
import uk.ac.gda.api.scan.IScanObject;

/**
 * Minimal base class to hold information about each object that will be scanned. The other methods defined in the new IScanObject interface are not defined to
 * force derived concrete versions to implement them.
 *
 * @author Keith Ralphs
 */
public abstract class ScanObject implements IScanObject {

	/**
	 * The scannable this object operates.
	 */
	protected Scannable scannable;

	@Override
	public Scannable getScannable() {
		return scannable;
	}

	@Override
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}
}
