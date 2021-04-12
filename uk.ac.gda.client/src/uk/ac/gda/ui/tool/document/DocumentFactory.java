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

import static uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType.ONE_DIMENSION_LINE;
import static uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType.TWO_DIMENSION_POINT;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.AcquisitionType;
import uk.ac.gda.client.properties.acquisition.AcquisitionTypeProperties;

/**
 * Creates default acquisition documents.
 *
 * @author Maurizio Nagni
 */
@Component
public class DocumentFactory {

	private final Map<AcquisitionTemplateType, ScanningAcquisitionFactory> defaultFactories = new EnumMap<>(AcquisitionTemplateType.class);

	@PostConstruct
    private void postConstruct() {
		defaultFactories.put(ONE_DIMENSION_LINE, new OneDimensionLine());
		defaultFactories.put(TWO_DIMENSION_POINT, new TwoDimensionPoint());
    }

	/**
	 * Creates a new acquisition document.
	 *
	 * @param templateType the template type type
	 * @param acquisitionType the acquisition type. See {@link AcquisitionTypeProperties#getAcquisitionProperties(String)}
	 *
	 * @return a new supplier either providing a new default document or an empty {@code ScanningAcquisition}
	 *
	 */
	public final Supplier<ScanningAcquisition> newScanningAcquisition(AcquisitionTemplateType templateType, String acquisitionType) {
		if (!defaultFactories.containsKey(templateType))
				return ScanningAcquisition::new;
		return () -> {
			ScanningAcquisition acquisition = defaultFactories.get(templateType).newScanningAcquisition(acquisitionType).get();

			acquisition.setType(Stream.of(AcquisitionType.values())
					.filter(type -> type.getName().equals(acquisitionType))
					.findFirst().orElse(AcquisitionType.GENERIC));

			return acquisition;
		};
	}
}