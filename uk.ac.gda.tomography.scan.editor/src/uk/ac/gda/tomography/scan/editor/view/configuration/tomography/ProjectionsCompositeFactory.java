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

import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURATION_LAYOUT_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.GUIComponents.integerPositiveContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.helper.ScannableTrackDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

/**
 * Composite to edit the points in a {@link ScannableTrackDocument} document.
 *
 * @author Maurizio Nagni
 */
public class ProjectionsCompositeFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(ProjectionsCompositeFactory.class);

	/** The Projections Composite elements **/
	private Text projections;

	private ScannableTrackDocumentHelper scannableTrackDocumentHelper;
	private Composite composite;

	public ProjectionsCompositeFactory() {
		try {
			this.scannableTrackDocumentHelper = new ScannableTrackDocumentHelper(this::getScanningParameters);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning("Tomography cannot be instantiated normally", e);
		}
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		composite = createClientGroup(parent, SWT.NONE, 1, PROJECTIONS);
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
		this.projections = integerPositiveContent(parent, labelStyle, textStyle,
				PROJECTIONS, PROJECTIONS_TOOLTIP);
	}

	private void totalProjectionsListener(ModifyEvent event) {
		if (!event.getSource().equals(projections))
			return;
		int numPoints = Optional.ofNullable(projections.getText())
				.filter(s -> !s.isEmpty())
				.map(Integer::parseInt)
				.orElseGet(() -> 1);

		int size = getScanningParameters().getScanpathDocument().getScannableTrackDocuments().size();
		var trackDocumentsPoints = new int[size];
		Arrays.fill(trackDocumentsPoints, numPoints);
		scannableTrackDocumentHelper.updateScannableTrackDocumentsPoints(trackDocumentsPoints);
		SpringApplicationContextFacade.publishEvent(new ScanningAcquisitionChangeEvent(this));
	}

	private void bindElements() {
		// Nothing to do
	}

	private void initialiseElements() {
		projections.setText(Integer.toString(getScannableTrackDocument().getPoints()));
	}

	private  void addWidgetsListener() {
		projections.addModifyListener(this::totalProjectionsListener);
	}

	private ScannableTrackDocument getScannableTrackDocument() {
		List<ScannableTrackDocument> tracks = getScanningAcquisitionTemporaryHelper()
				.getScanpathDocument()
				.map(ScanpathDocument::getScannableTrackDocuments)
				.orElseGet(ArrayList::new);

		if (!tracks.isEmpty()) {
			return tracks.get(0);
		}
		throw new NoSuchElementException("No track document available");
	}

	private ScanningParameters getScanningParameters() {
		return getScanningAcquisitionTemporaryHelper()
				.getScanningParameters()
				.orElseThrow();
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}
