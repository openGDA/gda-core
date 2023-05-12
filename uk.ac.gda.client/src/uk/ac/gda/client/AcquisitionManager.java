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

package uk.ac.gda.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.osgi.OsgiService;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplate;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.document.DocumentFactory;

@OsgiService(AcquisitionManager.class)
public class AcquisitionManager {

	private Map<AcquisitionKeys, ScanningAcquisition> acquisitions;

	private final List<AcquisitionTemplate> templates;

	public AcquisitionManager(List<AcquisitionTemplate> templates) {
		this.templates = templates;
		acquisitions = new HashMap<>();
	}

	/**
	 * Returns the most recent acquisition corresponding to the given key,
	 * or one created from template if no recent acquisition found
	 */
	public ScanningAcquisition getAcquisition(AcquisitionKeys key) {
		return acquisitions.computeIfAbsent(key, this::acquisitionFromTemplate);
	}

	public ScanningAcquisition newAcquisition(AcquisitionKeys key) {
		ScanningAcquisition acquisition;
		if (acquisitions.containsKey(key)) {
			acquisition = acquisitionFromParent(acquisitions.get(key));
		} else {
			acquisition = acquisitionFromTemplate(key);
		}
		register(acquisition);
		return acquisition;
	}

	/** A client that obtains an acquisition externally can register it here for later retrieval */
	public void register(ScanningAcquisition acquisition) {
		acquisitions.put(acquisition.getKey(), acquisition);
	}

	public List<ScannablePropertiesValue> getStartPosition(AcquisitionKeys key) {
		return getTemplate(key).getStartPosition();
	}

	private ScanningAcquisition acquisitionFromParent(ScanningAcquisition parent) {
		return getDocumentFactory().newScanningAcquisition(parent);
	}

	private ScanningAcquisition acquisitionFromTemplate(AcquisitionKeys key) {
		var acquisition = getDocumentFactory().newScanningAcquisition(getTemplate(key));
		acquisition.setKey(key);
		return acquisition;
	}

	private AcquisitionTemplate getTemplate(AcquisitionKeys key) {
		return templates.stream()
				.filter(template -> template.getType() == key.getPropertyType())
				.filter(template -> template.getSubType() == key.getSubType())
				.findFirst().orElseThrow(() -> new MissingResourceException("Acquisition template not configured",
													AcquisitionTemplate.class.getCanonicalName(), key.toString()));
	}

	private DocumentFactory getDocumentFactory() {
		return SpringApplicationContextFacade.getBean(DocumentFactory.class);
	}
}
