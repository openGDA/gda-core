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

package uk.ac.gda.tomography.scan.editor.view.configuration.radiography;

import static uk.ac.gda.ui.tool.ClientMessages.NAME;
import static uk.ac.gda.ui.tool.ClientMessages.NAME_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.TOTAL_PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.TOTAL_PROJECTIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.DEFAULT_TEXT_SIZE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyPositiveIntegerText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.ui.tool.Reloadable;

/**
 * @author Maurizio Nagni
 */
public class RadiographyConfigurationLayoutFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(RadiographyConfigurationLayoutFactory.class);

	private final AcquisitionController<ScanningAcquisition> acquisitionController;
	private Composite mainComposite;

	/** Scan prefix **/
	private Text name;

	/** The Projections Composite elements **/
	private Text totalProjections;

	public RadiographyConfigurationLayoutFactory(AcquisitionController<ScanningAcquisition> acquisitionController) {
		this.acquisitionController = acquisitionController;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.debug("Creating {}", this);
		mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 3);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainComposite);
		standardMarginHeight(mainComposite.getLayout());
		standardMarginWidth(mainComposite.getLayout());

		createElements(mainComposite, SWT.NONE, SWT.BORDER);

		mainComposite.setSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		logger.debug("Created {}", this);
		return mainComposite;
	}

	/**
	 * @param parent
	 *            a three column composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void createElements(Composite parent, int labelStyle, int textStyle) {
		nameContent(parent, labelStyle, textStyle);

		// Defines a Group with 3 columns
		Group group = createClientGroup(parent, SWT.NONE, 3, PROJECTIONS);
		// Configure the group to span all the parent 3 columns
		createClientGridDataFactory().span(3, 1).applyTo(group);
		projectionsContent(group, labelStyle, textStyle);
	}

	private void nameContent(Composite parent, int labelStyle, int textStyle) {
		Label labelName = createClientLabel(parent, labelStyle, NAME);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.END).applyTo(labelName);

		this.name = createClientText(parent, textStyle, NAME_TOOLTIP);
		createClientGridDataFactory().span(2, 1).applyTo(this.name);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		Label label = createClientLabel(parent, labelStyle, TOTAL_PROJECTIONS);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		totalProjections = createClientText(parent, textStyle, TOTAL_PROJECTIONS_TOOLTIP, verifyOnlyPositiveIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(totalProjections);
	}

	@Override
	public void reload() {
		// TBD
//		bindElements();
//		initialiseElements();
		mainComposite.getShell().layout(true, true);
	}
}
