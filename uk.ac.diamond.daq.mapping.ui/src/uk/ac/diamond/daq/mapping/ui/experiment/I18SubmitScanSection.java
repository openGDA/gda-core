/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;

public class I18SubmitScanSection  extends SubmitScanSection {
	private static final Logger log = LoggerFactory.getLogger(I18SubmitScanSection.class);

	@Override
	protected void submitScan() {
		IMappingExperimentBean mappingBean = super.getBean();
		try {
			Map<String, Object> config = (mappingBean.getBeamlineConfiguration()==null)?new HashMap<String, Object>() : mappingBean.getBeamlineConfiguration();
			if(!config.keySet().contains("t1z")) {
				IScannable<?> scannable = getView().getScannableDeviceService().getScannable("t1z");
				// Set the t1z position to the current position in the mapping bean
				// if it has not already been defined by the user
				config.put("t1z", scannable.getPosition());
				mappingBean.setBeamlineConfiguration(config);
			}

		} catch (ScanningException e) {
			log.warn("Unable to set the t1z position to the current position - the t1z position at scan start will be used instead");
		}
		super.submitScan();
	}

}
