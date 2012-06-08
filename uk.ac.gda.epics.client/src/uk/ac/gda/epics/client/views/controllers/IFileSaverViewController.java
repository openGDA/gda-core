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
public interface IFileSaverViewController {

	void updateFileSaverCaptureState(short fileSaverCaptureState);

	void updateFileSaveX(int fileSaveX);

	void updateFileSaveY(int fileSaveY);

	void updateFileSaveTimeStamp(double fileSaveTimeStamp);

	public class Stub implements IFileSaverViewController {

		@Override
		public void updateFileSaverCaptureState(short fileSaverCaptureState) {

		}

		@Override
		public void updateFileSaveX(int fileSaveX) {
		}

		@Override
		public void updateFileSaveY(int fileSaveY) {
		}

		@Override
		public void updateFileSaveTimeStamp(double fileSaveTimeStamp) {

		}
	}
}