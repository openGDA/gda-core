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

package uk.ac.gda.ui.tool.document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.helper.ScannableTrackDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Temporary class to collect together the most used getters from the active {@link AcquisitionController#getAcquisition()}.
 * <p>
 * The goal of this class is to reduce the duplication of similar getters but at the same time is a temporary solution
 * before consolidate it with classes from {@code uk.ac.diamond.daq.mapping.api.document.helper} package.
 * </p>
 * <p>
 * Except for {@link #getScannableTrackDocuments()}, the crucial characteristic of these methods is to return {@link Optional} objects.
 * </p>
 *
 * @author Maurizio Nagni
 */
@Component
public class ScanningAcquisitionTemporaryHelper {

	@Autowired
	private ClientSpringContext clientSpringContext;

	private ClientSpringContext getClientSpringContext() {
		return clientSpringContext;
	}

	public Optional<AcquisitionController<ScanningAcquisition>> getAcquisitionController() {
		return getClientSpringContext().getAcquisitionController();
	}

	public Optional<ScanningAcquisition> getScanningAcquisition() {
		return getAcquisitionController()
				.map(AcquisitionController<ScanningAcquisition>::getAcquisition);
	}

	public Optional<ScanningParameters> getScanningParameters() {
		return getScanningAcquisition()
				.map(ScanningAcquisition::getAcquisitionConfiguration)
				.map(ScanningConfiguration::getAcquisitionParameters);
	}

	public Optional<ScanningConfiguration> getAcquisitionConfiguration() {
		return getScanningAcquisition()
				.map(ScanningAcquisition::getAcquisitionConfiguration);
	}

	public Optional<ScanpathDocument> getScanpathDocument() {
		return getScanningParameters()
				.map(ScanningParameters::getScanpathDocument);
	}

	/**
	 * Returns the existing {@link ScannableTrackDocument}s
	 *
	 * @return a {@code List}, eventually {@link Collections#emptyList()}
	 */
	public List<ScannableTrackDocument> getScannableTrackDocuments() {
		return getScanpathDocument()
				.map(ScanpathDocument::getScannableTrackDocuments)
				.orElseGet(Collections::emptyList);
	}

	public Optional<AcquisitionTemplateType> getSelectedAcquisitionTemplateType() {
		return getScanpathDocument()
			.map(ScanpathDocument::getModelDocument);
	}

	/**
	 * Creates a new {@link ScannableTrackDocumentHelper} if {@link #getAcquisitionConfiguration()} is present.
	 *
	 * @return a new @code{Optional} {@link ScannableTrackDocumentHelper}
	 */
	public Optional<ScannableTrackDocumentHelper> createScannableTrackDocumentHelper() {
		return getAcquisitionConfiguration()
				.map(s -> new ScannableTrackDocumentHelper(s::getAcquisitionParameters));
	}
}
