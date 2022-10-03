/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.eclipse.scanning.malcolm.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.points.IPointGenerator;

/**
 * Instances of {@link EpicsMalcolmModel} are used to configure the actual malcolm device.
 * This class should be distinguished from the {@link IMalcolmModel} that this
 * class is configured with. The majority of the information contained in
 * an {@link EpicsMalcolmModel} describes the scan, e.g. the scan path
 * defined by the {@link IPointGenerator} and the directory to write to.
 * <p>
 * Note: the field names within this class are as required by malcolm.
 * They cannot be changed without breaking their deserialization by malcolm.
 */
public final class EpicsMalcolmModel {

	/**
	 * A point generator describing the scan path.
	 */
	private final IPointGenerator<?> generator;

	/**
	 * The axes of the scan that malcolm should control. Can be <code>null</code>
	 * in which case malcolm will control all the axes that it knows about.
	 */
	private final List<String> axesToMove;

	/**
	 * The directory in which malcolm should write its files for the scan,
	 * typically something like e.g. {@code /dls/ixx/data/2018/cm12345-1/ixx-123456/}
	 * where the final segment identifies the scan.
	 */
	private final String fileDir;

	/**
	 * A file template for the files that malcolm creates, e.g.
	 * {@code ixx-123456-%s.h5}, where '%s' is the placeholder for malcolm to
	 * insert the device name, e.g. {@code PANDABOX}.
	 */
	private final String fileTemplate;

	/**
	 * A {@link MalcolmTable} to configure the detectors controlled by malcolm.
	 * The column headings are 'name', 'mri', 'exposure' and 'framesPerStep'.
	 */
	private final MalcolmTable detectors;

	private final int[] breakpoints;

	public EpicsMalcolmModel(String fileDir, String fileTemplate,
			List<String> axesToMove, IPointGenerator<?> generator,
			MalcolmTable detectors, int[] breakpoints) {
		this.fileDir = fileDir;
		this.fileTemplate = fileTemplate;
		this.axesToMove = axesToMove;
		this.generator = generator;
		this.detectors = detectors;
		this.breakpoints = breakpoints;
	}

	public String getFileDir() {
		return fileDir;
	}

	public String getFileTemplate() {
		return fileTemplate;
	}

	public List<String> getAxesToMove() {
		return axesToMove;
	}

	public IPointGenerator<?> getGenerator() {
		return generator;
	}

	public MalcolmTable getDetectors() {
		return detectors;
	}

	public int[] getBreakpoints() {
		return breakpoints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axesToMove == null) ? 0 : axesToMove.hashCode());
		result = prime * result + ((fileDir == null) ? 0 : fileDir.hashCode());
		result = prime * result + ((fileTemplate == null) ? 0 : fileTemplate.hashCode());
		result = prime * result + ((generator == null) ? 0 : generator.hashCode());
		result = prime * result + ((detectors == null) ? 0 : detectors.hashCode());
		result = prime * result + Arrays.hashCode(breakpoints);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EpicsMalcolmModel other = (EpicsMalcolmModel) obj;
		if (axesToMove == null) {
			if (other.axesToMove != null)
				return false;
		} else if (!axesToMove.equals(other.axesToMove))
			return false;
		if (fileDir == null) {
			if (other.fileDir != null)
				return false;
		} else if (!fileDir.equals(other.fileDir))
			return false;
		if (fileTemplate == null) {
			if (other.fileTemplate != null)
				return false;
		} else if (!fileTemplate.equals(other.fileTemplate))
			return false;
		if (generator == null) {
			if (other.generator != null)
				return false;
		} else if (!generator.equals(other.generator))
			return false;
		if (detectors == null)
			if (other.detectors != null)
				return false;
		if (!Arrays.equals(breakpoints, other.breakpoints))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EpicsMalcolmModel [generator=" + generator + ", axesToMove=" + axesToMove + ", fileDir=" + fileDir
				+ ", fileTemplate=" + fileTemplate + ", detectors=" + detectors + ", breakpoints="
				+ Arrays.toString(breakpoints) + "]";
	}

}