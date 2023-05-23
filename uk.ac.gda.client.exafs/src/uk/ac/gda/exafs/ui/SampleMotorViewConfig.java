/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import java.util.Collections;
import java.util.List;

import gda.factory.FindableBase;

public class SampleMotorViewConfig extends FindableBase {

	private List<ConfigDetails> configurations = Collections.emptyList();

	public List<ConfigDetails> getConfigurations() {
		return configurations;
	}
	public void setConfigurations(List<ConfigDetails> configurations) {
		this.configurations = configurations;
	}

	public static class ConfigDetails {

		private String groupName = "";
		private List<String> scannableNames = Collections.emptyList();
		private int columnNumber;

		public String getGroupName() {
			return groupName;
		}
		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}
		public List<String> getScannableNames() {
			return scannableNames;
		}
		public void setScannableNames(List<String> scannableNames) {
			this.scannableNames = scannableNames;
		}
		public int getColumnNumber() {
			return columnNumber;
		}
		public void setColumnNumber(int columnNumber) {
			this.columnNumber = columnNumber;
		}
	}
}