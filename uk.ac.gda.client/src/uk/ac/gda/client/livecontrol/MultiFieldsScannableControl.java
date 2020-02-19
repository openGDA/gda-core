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
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.FindableBase;
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
public class MultiFieldsScannableControl extends FindableBase implements LiveControl {

	private static final Logger logger = LoggerFactory.getLogger(MultiFieldsScannableControl.class);
	private String displayName;
	private String group;
	private String scannableName;
	private Boolean showStop; // Show stop by default

	@Override
	public void createControl(Composite composite) {
		final Optional<Scannable> optionalScannable = Finder.getInstance().findOptional(getScannableName());
		if (!optionalScannable.isPresent()) {
			logger.warn("Could not get scannable '{}' for live control", getScannableName());
			return;
		}
		final Scannable scannable = optionalScannable.get();

		final ScannableFieldsComposite sfc = new ScannableFieldsComposite(composite, SWT.None);
		sfc.setScannable(scannable);
		if (getDisplayName() != null) {
			sfc.setDisplayName(displayName);
		}
		if (showStop != null && !showStop) {
			sfc.hideStopButton();
		}
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(group, scannableName, displayName, showStop);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MultiFieldsScannableControl) {
			final MultiFieldsScannableControl other = (MultiFieldsScannableControl)obj;
			return Objects.equals(group, other.group)
					&& Objects.equals(scannableName, other.scannableName)
					&& Objects.equals(displayName, other.displayName)
					&& Objects.equals(showStop, other.showStop);
		}
		return false;
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
