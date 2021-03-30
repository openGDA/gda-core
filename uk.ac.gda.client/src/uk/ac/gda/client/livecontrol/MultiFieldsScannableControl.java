/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.rcp.views.ScannableFieldsComposite;
/**
 * A {@link LiveControl} for a {@link Scannable} with multiple inputs and multiple outputs.
 *
 * If units are required for each field, they should be set in {@link Scannable}'s output format property
 * which must be separated with its value by a single space!
 *
 * @since 9.16
 * @author Fajin Yuan
 */
public class MultiFieldsScannableControl extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(MultiFieldsScannableControl.class);
	private String displayName;
	private String scannableName;
	private Boolean showStop; // Show stop by default

	@Override
	public void createControl(Composite composite) {
		Finder.findOptionalOfType(getScannableName(), Scannable.class)
				.ifPresentOrElse( scannable -> {
					ScannableFieldsComposite sfc = new ScannableFieldsComposite(composite, SWT.None);
					sfc.setScannable(scannable);
					if (getDisplayName() != null) {
						sfc.setDisplayName(displayName);
					}
					if (showStop != null && !showStop) {
						sfc.hideStopButton();
					}
				}, () -> logger.warn("Could not get scannable '{}' for live control", getScannableName()) );
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(displayName, scannableName, showStop);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiFieldsScannableControl other = (MultiFieldsScannableControl) obj;
		return Objects.equals(displayName, other.displayName) && Objects.equals(scannableName, other.scannableName)
				&& Objects.equals(showStop, other.showStop);
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Boolean getShowStop() {
		return showStop;
	}

	public void setShowStop(Boolean showStop) {
		this.showStop = showStop;
	}


}
