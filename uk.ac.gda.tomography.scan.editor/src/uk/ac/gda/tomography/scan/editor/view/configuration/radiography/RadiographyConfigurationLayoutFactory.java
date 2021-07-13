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
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.helper.ScannableTrackDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.GUIComponents;
import uk.ac.gda.ui.tool.Reloadable;

/**
 * @author Maurizio Nagni
 */
public class RadiographyConfigurationLayoutFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(RadiographyConfigurationLayoutFactory.class);

	/** Scan prefix **/
	private Text name;

	/** The Projections Composite elements **/
	private Text frames;

	private final ScannableTrackDocumentHelper scannableTrackDocumentHelper;
	private final AcquisitionController<ScanningAcquisition> acquisitionController;

	private Composite mainComposite;

	public RadiographyConfigurationLayoutFactory(AcquisitionController<ScanningAcquisition> acquisitionController) {
		this.acquisitionController = acquisitionController;
		this.scannableTrackDocumentHelper = new ScannableTrackDocumentHelper(this::getScanningParameters);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.debug("Creating {}", this);
		mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.BEGINNING).grab(true, true).applyTo(mainComposite);
		standardMarginHeight(mainComposite.getLayout());
		standardMarginWidth(mainComposite.getLayout());

		createElements(mainComposite, SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
		addWidgetsListener();
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
		this.name = GUIComponents.textContent(parent, labelStyle, textStyle,
				NAME, NAME_TOOLTIP);
		// guarantee that fills the horizontal space
		createClientGridDataFactory().grab(true, true).applyTo(name);

		var group = createClientGroup(parent, SWT.NONE, 1, PROJECTIONS);
		createClientGridDataFactory().applyTo(group);
		this.frames = GUIComponents.integerPositiveContent(group, labelStyle, textStyle,
				PROJECTIONS, PROJECTIONS_TOOLTIP);
	}

	@Override
	public void reload() {
		bindElements();
		initialiseElements();
		mainComposite.getShell().layout(true, true);
	}

	// ---- BINDING ----
	private void bindElements() {
		name.addModifyListener(modifyNameListener);
	}

	private final ModifyListener modifyNameListener = event -> updateAcquisitionName();

	private void updateAcquisitionName() {
		getAcquisitionController().getAcquisition().setName(name.getText());
	}

	private AcquisitionController<ScanningAcquisition> getAcquisitionController() {
		return acquisitionController;
	}
	// ------------------------

	// ---- INITIALIZATION ----
	private void initialiseElements() {
		name.setText(getAcquisitionController().getAcquisition().getName());
		initializeProjections();
	}

	private void initializeProjections() {
		frames.setText(Integer.toString(getScannableTrackDocument().getPoints()));
	}
	// ------------------------


	// ---- WIDGET LISTENER ----
	private  void addWidgetsListener() {
		frames.addModifyListener(this::frameListener);
	}

	private void frameListener(ModifyEvent event) {
		if (!event.getSource().equals(frames))
			return;
		int points = Optional.ofNullable(frames.getText())
				.filter(s -> !s.isEmpty())
				.map(Integer::parseInt)
				.orElseGet(() -> 1);
		updateScannableTrackDocumentsPoints(points);
	}

	private void updateScannableTrackDocumentsPoints(int numPoints) {
		int size = getScanningParameters().getScanpathDocument().getScannableTrackDocuments().size();
		var trackDocumentsPoints = new int[size];
		Arrays.fill(trackDocumentsPoints, numPoints);
		scannableTrackDocumentHelper.updateScannableTrackDocumentsPoints(trackDocumentsPoints);
		SpringApplicationContextFacade.publishEvent(
				new ScanningAcquisitionChangeEvent(this));
	}

	// ------------------------
	private ScannableTrackDocument getScannableTrackDocument() {
		return getAcquisitionController().getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getScanpathDocument().getScannableTrackDocuments()
				.get(0);
	}

	private ScanningConfiguration getAcquisitionConfiguration() {
		return getAcquisitionController().getAcquisition().getAcquisitionConfiguration();
	}

	private ScanningParameters getScanningParameters() {
		return getAcquisitionConfiguration().getAcquisitionParameters();
	}
}
