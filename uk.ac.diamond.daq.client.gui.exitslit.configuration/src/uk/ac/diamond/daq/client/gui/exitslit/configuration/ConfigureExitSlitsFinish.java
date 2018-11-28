/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.exitslit.configuration;

import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.COLOUR_WHITE;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createInstructionLabel;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createSeparator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * Final page in sequence - just a message to the user telling them to use Live Controls for fine-tuning.
 */
public class ConfigureExitSlitsFinish extends ConfigureExitSlitsComposite {

	public ConfigureExitSlitsFinish(Composite parent, String title, String description, String instructions) {
		super(parent, title, description);

		GridLayoutFactory.swtDefaults().applyTo(this);
		setBackground(COLOUR_WHITE);

		// Instructions
		GridDataFactory.swtDefaults().applyTo(createInstructionLabel(this, instructions));
		GridDataFactory.fillDefaults().grab(true,  false).applyTo(createSeparator(this));
	}

	@Override
	public boolean canGoToNextPage() {
		return false;
	}

	@Override
	public boolean canGoToPreviousPage() {
		return true;
	}

	@Override
	protected boolean canCancel() {
		return false;
	}

	@Override
	protected boolean canFinish() {
		return true;
	}

	@Override
	protected void updateButtons() {
		// Nothing to do
	}
}
