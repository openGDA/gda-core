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

import static uk.ac.gda.ui.tool.ClientMessages.AT_END;
import static uk.ac.gda.ui.tool.ClientMessages.AT_END_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.AT_START;
import static uk.ac.gda.ui.tool.ClientMessages.AT_START_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.DARK_EXPOSURE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.EXPOSURE;
import static uk.ac.gda.ui.tool.ClientMessages.FLAT_EXPOSURE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.IMAGE_CALIBRATION;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_DARK;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_DARK_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_FLAT;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_FLAT_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.GUIComponents.checkComponent;
import static uk.ac.gda.ui.tool.GUIComponents.doublePositiveContent;
import static uk.ac.gda.ui.tool.GUIComponents.integerPositiveContent;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

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

	/** The Calibration Composite elements **/
	private Text numberDark;
	private Text darkExposure;
	private Text numberFlat;
	private Text flatExposure;
	private Button beforeAcquisition;
	private Button afterAcquisition;

	private Composite composite;

	private DataBindingContext bindingContext = new DataBindingContext();

	@Override
	public Composite createComposite(Composite parent, int style) {
		composite = createClientGroup(parent, SWT.NONE, 2, IMAGE_CALIBRATION);
		createClientGridDataFactory().applyTo(composite);
		createElements(composite, SWT.NONE, SWT.BORDER);
		bindElements();
		return composite;
	}

	@Override
	public void reload() {
		if (composite == null || composite.isDisposed()) return;
		removeOldBindings();
		bindElements();
	}

	private void createElements(Composite parent, int labelStyle, int textStyle) {
		this.numberDark = integerPositiveContent(parent, labelStyle, textStyle,
				NUM_DARK, NUM_DARK_TOOLTIP);
		this.darkExposure = doublePositiveContent(parent, labelStyle, textStyle,
				EXPOSURE, DARK_EXPOSURE_TP);

		this.numberFlat = integerPositiveContent(parent, labelStyle, textStyle,
				NUM_FLAT, NUM_FLAT_TOOLTIP);
		this.flatExposure = doublePositiveContent(parent, labelStyle, textStyle,
				EXPOSURE, FLAT_EXPOSURE_TP);

		this.beforeAcquisition = checkComponent(parent,
				AT_START, AT_START_TOOLTIP);
		this.afterAcquisition = checkComponent(parent,
				AT_END, AT_END_TOOLTIP);
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

		var flatProjectionsUi = WidgetProperties.text(SWT.Modify).observe(numberFlat);
		var flatProjectionsModel = PojoProperties.value("numberExposures", Integer.class).observe(flatsModel);
		bindingContext.bindValue(flatProjectionsUi, flatProjectionsModel);

		var darkProjectionsUi = WidgetProperties.text(SWT.Modify).observe(numberDark);
		var darkProjectionsModel = PojoProperties.value("numberExposures", Integer.class).observe(darksModel);
		bindingContext.bindValue(darkProjectionsUi, darkProjectionsModel);

		/* exposure */

		var flatDetector = flatsModel.getDetectorDocument();
		var flatExposureUi = WidgetProperties.text(SWT.Modify).observe(flatExposure);
		var flatExposureModel = PojoProperties.value("exposure", Double.class).observe(flatDetector);
		bindingContext.bindValue(flatExposureUi, flatExposureModel);

		var darkDetector = darksModel.getDetectorDocument();
		var darkExposureUi = WidgetProperties.text(SWT.Modify).observe(darkExposure);
		var darkExposureModel = PojoProperties.value("exposure", Double.class).observe(darkDetector);
		bindingContext.bindValue(darkExposureUi, darkExposureModel);

		/* before/after scan */

		var beforeScanUi = WidgetProperties.buttonSelection().observe(beforeAcquisition);
		var beforeScanFlatsModel = PojoProperties.value("beforeAcquisition", Boolean.class).observe(flatsModel);
		var beforeScanDarksModel = PojoProperties.value("beforeAcquisition", Boolean.class).observe(darksModel);
		bindingContext.bindValue(beforeScanUi, beforeScanFlatsModel);
		bindingContext.bindValue(beforeScanUi, beforeScanDarksModel);

		var afterScanUi = WidgetProperties.buttonSelection().observe(afterAcquisition);
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
