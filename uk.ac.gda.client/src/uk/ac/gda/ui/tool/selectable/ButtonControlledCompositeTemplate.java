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

package uk.ac.gda.ui.tool.selectable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gda.rcp.views.CompositeFactory;

/**
 * Defines the elements and draw an acquisition configuration layout and the associated command buttons.
 *
 * <p>
 * The buttons creates by {@link #getButtonControlsFactory()} drive the acquisition configuration provided by {@link #getControlledCompositeFactory()},
 * while {@link #getButtonControlsContainerSupplier()} provides a reference to an composite external to the class implementing this interface
 * </p>
 *
 * <p>
 * Note that beacuse of {@link #createButtonControlledComposite(Composite, int)} this interface acts like a template pattern.
 * </p>
 *
 * @author Maurizio Nagni
 */
public interface ButtonControlledCompositeTemplate {

	/**
	 * Creates the acquisition configuration layout
	 */
	CompositeFactory getControlledCompositeFactory();

	/**
	 * Creates the command buttons layout
	 */
	CompositeFactory getButtonControlsFactory();

	/**
	 * Provides the container specific for the control buttons
	 */
	Supplier<Composite> getButtonControlsContainerSupplier();

	default Composite createButtonControlledComposite(Composite parent, int style) {
		// Creates the acquistion configuration
		Composite configuration = getControlledCompositeFactory().createComposite(parent, style);
		// Eventually, creates the associates control buttons
		Optional.ofNullable(getButtonControlsContainerSupplier()).map(Supplier::get).ifPresent(c -> {
			// Deletes, if any the existing composites inside the internal scrollable area
			Arrays.stream(c.getChildren()).forEach(Control::dispose);

			if (getButtonControlsFactory() != null) {
				// Creates the buttons
				getButtonControlsFactory().createComposite(c, style).layout();
				c.layout(true, true);
			}
		});
		return configuration;
	}
}
