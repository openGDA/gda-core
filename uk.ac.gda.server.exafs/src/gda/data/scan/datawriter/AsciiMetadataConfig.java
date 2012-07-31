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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Bean which describes a single header line used by AsciiDataWriterConfiguration
 */
public class AsciiMetadataConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(AsciiMetadataConfig.class);

	String label = "";
	
	Scannable[] labelValues;

	@Override
	public String toString() {
		
		try {
			if (labelValues == null || labelValues.length == 0){
				return label;
			}
			
			Object[] positions = new Object[labelValues.length];
			
			for (int i = 0; i < labelValues.length; i++){
				try {
					positions[i] = labelValues[i].getPosition();
					if (positions[i] instanceof Short || positions[i] instanceof Integer) {
						positions[i] = Double.parseDouble(positions[i].toString());
					}
					
				} catch (DeviceException e) {
					logger.error("DeviceException whilst fetching position from " + labelValues[i].getName() + " to add to metadata in file." +
							"\nIf this problem persists then the object should be reconfigured or removed from the metadata configuration.",e);
					positions[i] = "";
				}
			}
			
			return String.format(label, positions);
			
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
