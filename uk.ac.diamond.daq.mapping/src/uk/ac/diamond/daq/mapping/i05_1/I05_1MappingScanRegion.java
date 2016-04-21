/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.i05_1;


public class I05_1MappingScanRegion {

	private I05_1ScanPath i05_ScanPath = new I05_1ScanPath();
	private I05_1MappingRegion region = new I05_1MappingRegion();

	public I05_1MappingRegion getRegion() {
		return region;
	}

	public void setRegion(I05_1MappingRegion region) {
		this.region = region;
	}

	public I05_1ScanPath getScanPath() {
		return i05_ScanPath;
	}

	public void setScanPath(I05_1ScanPath i05_ScanPath) {
		this.i05_ScanPath = i05_ScanPath;
	}

}
