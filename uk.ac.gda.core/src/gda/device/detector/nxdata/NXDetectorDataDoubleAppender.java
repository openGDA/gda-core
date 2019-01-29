/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdata;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NXDetectorDataDoubleAppender extends NXDetectorDataTAppender<Double> {

	private static final Logger logger = LoggerFactory.getLogger(NXDetectorDataDoubleAppender.class);

	public NXDetectorDataDoubleAppender(List<String> elementNames, List<Double> elementValues) {
		super(elementNames, elementValues);
	}

	@Deprecated
	public NXDetectorDataDoubleAppender(List<String> elementNames, List<Double> elementValues, List<String> elementUnits) {
		super(elementNames, elementValues);
		logger.warn("Units not supported, ignoring elementUnits: {}", elementUnits);
	}

}
