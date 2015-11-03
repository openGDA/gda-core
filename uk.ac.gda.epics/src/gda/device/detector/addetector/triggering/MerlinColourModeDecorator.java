/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

public final class MerlinColourModeDecorator extends AbstractCollectionStrategyDecorator {

	private int imagesPerCollectionMultiplier = 1;

	public MerlinColourModeDecorator() {
	}

	/** When in Colour mode, the Merlin detector returns multiple images for each acquisition.
	 */

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return getDecoratee().getNumberImagesPerCollection(collectionTime)*imagesPerCollectionMultiplier;
	}

	/* Getters and setters for private fields. */

	public int getImagesPerCollectionMultiplier() {
		return imagesPerCollectionMultiplier;
	}

	public void setImagesPerCollectionMultiplier(int imagesPerCollectionMultiplier) {
		this.imagesPerCollectionMultiplier = imagesPerCollectionMultiplier;
	}
}
