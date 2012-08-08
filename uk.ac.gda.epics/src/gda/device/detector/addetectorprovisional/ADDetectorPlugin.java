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

package gda.device.detector.addetectorprovisional;

import java.util.List;

import gda.device.scannable.PositionInputStream;


public interface ADDetectorPlugin<T> extends PositionInputStream<T> {
	
	String getName(); // For naming within the context of the ADDetecoctor instance. e.g. from Jython: pilatus.roi1
	
	boolean willRequireCallbacks();

	List<String> getInputStreamFieldNames();

	List<String> getInputStreamFormats();

	void prepareForCollection(int numberImagesPerCollection) throws Exception;
	
	void prepareForLine() throws Exception;

	void completeLine() throws Exception;

	void completeCollection() throws Exception;
	
	void atCommandFailure() throws Exception;

	void stop() throws Exception;
	
	
	
}
