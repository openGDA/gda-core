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



public class I05_1MappingExperimentBean {

	I05_1SampleMetadata i05_1SampleMetadata = new I05_1SampleMetadata();
	I05_1DetectorParameters i05_1DetectorParameters = new I05_1DetectorParameters();
	I05_1MappingScanRegion i05_1MappingScanRegion = new I05_1MappingScanRegion();
	I05_1BeamlineConfiguration i05_1BeamlineConfiguration = new I05_1BeamlineConfiguration();

	public I05_1SampleMetadata getI05_1SampleMetadata() {
		return i05_1SampleMetadata;
	}

	public void setI05_1SampleMetadata(I05_1SampleMetadata i05_1SampleMetadata) {
		this.i05_1SampleMetadata = i05_1SampleMetadata;
	}

	public I05_1DetectorParameters getI05_1DetectorParameters() {
		return i05_1DetectorParameters;
	}

	public void setI05_1DetectorParameters(I05_1DetectorParameters i05_1DetectorParameters) {
		this.i05_1DetectorParameters = i05_1DetectorParameters;
	}

	public I05_1MappingScanRegion getI05_1MappingScanRegion() {
		return i05_1MappingScanRegion;
	}

	public void setI05_1MappingScanRegion(I05_1MappingScanRegion i05_1MappingScanRegion) {
		this.i05_1MappingScanRegion = i05_1MappingScanRegion;
	}

	public I05_1BeamlineConfiguration getI05_1BeamlineConfiguration() {
		return i05_1BeamlineConfiguration;
	}

	public void setI05_1BeamlineConfiguration(I05_1BeamlineConfiguration i05_1BeamlineConfiguration) {
		this.i05_1BeamlineConfiguration = i05_1BeamlineConfiguration;
	}

}
