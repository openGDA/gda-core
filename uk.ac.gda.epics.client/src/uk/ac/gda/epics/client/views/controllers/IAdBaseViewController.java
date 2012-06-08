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

package uk.ac.gda.epics.client.views.controllers;

/**
 *
 */
public interface IAdBaseViewController {

	void updateAcquireState(short acquisitionState);

	void updateDetectorDataType(String detectorDataType);

	void updateAcqExposure(double acqExposure);

	void updateAcqPeriod(double acqPeriod);

	void updateArrayCounter(int arrayCounter);

	void updateArrayRate(double arrayRate);

	void updateTimeRemaining(double timeRemaining);

	void updateNumberOfExposuresCounter(int numOfExposuresCounter);

	void updateNumberOfImagesCounter(int numberOfImagesCounter);

	void updateDetectorState(short detectorState);

	public class Stub implements IAdBaseViewController {

		@Override
		public void updateAcquireState(short acquisitionState) {

		}

		@Override
		public void updateDetectorDataType(String detectorDataType) {

		}

		@Override
		public void updateAcqExposure(double acqExposure) {

		}

		@Override
		public void updateAcqPeriod(double acqPeriod) {

		}

		@Override
		public void updateArrayCounter(int arrayCounter) {

		}

		@Override
		public void updateArrayRate(double arrayRate) {

		}

		@Override
		public void updateTimeRemaining(double timeRemaining) {

		}

		@Override
		public void updateNumberOfExposuresCounter(int numOfExposuresCounter) {

		}

		@Override
		public void updateNumberOfImagesCounter(int numberOfImagesCounter) {

		}

		@Override
		public void updateDetectorState(short detectorState) {

		}

	}
}
