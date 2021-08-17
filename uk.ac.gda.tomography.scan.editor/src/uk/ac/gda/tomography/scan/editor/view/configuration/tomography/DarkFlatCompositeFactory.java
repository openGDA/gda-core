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
import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURATION_LAYOUT_ERROR;
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
import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

/**
 * Composite to edit an {@link ImageCalibration} document.
 *
 * @author Maurizio Nagni
 */
public class DarkFlatCompositeFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(DarkFlatCompositeFactory.class);

	/** The Calibration Composite elements **/
	private Text numberDark;
	private Text darkExposure;
	private Text numberFlat;
	private Text flatExposure;
	private Button beforeAcquisition;
	private Button afterAcquisition;

	private ImageCalibrationHelper imageCalibrationHelper;
	private Composite composite;

	public DarkFlatCompositeFactory() {
		try {
			this.imageCalibrationHelper = new ImageCalibrationHelper(this::getScanningConfiguration);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning("Tomography cannot be instantiated normally", e);
		}
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		composite = createClientGroup(parent, SWT.NONE, 2, IMAGE_CALIBRATION);
		createClientGridDataFactory().applyTo(composite);

		logger.debug("Creating {}", this);
		try {
			createElements(composite, SWT.NONE, SWT.BORDER);
			bindElements();
			initialiseElements();
			addWidgetsListener();
			logger.debug("Created {}", this);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
		return composite;
	}

	@Override
	public void reload() {
		try {
			bindElements();
			initialiseElements();
			composite.getShell().layout(true, true);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
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

	private void setNumberFlat(Widget widget) {
		Optional.ofNullable(widget)
			.map(Text.class::cast)
			.map(Text::getText)
			.map(Integer::parseInt)
			.ifPresent(imageCalibrationHelper::updateFlatNumberExposures);
	}

	private void setNumberDark(Widget widget) {
		Optional.ofNullable(widget)
		.map(Text.class::cast)
		.map(Text::getText)
		.map(Integer::parseInt)
		.ifPresent(imageCalibrationHelper::updateDarkNumberExposures);
	}

	private void bindElements() {
		// Nothing to do
	}

	private void initialiseElements() {
		ScanningConfiguration configuration = getAcquisitionConfiguration();


		var ic = configuration.getImageCalibration();
		Optional.ofNullable(ic.getDarkCalibration())
			.ifPresent(this::initializeDarkCalibration);

		Optional.ofNullable(ic.getFlatCalibration())
			.ifPresent(this::initializeFlatCalibration);
	}

	private void initializeDarkCalibration(DarkCalibrationDocument darkCalibrationDocument) {
		int exposures = Optional.ofNullable(darkCalibrationDocument.getNumberExposures())
				.orElse(0);
		numberDark.setText(Integer.toString(exposures));

		double exposure = Optional.ofNullable(darkCalibrationDocument.getDetectorDocument())
				.map(DetectorDocument::getExposure)
				.orElse(0d);
		darkExposure.setText(Double.toString(exposure));


		// For the moment dark and flat have the same boolean values
		boolean selected = Optional.ofNullable(darkCalibrationDocument.isBeforeAcquisition())
				.orElse(false);
		beforeAcquisition.setSelection(selected);

		// For the moment dark and flat have the same boolean values
		selected = Optional.ofNullable(darkCalibrationDocument.isAfterAcquisition())
				.orElse(false);
		afterAcquisition.setSelection(selected);

		forceFocusOnEmpty(numberDark, Integer.toString(exposures));
	}

	private void initializeFlatCalibration(FlatCalibrationDocument flatCalibrationDocument) {
		int exposures = Optional.ofNullable(flatCalibrationDocument.getNumberExposures())
				.orElse(0);
		numberFlat.setText(Integer.toString(exposures));

		double exposure = Optional.ofNullable(flatCalibrationDocument.getDetectorDocument())
			.map(DetectorDocument::getExposure)
			.orElse(0d);
		flatExposure.setText(Double.toString(exposure));

		forceFocusOnEmpty(numberFlat, Integer.toString(exposures));
	}

	private void forceFocusOnEmpty(Text text, String defaultValue) {
		text.addFocusListener(FocusListener.focusLostAdapter(c -> {
			if (text.getText().trim().isEmpty()) {
				text.setText(defaultValue);
			}
		}));
	}

	private void beforeAcquisitionListener(SelectionEvent event) {
		if (!event.getSource().equals(beforeAcquisition))
			return;
		imageCalibrationHelper.updateDarkBeforeAcquisitionExposures(beforeAcquisition.getSelection());
		imageCalibrationHelper.updateFlatBeforeAcquisitionExposures(beforeAcquisition.getSelection());
	}

	private void afterAcquisitionListener(SelectionEvent event) {
		if (!event.getSource().equals(afterAcquisition))
			return;
		imageCalibrationHelper.updateDarkAfterAcquisitionExposures(afterAcquisition.getSelection());
		imageCalibrationHelper.updateFlatAfterAcquisitionExposures(afterAcquisition.getSelection());
	}

	private  void addWidgetsListener() {
		// Calibration fields
		addWidgetDisposableListener(beforeAcquisition, SelectionListener.widgetSelectedAdapter(this::beforeAcquisitionListener));
		addWidgetDisposableListener(afterAcquisition, SelectionListener.widgetSelectedAdapter(this::afterAcquisitionListener));
		addWidgetDisposableListener(numberDark, SWT.Modify, event -> setNumberDark(event.widget));
		addWidgetDisposableListener(numberFlat, SWT.Modify, event -> setNumberFlat(event.widget));
	}

	private ScanningConfiguration getAcquisitionConfiguration() {
		return getScanningAcquisitionTemporaryHelper()
			.getAcquisitionConfiguration()
			.orElseThrow();
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
