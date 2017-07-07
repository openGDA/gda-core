/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.roi;

/**
 * This is the class used to index which of the ROIs should be used for the ROI+Stats pair
 *
 * @author James Mudd
 */
public class LiveStreamRoiIndexer implements RectangularROIProvider<Integer> {

	/** The object which provides all the ROIs remotely from the client*/
	private LiveStreamROIProvider liveStreamRoiProvider;

	/** The ROI index zero based */
	private int index;

	@Override
	public RectangularROI<Integer> getRoi() {
		return liveStreamRoiProvider.getRois().get(index);
	}

	public LiveStreamROIProvider getLiveStreamRoiProvider() {
		return liveStreamRoiProvider;
	}

	public void setLiveStreamRoiProvider(LiveStreamROIProvider liveStreamRoiProvider) {
		this.liveStreamRoiProvider = liveStreamRoiProvider;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
