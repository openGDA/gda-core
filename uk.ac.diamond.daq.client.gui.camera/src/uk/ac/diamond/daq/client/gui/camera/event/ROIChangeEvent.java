/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.event;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.IDataset;

/**
 *  This event is published when a ROI is created or edited. 
 *
 * @author Maurizio Nagni
 */
public class ROIChangeEvent extends CameraEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4324209303234122825L;
	private final RectangularROI roi;
	private final IDataset dataset;
	
    /**
     * Creates an instance for this class
     * 
     * @param source the Object which published this event
     * @param roi the ROI associated with this event
     * @param dataset the dataset over which the ROI has been selected
     */
    public ROIChangeEvent(Object source, RectangularROI roi, IDataset dataset) {
		super(source);
		this.roi = roi;
		this.dataset = dataset;
	}

	public RectangularROI getRoi() {
		return roi;
	}

	public IDataset getDataset() {
		return dataset;
	}
}
