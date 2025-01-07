/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

/**
 * Bean which describes a single header line used by AsciiDataWriterConfiguration
 */
public class AsciiMetadataConfig {

	private static final Logger logger = LoggerFactory.getLogger(AsciiMetadataConfig.class);
	private String label = "";
	private Scannable[] labelValues;

	@Override
	public String toString() {
		try {
			if (labelValues == null || labelValues.length == 0) {
				return label;
			}

			List<Object> positionsList = new ArrayList<>();
			for (var scn : labelValues){
				try {
					if (label == null){
						positionsList.add("null");
					} else {
						Object scnPosition = scn.getPosition();
						if (scnPosition.getClass().isArray()) {
							Object[] posArray = ScannableUtils.toObjectArray(scnPosition);
							positionsList.addAll(Arrays.asList(posArray));
						} else if (scnPosition instanceof Short || scnPosition instanceof Integer) {
							positionsList.add(Double.parseDouble(scnPosition.toString()));
						} else {
							positionsList.add(scnPosition);
						}
					}
				} catch (DeviceException e) {
					logger.error("Cannot give correct value for AsciiMetadataConfig item as DeviceException whilst fetching position from " + scn.getName() + " to add to metadata in file." + "\nIf this problem persists then the object should be reconfigured or removed from the metadata configuration.",e);
					positionsList.add("");

					// if get here then do not do the String.format line below
					StringBuilder namesOnly = new StringBuilder();
					for (Scannable scannable : labelValues) {
						namesOnly.append(scannable.getName() + "\t");
					}
					return namesOnly.toString();
				}
			}
			return String.format(label, positionsList.toArray());
			// Should not throw this Exception, it causes the scan to die when some hardware
			// that is not critical is not there. Instead we return the exception string and
			// log the exception.
		} catch (Exception ne) {
			logger.error("Cannot format", ne);
			return ne.getMessage();
		}

	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return Returns the labelValues.
	 */
	public Scannable[] getLabelValues() {
		return labelValues;
	}

	/**
	 * @param labelValues The labelValues to set.
	 */
	public void setLabelValues(Scannable[] labelValues) {
		this.labelValues = labelValues;
	}
}
