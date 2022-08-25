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

package uk.ac.diamond.daq.autoprocessing.ui;

/**
 * Parameter used in autoprocessing configuration
 * <p>
 * Contains the parameter value and name as well as descriptions and hints to generate UI
 *
 * @param <T>
 */
public class AutoProcessingField<T> {

	public enum AutoProcEditorHint {
		NO_HINT, FILE, XRF
	}

	private String name;
	private String label;
	private T value;
	private String description;
	private String unit = "";
	private String[] options;
	private AutoProcEditorHint hint = AutoProcEditorHint.NO_HINT;

	public AutoProcessingField(String name, T value, String description) {
		this.name = name;
		this.value = value;
		this.description = description;
		this.label = name;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		if (value == null) {
			return;
		}
		this.value = value;
	}

	public String getDescription() {
		return this.description;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getUnit() {
		return this.unit;
	}

	public void setEditorHint(AutoProcEditorHint hint) {
		this.hint = hint;
	}

	public AutoProcEditorHint getEditorHint() {
		return this.hint;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return this.label;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}
}