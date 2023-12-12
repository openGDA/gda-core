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
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.rcp.views.ScannableDisplayComposite;

public class ScannableDisplayLiveControl extends LiveControlBase {

	private static final Logger logger = LoggerFactory.getLogger(ScannableDisplayLiveControl.class);

	private String displayName;
	private String scannableName;
	private String userUnits;
	private int style = SWT.NONE;
	private int textWidth = 120;
	private int labelTextSize = Display.getDefault().getSystemFont().getFontData()[0].getHeight();
	private int valueTextSize = Display.getDefault().getSystemFont().getFontData()[0].getHeight();
	private int valueColour = SWT.COLOR_BLACK;
	private int labelColour = SWT.COLOR_BLACK;
	private boolean boldValue = false;
	private boolean isTextInput;
	private double valueThreshold = Double.POSITIVE_INFINITY;
	private int aboveThresholdColour = SWT.COLOR_RED;
	private boolean rescalingFont = false;

	@Override
	public void createControl(Composite parent) {
		Finder.findOptionalOfType(getScannableName(), Scannable.class)
				.ifPresentOrElse( scannable -> {
					ScannableDisplayComposite composite = new ScannableDisplayComposite(parent, getStyle());
					composite.setDisplayName(getDisplayName());
					composite.setRescalingFont(isRescalingFont());
					composite.setTextWidth(getTextWidth());
					composite.setLabelSize(getLabelTextSize());
					composite.setValueSize(getValueTextSize());
					composite.setLabelColour(getLabelColour());
					composite.setValueColourDefault(getValueColour());
					composite.setTextInput(isTextInput());
					composite.setValueBold(isBoldValue());
					composite.setValueThreshold(getValueThreshold());
					composite.setAboveThresholdColour(getAboveThresholdColour());
					if (getUserUnits() != null) {
						composite.setUserUnit(getUserUnits());
					}
					composite.setScannable(scannable); // this calls composite.configure() - must be at the end
				}, () -> logger.warn("Could not get scannable '{}' for live control", getScannableName()) );
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

	public String getUserUnits() {
		return userUnits;
	}

	public void setUserUnits(String userUnits) {
		this.userUnits = userUnits;
	}

	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getTextWidth() {
		return textWidth;
	}

	public void setTextWidth(int textWidth) {
		this.textWidth = textWidth;
	}

	public int getLabelTextSize() {
		return labelTextSize;
	}

	public void setLabelTextSize(int labelTextSize) {
		this.labelTextSize = labelTextSize;
	}

	public int getValueTextSize() {
		return valueTextSize;
	}

	public void setValueTextSize(int valueTextSize) {
		this.valueTextSize = valueTextSize;
	}

	public int getValueColour() {
		return valueColour;
	}

	public void setValueColour(int valueColour) {
		this.valueColour = valueColour;
	}

	public int getLabelColour() {
		return labelColour;
	}

	public void setLabelColuor(int setLabelColuor) {
		this.labelColour = setLabelColuor;
	}

	public boolean isTextInput() {
		return isTextInput;
	}

	public void setTextInput(boolean isTextInput) {
		this.isTextInput = isTextInput;
	}

	public boolean isBoldValue() {
		return boldValue;
	}

	public void setBoldValue(boolean boldLabel) {
		this.boldValue = boldLabel;
	}

	public double getValueThreshold() {
		return valueThreshold;
	}

	public void setValueThreshold(double valueThreshold) {
		this.valueThreshold = valueThreshold;
	}

	public int getAboveThresholdColour() {
		return aboveThresholdColour;
	}

	public void setAboveThresholdColour(int aboveThresholdColour) {
		this.aboveThresholdColour = aboveThresholdColour;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(aboveThresholdColour, boldValue, displayName, isTextInput, labelColour,
				labelTextSize, scannableName, style, textWidth, userUnits, valueColour, valueTextSize, valueThreshold);
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
		ScannableDisplayLiveControl other = (ScannableDisplayLiveControl) obj;
		return aboveThresholdColour == other.aboveThresholdColour && boldValue == other.boldValue
				&& Objects.equals(displayName, other.displayName) && isTextInput == other.isTextInput
				&& labelColour == other.labelColour && labelTextSize == other.labelTextSize
				&& Objects.equals(scannableName, other.scannableName) && style == other.style
				&& textWidth == other.textWidth && Objects.equals(userUnits, other.userUnits)
				&& valueColour == other.valueColour && valueTextSize == other.valueTextSize
				&& Double.doubleToLongBits(valueThreshold) == Double.doubleToLongBits(other.valueThreshold);
	}

	public boolean isRescalingFont() {
		return rescalingFont;
	}

	public void setRescalingFont(boolean rescalingFont) {
		this.rescalingFont = rescalingFont;
	}

}
