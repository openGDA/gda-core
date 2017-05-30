/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.editors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class SpecsRegionEditorRaw {

	@Inject
	private IGuiGeneratorService guiGenerator;

	private Composite parent;

	@Inject
	public SpecsRegionEditorRaw() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		this.parent = parent;
	}

	@Focus
	public void onFocus() {
		parent.setFocus();
	}

	@Optional
	@Inject
	private void selectedRegionChanged(@UIEventTopic(SpecsUiConstants.REGION_SELECTED_EVENT) SpecsPhoibosRegion region) {
		// Remove all the existing GUI
		for (Control control : parent.getChildren()) {
			control.dispose();
		}

		// TODO Probably want a custom GUI with more features here
		guiGenerator.generateGui(region, parent);
	}
}