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

import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURATION_LAYOUT_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.NAME;
import static uk.ac.gda.ui.tool.ClientMessages.NAME_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.tomography.scan.editor.view.configuration.tomography.DarkFlatCompositeFactory;
import uk.ac.gda.tomography.scan.editor.view.configuration.tomography.ProjectionsCompositeFactory;
import uk.ac.gda.ui.tool.GUIComponents;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

/**
 * @author Maurizio Nagni
 */
public class RadiographyConfigurationLayoutFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(RadiographyConfigurationLayoutFactory.class);

	/** Scan prefix **/
	private Text name;

	private Composite mainComposite;

	private List<Reloadable> composites = new ArrayList<>();

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.debug("Creating {}", this);
		mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.BEGINNING).grab(true, true).applyTo(mainComposite);
		standardMarginHeight(mainComposite.getLayout());
		standardMarginWidth(mainComposite.getLayout());

		try {
			createElements(mainComposite, SWT.NONE, SWT.BORDER);
			bindElements();
			initialiseElements();
			addWidgetsListener();
			logger.debug("Created {}", this);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
		return mainComposite;
	}

	@Override
	public void reload() {
		try {
			bindElements();
			initialiseElements();
			mainComposite.getShell().layout(true, true);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
	}

	/**
	 * @param parent
	 *            a three column composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void createElements(Composite parent, int labelStyle, int textStyle) {
		this.name = GUIComponents.textContent(parent, labelStyle, textStyle,
				NAME, NAME_TOOLTIP);
		// guarantee that fills the horizontal space
		createClientGridDataFactory().grab(true, true).applyTo(name);

		//----- Reference for other configuration components
		var projections = new ProjectionsCompositeFactory();
		composites.add(projections);
		projections.createComposite(parent, textStyle);
		//----- Reference for other configuration components
		var darkFlat = new DarkFlatCompositeFactory();
		composites.add(darkFlat);
		darkFlat.createComposite(parent, textStyle);
		//----- Reference for other configuration components
	}

	// ---- BINDING ----
	private void bindElements() {
		name.addModifyListener(modifyNameListener);
	}

	private final ModifyListener modifyNameListener = event -> updateAcquisitionName();

	private void updateAcquisitionName() {
		getScanningAcquisitionTemporaryHelper()
			.getScanningAcquisition()
			.ifPresent(a -> a.setName(name.getText()));
	}

	// ---- INITIALIZATION ----
	private void initialiseElements() {
		getScanningAcquisitionTemporaryHelper()
			.getScanningAcquisition()
			.map(ScanningAcquisition::getName)
			.ifPresent(name::setText);
	}


	// ---- WIDGET LISTENER ----
	private  void addWidgetsListener() {
		// Nothing to do
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}
