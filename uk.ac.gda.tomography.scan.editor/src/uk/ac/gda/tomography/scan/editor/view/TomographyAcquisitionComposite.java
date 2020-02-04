/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.client.gui.energy.BeamEnergyDialogBuilder;
import uk.ac.gda.tomography.scan.editor.StagesComposite;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientResourceManager;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Tomography dashboard
 *
 * @author Maurizio Nagni
 */
public class TomographyAcquisitionComposite extends CompositeTemplate<TomographyParametersAcquisitionController> {

	private static final Logger logger = LoggerFactory.getLogger(TomographyAcquisitionComposite.class);

	private Group source;
	private Button energyButton;
	private Label energy;
	private Label energyValue;
	private Button shutter;
	private Label shutterLabel;
	private Label shutterValue;

	public TomographyAcquisitionComposite(final Composite parent, final TomographyParametersAcquisitionController controller) {
		super(parent, SWT.NONE, controller);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		headerElements(ClientSWTElements.createComposite(this, SWT.NONE, 3), labelStyle, textStyle);
		stageCompose(ClientSWTElements.createComposite(this, SWT.NONE, 1));
	}

	private void headerElements(Composite parent, int labelStyle, int textStyle) {
		createSource(ClientSWTElements.createGroup(parent, 3, ClientMessages.SOURCE), labelStyle, textStyle);
	}

	private void stageCompose(Composite parent) {
		StagesComposite stagesComposite = StagesComposite.buildModeComposite(parent, controller);
		controller.setTomographyMode(stagesComposite.getStageType().getStage());
	}

	private void createSource(Composite parent, int labelStyle, int textStyle) {
		energyButton = ClientSWTElements.createButton(parent, textStyle, ClientMessages.EMPTY_MESSAGE, ClientMessages.ENERGY_KEV, ClientImages.BEAM_16);
		energy = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.ENERGY_KEV);
		energyValue = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NOT_AVAILABLE, null,
				FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont()));

		shutter = ClientSWTElements.createButton(parent, SWT.CHECK, ClientMessages.EMPTY_MESSAGE, ClientMessages.SHUTTER_TP);
		shutterLabel = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.SHUTTER);
		shutterValue = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NOT_AVAILABLE, null,
				FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont()));
	}

	@Override
	protected void bindElements() {
		energyButton.addListener(SWT.Selection, event -> {
			BeamEnergyDialogBuilder builder = new BeamEnergyDialogBuilder();
			builder.addImagingController();
			builder.build(getShell()).open();
		});
	}

	@Override
	protected void initialiseElements() {
		// No elements to initialise
	}
}
