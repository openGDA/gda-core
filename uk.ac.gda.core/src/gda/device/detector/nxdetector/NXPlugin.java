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

package gda.device.detector.nxdetector;

import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.scannable.PositionInputStream;
import gda.scan.ScanInformation;

import java.util.List;

public interface NXPlugin extends PositionInputStream<NXDetectorDataAppender> {

	String getName(); // For naming within the context of the ADDetector instance. e.g. from Jython: pilatus.roi1

	boolean willRequireCallbacks();

	void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception;

	void prepareForLine() throws Exception;

	void completeLine() throws Exception;

	void completeCollection() throws Exception;

	void atCommandFailure() throws Exception;

	void stop() throws Exception;

	List<String> getInputStreamNames();

	List<String> getInputStreamFormats();

	/**
	 * 
	 * @return True if the read method at any time. Return False if the read method must be called before the next detector exposure in a scan
	 * 
	 * Return False if the data must be read from source , e.g. hardware, before starting the next exposure.
	 */
	boolean supportsAsynchronousRead();

}
