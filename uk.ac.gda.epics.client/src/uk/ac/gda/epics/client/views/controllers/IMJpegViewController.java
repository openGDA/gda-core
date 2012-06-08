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
public interface IMJpegViewController {

	void updateMJpegNDArrayPort(String ndArrayPort);

	void updateMJpegX(int mJpegX);

	void updateMJpegY(int mJpegY);

	void updateMJpegTimeStamp(double mJpegTimestamp);

	public class Stub implements IMJpegViewController {

		@Override
		public void updateMJpegNDArrayPort(String ndArrayPort) {

		}

		@Override
		public void updateMJpegX(int mJpegX) {

		}

		@Override
		public void updateMJpegY(int mJpegY) {

		}

		@Override
		public void updateMJpegTimeStamp(double mJpegTimestamp) {

		}

	}
}
