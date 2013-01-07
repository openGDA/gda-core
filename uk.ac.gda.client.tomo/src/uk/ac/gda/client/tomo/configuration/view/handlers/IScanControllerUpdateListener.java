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
package uk.ac.gda.client.tomo.configuration.view.handlers;

public interface IScanControllerUpdateListener {

	public void updateMessage(String message);

	public void updateScanProgress(double progress);

	public void updateError(Exception exception);

	public void updateExposureTime(double exposureTime);

	public void isScanRunning(boolean isScanRunning, String runningConfigId);

	public class Stub implements IScanControllerUpdateListener {

		@Override
		public void updateMessage(String message) {
		}

		@Override
		public void updateScanProgress(double progress) {
		}

		@Override
		public void updateError(Exception exception) {
		}

		@Override
		public void updateExposureTime(double exposureTime) {
		}

		@Override
		public void isScanRunning(boolean isScanRunning, String runningConfigId) {
		}

	}

}