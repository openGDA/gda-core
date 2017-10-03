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

package gda.device.scannable.component;

import gda.device.Scannable;
import gda.device.scannable.PositionConvertorFunctions;

/**
 * All setters, getters and messages work with external positions.
 */
public class ScannableLimitsComponent implements LimitsComponent {

	/**
	 * Array of lower limits (one for each input name). Null if no limits set.
	 * Any value within array may be null if that input has no corresponding limit.
	 */
	private Double[] internalLowerLim = null;

	/**
	 * Array of upper limits (one for each input name). Null if no limits set.
	 * Any value within array may be null if that input has no corresponding limit.
	 */
	private Double[] internalUpperLim = null;

	private Scannable hostScannable;

	/**
	 * e.g. 'Scannable' or 'Epics' (for messages only).
	 */
	protected String limitType = "";

	public ScannableLimitsComponent() {
		limitType = "Scannable";
	}

	@Override
	public String checkInternalPosition(Object[] internalPosition) {

		// If neither limits are set, return null indicating okay.
		if ((internalLowerLim == null) & (internalUpperLim == null))
			return null;

		Double[] pos;
		try {
			pos = PositionConvertorFunctions.toDoubleArray(internalPosition);
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}

		// Check lower limits if set
		if (internalLowerLim != null) {
			for (int i = 0; i < internalLowerLim.length; i++) {
				if ((internalLowerLim[i] != null) && (pos[i] != null)) {
					if (pos[i] < internalLowerLim[i]) {
						String fieldName = String.format("%s.%s", getHostScannable().getName(), getHostScannable()
								.getInputNames()[i]);
						String internalRequested = internalPosition[i].toString();
						String internalLimit = internalLowerLim[i].toString();
						return String.format("%s limit violation on %s: %s < %s (internal/hardware/dial values).",
								limitType, fieldName, internalRequested, internalLimit);
					}
				}
			}
		}

		// Check upper limits if set
		if (internalUpperLim != null) {
			for (int i = 0; i < internalUpperLim.length; i++) {
				if ((internalUpperLim[i] != null) && (pos[i] != null)) {
					if (pos[i] > internalUpperLim[i]) {
						String fieldName = String.format("%s.%s", getHostScannable().getName(), getHostScannable()
								.getInputNames()[i]);
						String internalRequested = internalPosition[i].toString();
						String internalLimit = internalUpperLim[i].toString();
						return String.format("%s limit violation on %s: %s > %s (internal/hardware/dial values).",
								limitType, fieldName, internalRequested, internalLimit);
					}
				}
			}
		}
		// Position okay
		return null;
	}

	final private void checkPositionLength(Object[] positionArray) {
		if ((positionArray.length != getHostScannable().getInputNames().length)
				&& (positionArray.length != (getHostScannable().getInputNames().length + getHostScannable().getExtraNames().length))) {
			throw new IllegalArgumentException(String.format(
					"Expected position of length %d or %d but got position of length %d", getHostScannable().getInputNames().length, getHostScannable().getInputNames().length
							+ getHostScannable().getExtraNames().length, positionArray.length));
		}
	}

	@Override
	public Double[] getInternalLower() {
		return internalLowerLim;
	}

	@Override
	public Double[] getInternalUpper() {
		return internalUpperLim;
	}

	@Override
	public void setInternalLower(Double[] internalLowerLim) {
		if (internalLowerLim !=null) {
			checkPositionLength(internalLowerLim);
		}
		this.internalLowerLim = internalLowerLim;
	}

	@Override
	public void setInternalUpper(Double[] internalUpperLim) {
		if (internalUpperLim != null) {
			checkPositionLength(internalUpperLim);
		}
		this.internalUpperLim = internalUpperLim;
	}

	public void setInternalUpper(Double internalUpperLim, int index, int length) {
		if (this.internalUpperLim == null)
			setInternalUpper(new Double[length]);
		this.internalUpperLim[index] = internalUpperLim;
	}

	public void setInternalLower(Double internalLowerLim, int index, int length) {
		if (this.internalLowerLim == null)
			setInternalLower(new Double[length]);
		this.internalLowerLim[index] = internalLowerLim;
	}

	public void setHostScannable(Scannable hostScannable) {
		this.hostScannable = hostScannable;
	}

	public Scannable getHostScannable() {
		return hostScannable;
	}

}
