/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiFilename;

/**
 * Defines the script files to be run before and/or after a scan.
 */
public interface IScriptFiles {

	public void setBeforeScanScript(String beforeScanScript);

	/**
	 * @return the script to run before a scan
	 */
	@UiFilename
	public String getBeforeScanScript();

	public void setAfterScanScript(String afterScanScript);

	/**
	 * @return the script to run after a scan
	 */
	@UiFilename
	@UiComesAfter("beforeScanScript")
	public String getAfterScanScript();

	public void setAlwaysRunAfterScript(boolean alwaysRunAfterScript);

	/**
	 * @return true if the "after scan" script should always be run, even if there is an error
	 */
	public boolean isAlwaysRunAfterScript();

}
