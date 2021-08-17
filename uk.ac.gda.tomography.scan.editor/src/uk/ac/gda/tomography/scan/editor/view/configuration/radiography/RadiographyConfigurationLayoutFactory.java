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
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
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

	/** The Projections Composite elements **/
	private Text frames;

	private ScannableTrackDocumentHelper scannableTrackDocumentHelper;

	private Composite mainComposite;

	public RadiographyConfigurationLayoutFactory() {
		try {
			this.scannableTrackDocumentHelper = new ScannableTrackDocumentHelper(this::getScanningParameters);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
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
		initializeProjections();
	}

	private void initializeProjections() {
		List<ScannableTrackDocument> tracks = getScanningAcquisitionTemporaryHelper()
				.getScanpathDocument()
				.map(ScanpathDocument::getScannableTrackDocuments)
				.orElseGet(ArrayList::new);

		if (!tracks.isEmpty()) {
			frames.setText(Integer.toString(tracks.get(0).getPoints()));
		}
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
		int size = getScanningAcquisitionTemporaryHelper()
			.getScanpathDocument()
			.map(ScanpathDocument::getScannableTrackDocuments)
			.map(List::size)
			.orElse(0);

		if (size != numPoints || size == 0) {
			UIHelper.showError("The acquisition document has not enough points.", "Configuration Error");
		}
		var trackDocumentsPoints = new int[size];
		Arrays.fill(trackDocumentsPoints, numPoints);
		scannableTrackDocumentHelper.updateScannableTrackDocumentsPoints(trackDocumentsPoints);
		SpringApplicationContextFacade.publishEvent(
				new ScanningAcquisitionChangeEvent(this));
	}

	// ------------------------
	private ScanningParameters getScanningParameters() {
		return getScanningAcquisitionTemporaryHelper()
				.getScanningParameters()
				.orElseThrow();
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}
