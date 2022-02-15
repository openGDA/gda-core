/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.util.Map;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.rcp.views.ColourThresholdStateComposite;

public class ScannableColourThresholdStateControl extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(ScannableColourThresholdStateControl.class);

	private String displayName;
	private String scannableName; // Used by the finder to get the scannable

	private int canvasWidth = 30;
	private int canvasHeight = 30;

	private Map<String, Color> stateMap;

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite composite) {
		// Get the scannable with the finder
		Optional<Scannable> optionalScannable = Finder.findOptionalOfType(getScannableName(), Scannable.class);
		if (optionalScannable.isEmpty()) {
			logger.warn("Could not get scannable '{}' for live control", getScannableName());
			return;
		}
		Scannable scannable = optionalScannable.get();

		new ColourThresholdStateComposite(composite, SWT.NONE, displayName,
				canvasWidth, canvasHeight, scannable, stateMap);
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public Map<String, Color> getStateMap() {
		return stateMap;
	}

	public void setStateMap(Map<String, Color> stateMap) {
		this.stateMap = stateMap;
	}

	public String getCanvasWidth() {
		return String.valueOf(canvasWidth);
	}

	public void setCanvasWidth(String canvasWidth) {
		this.canvasWidth = Integer.parseInt(canvasWidth);
	}

	public String getCanvasHeight() {
		return String.valueOf(canvasHeight);
	}

	public void setCanvasHeight(String canvasHeight) {
		this.canvasHeight = Integer.parseInt(canvasHeight);
	}
}
