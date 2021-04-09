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

package uk.ac.gda.ui.tool.processing;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.configuration.processing.ApplyNexusTemplatesRequest;
import uk.ac.gda.api.acquisition.configuration.processing.DiffractionCalibrationMergeRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestBuilder;
import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Client enumeration used by the {@link ProcessingRequestComposite}.
 *
 * <p>
 * Each enum element collects together enough information for the {@code ProcessingRequestComposite} to display and create a
 * {@link ProcessingRequestBuilder} for the {@link AcquisitionConfiguration#getProcessingRequest()}
 * </p>
 *
 * @author Maurizio Nagni
 */
public enum ProcessingRequestKey {
	APPLY_NEXUS_TEMPLATE(ApplyNexusTemplatesRequest.KEY,
			ClientMessages.APPLY_NEXUS_TEMPLATE,
			ClientMessages.APPLY_NEXUS_TEMPLATE_TP,
			ApplyNexusTemplatesRequest.Builder.class),
	DIFFRACTION_CALIBRATION_MERGE(DiffractionCalibrationMergeRequest.KEY,
			ClientMessages.DIFFRACTION_CALIBRATION_MERGE,
			ClientMessages.DIFFRACTION_CALIBRATION_MERGE_TP,
			DiffractionCalibrationMergeRequest.Builder.class),
	SAVU(SavuProcessingRequest.KEY,
			ClientMessages.SAVU,
			ClientMessages.SAVU_TP,
			SavuProcessingRequest.Builder.class);

	private static final Logger logger = LoggerFactory.getLogger(ProcessingRequestKey.class);
	private final String key;
	private final ClientMessages label;
	private final ClientMessages tooltip;
	private final Class<? extends ProcessingRequestBuilder<URL>> builder;
	private ProcessingRequestKey(String key, ClientMessages label, ClientMessages tooltip, Class<? extends ProcessingRequestBuilder<URL>> builder) {
		this.key = key;
		this.label = label;
		this.builder = builder;
		this.tooltip = tooltip;
	}
	public String getKey() {
		return key;
	}
	public ClientMessages getLabel() {
		return label;
	}
	public ClientMessages getTooltip() {
		return tooltip;
	}
	public Class<? extends ProcessingRequestBuilder<URL>> getBuilder() {
		return builder;
	}
}