/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.rcp.ncd.calibration.views;

import java.util.Collection;

import gda.rcp.ncd.calibration.CalibrationSet;
import uk.ac.gda.server.ncd.calibration.CalibrationEdge;

public interface BraggCalibrationModel {
	/** Get the list of edges the server knows about */
	Collection<CalibrationSet> availableEdges();

	/** Start a scan of the specified edges on the server */
	void scanAll(Collection<CalibrationEdge> edges);

	/** The edge that should be featured in views showing a single edge */
	void setSelectedEdge(CalibrationSet calibration);
	/** The edges that should be included in the calibration process */
	void setActiveEdges(Collection<CalibrationSet> edges);
	/** Notify listeners of new scan data */
	void newScanData(CalibrationSet edge);

	/** Get the currently selected edge */
	CalibrationSet getSelectedEdge();
	/** Get the currently active edges */
	Collection<CalibrationSet> getActiveEdges();

	/** Add a listener that should receive bragg calibration events */
	void addListener(CalibrationListener listener);
	/** Remove listeners that should be notified of calibration events */
	void removeListener(CalibrationListener listener);

	String braggDataPath();
	String exafsPath();

	public interface CalibrationListener {
		default void activeEdgeChanged(@SuppressWarnings("unused") Collection<CalibrationSet> activeEdges) {}
		default void selectedEdgeChanged(@SuppressWarnings("unused") CalibrationSet selected) {}
		default void newScanData(@SuppressWarnings("unused") CalibrationSet calibration) {}
		default void featureChanged() {}
	}

	/** Report that a feature has been updated so that other listeners can be updated */
	void featureChanged();

	/** Check if the given intercept value should be allowed */
	boolean checkInterceptValue(double intercept);

	/** Set the new intercept value based on the calibration */
	void setNewInterceptValue(double intercept);
}
