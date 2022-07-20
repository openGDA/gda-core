/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view.configuration.tomography;

import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.innerComposite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;
import static uk.ac.gda.ui.tool.ClientSWTElements.spinner;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

/**
 * Composite to edit an {@link ImageCalibration} document.
 *
 * @author Maurizio Nagni
 */
public class DarkFlatCompositeFactory implements CompositeFactory, Reloadable {

	private Button darkFlatsAtStart;
	private Button darkFlatsAtEnd;

	private Spinner darkFields;
	private Spinner flatFields;

	private DataBindingContext bindingContext = new DataBindingContext();

	@Override
	public Composite createComposite(Composite parent, int style) {
		var darksAndFlats = composite(parent, 2);

		label(darksAndFlats, "Acquire darks/flats");
		var startEndButtons = innerComposite(darksAndFlats, 2, true);
		darkFlatsAtStart = new Button(startEndButtons, SWT.CHECK);
		darkFlatsAtStart.setText("@Start");
		STRETCH.applyTo(darkFlatsAtStart);

		darkFlatsAtEnd = new Button(startEndButtons, SWT.CHECK);
		darkFlatsAtEnd.setText("@End");
		STRETCH.applyTo(darkFlatsAtEnd);

		label(darksAndFlats, "Dark fields");
		label(darksAndFlats, "Flat fields");

		darkFields = spinner(darksAndFlats);
		flatFields = spinner(darksAndFlats);

		bindElements();

		return darksAndFlats;
	}

	@Override
	public void reload() {
		removeOldBindings();
		bindElements();
	}

	private void removeOldBindings() {
		new ArrayList<>(bindingContext.getBindings()).forEach(binding -> {
			bindingContext.removeBinding(binding);
			binding.dispose();
		});
	}

	private void bindElements() {

		/* projections */

		var imageCalibration = getScanningConfiguration().getImageCalibration();
		var flatsModel = imageCalibration.getFlatCalibration();
		var darksModel = imageCalibration.getDarkCalibration();

		var flatProjectionsUi = WidgetProperties.spinnerSelection().observe(flatFields);
		var flatProjectionsModel = PojoProperties.value("numberExposures", Integer.class).observe(flatsModel);
		bindingContext.bindValue(flatProjectionsUi, flatProjectionsModel);

		var darkProjectionsUi = WidgetProperties.spinnerSelection().observe(darkFields);
		var darkProjectionsModel = PojoProperties.value("numberExposures", Integer.class).observe(darksModel);
		bindingContext.bindValue(darkProjectionsUi, darkProjectionsModel);

		/* before/after scan */

		var beforeScanUi = WidgetProperties.buttonSelection().observe(darkFlatsAtStart);
		var beforeScanFlatsModel = PojoProperties.value("beforeAcquisition", Boolean.class).observe(flatsModel);
		var beforeScanDarksModel = PojoProperties.value("beforeAcquisition", Boolean.class).observe(darksModel);
		bindingContext.bindValue(beforeScanUi, beforeScanFlatsModel);
		bindingContext.bindValue(beforeScanUi, beforeScanDarksModel);

		var afterScanUi = WidgetProperties.buttonSelection().observe(darkFlatsAtEnd);
		var afterScanFlatsModel = PojoProperties.value("afterAcquisition", Boolean.class).observe(flatsModel);
		var afterScanDarksModel = PojoProperties.value("afterAcquisition", Boolean.class).observe(darksModel);
		bindingContext.bindValue(afterScanUi, afterScanFlatsModel);
		bindingContext.bindValue(afterScanUi, afterScanDarksModel);
	}

	private ScanningConfiguration getScanningConfiguration() {
		return getScanningAcquisitionTemporaryHelper()
				.getAcquisitionConfiguration()
				.orElseThrow();
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}
