/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter.scannablewriter;

import gda.data.nexus.NexusException;
import gda.data.nexus.NexusFileInterface;
import gda.data.nexus.NexusGlobals;
import gda.device.Scannable;

public class TransformationWriter extends SingleScannableWriter {

	private String[] dependsOn;
	private Double[][] vector;
	private String[] transformation;
	private Double[][] offset;
	private String[] offsetUnits;

	public class TransformationComponentWriter extends NumberComponentWriter {

		private final int index;

		public TransformationComponentWriter(final int i) {
			this.index = i;
		}

		@Override
		protected void addCustomAttributes(final NexusFileInterface file, final String scannableName,
				final String componentName) throws NexusException {

			super.addCustomAttributes(file, scannableName, componentName);
			file.putattr("transformation_type", transformation[index].getBytes(), NexusGlobals.NX_CHAR);
			if (dependsOn != null && dependsOn[index] != null) {
				file.putattr("depends_on", dependsOn[index].getBytes(), NexusGlobals.NX_CHAR);
			}
			if (offsetUnits != null && offsetUnits[index] != null) {
				file.putattr("offset_units", offsetUnits[index].getBytes(), NexusGlobals.NX_CHAR);
			}
			if (vector != null && vector[index] != null) {
				file.putattr("vector", vector[index], new int[] { vector[index].length }, NexusGlobals.NX_FLOAT64);
			}
			if (offset != null && offset[index] != null) {
				file.putattr("offset", offset[index], new int[] { offset[index].length }, NexusGlobals.NX_FLOAT64);
			}
		}
	}

	@Override
	protected ComponentWriter getComponentWriter(final Scannable s, final String componentName, final Object object) {
		final int index = indexForComponentName(s, componentName);
		if (transformation != null && transformation.length > index) {
			final TransformationComponentWriter cw = new TransformationComponentWriter(index);
			getCwriter().put(componentName, cw);
			return cw;
		}

		return super.getComponentWriter(s, componentName, object);
	}

	@Deprecated
	public String[] getDepends_on() {
		return getDependsOn();
	}

	@Deprecated
	public void setDepends_on(final String[] dependsOn) {
		setDependsOn(dependsOn);
	}

	public String[] getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(final String[] dependsOn) {
		this.dependsOn = dependsOn;
	}

	public Double[][] getVector() {
		return vector;
	}

	public void setVector(final Double[][] vector) {
		this.vector = vector;
	}

	public String[] getTransformation() {
		return transformation;
	}

	public void setTransformation(final String[] transformation) {
		this.transformation = transformation;
	}

	public Double[][] getOffset() {
		return offset;
	}

	public void setOffset(final Double[][] offset) {
		this.offset = offset;
	}

	@Deprecated
	public String[] getOffset_units() {
		return getOffsetUnits();
	}

	@Deprecated
	public void setOffset_units(final String[] offsetUnits) {
		setOffsetUnits(offsetUnits);
	}

	public String[] getOffsetUnits() {
		return offsetUnits;
	}

	public void setOffsetUnits(final String[] offsetUnits) {
		this.offsetUnits = offsetUnits;
	}
}
