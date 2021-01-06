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

package uk.ac.gda.client.composites;

import java.util.Optional;

import org.eclipse.swt.events.SelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Group of buttons to execute basic run/save/create functions.
 *
 *  @author Maurizio Nagni
 */
public class AcquisitionCompositeButtonGroupFactoryBuilder {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionCompositeButtonGroupFactoryBuilder.class);

	private Optional<SelectionListener> runListener = Optional.empty();
	private Optional<SelectionListener> newListener = Optional.empty();
	private Optional<SelectionListener> saveListener = Optional.empty();

	/**
	 * Sets the {@link SelectionListener} for the run button
	 * @param selectionListener the listener to add to the run button
	 * @return an instance of this builder
	 */
	public AcquisitionCompositeButtonGroupFactoryBuilder addRunSelectionListener(SelectionListener selectionListener) {
		this.runListener = Optional.of(selectionListener);
		logger.debug("Adding runListener {}", this.runListener);
		return this;
	}

	/**
	 * Sets the {@link SelectionListener} for the new button
	 * @param selectionListener the listener to add to the new button
	 * @return an instance of this builder
	 */
	public AcquisitionCompositeButtonGroupFactoryBuilder addNewSelectionListener(SelectionListener selectionListener) {
		this.newListener = Optional.of(selectionListener);
		logger.debug("Adding newListener {}", this.newListener);
		return this;
	}

	/**
	 * Sets the {@link SelectionListener} for the save button
	 * @param selectionListener the listener to add to the save button
	 * @return an instance of this builder
	 */
	public AcquisitionCompositeButtonGroupFactoryBuilder addSaveSelectionListener(SelectionListener selectionListener) {
		this.saveListener = Optional.of(selectionListener);
		logger.debug("Adding saveListener {}", this.saveListener);
		return this;
	}

	public CompositeFactory build() {
		ButtonGroupFactoryBuilder builder = new ButtonGroupFactoryBuilder();

		newListener.ifPresent(listener -> builder.addButton(ClientMessages.NEW, ClientMessages.NEW_CONFIGURATION_TP,
										  listener, ClientImages.ADD));
		saveListener.ifPresent(listener -> builder.addButton(ClientMessages.SAVE, ClientMessages.SAVE_CONFIGURATION_TP,
										   listener, ClientImages.SAVE));
		runListener.ifPresent(listener -> builder.addButton(ClientMessages.RUN, ClientMessages.RUN_CONFIGURATION_TP,
										  listener, ClientImages.RUN));
		return builder.build();
	}
}