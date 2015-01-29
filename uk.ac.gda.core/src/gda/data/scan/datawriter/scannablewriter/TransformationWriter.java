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

import gda.device.Scannable;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class TransformationWriter extends SingleScannableWriter {

	private String[] dependsOn;
	private Double[][] vector;
	private String[] transformation;
	private Double[] offset;
	private String[] offsetUnits;

	public class TransformationComponentWriter extends NumberComponentWriter {

		private final int index;

		public TransformationComponentWriter(final int i) {
			this.index = i;
		}

		@Override
		protected void addCustomAttributes(final NeXusFileInterface file, final String scannableName,
				final String componentName) throws NexusException {

			super.addCustomAttributes(file, scannableName, componentName);

			file.putattr("transformation", getTransformation()[index].getBytes(), NexusFile.NX_CHAR);
			if (getDependsOn()[index] != null) {
				file.putattr("depends_on", getDependsOn()[index].getBytes(), NexusFile.NX_CHAR);
			}
			if (getOffsetUnits()[index] != null) {
				file.putattr("offset_units", getOffsetUnits()[index].getBytes(), NexusFile.NX_CHAR);
			}
			if (getVector()[index] != null) {
				file.putattr(
						"vector",
						String.format("%5.5g, %5.5g, %5g5", getVector()[index][0], getVector()[index][1],
								getVector()[index][2]).getBytes(), NexusFile.NX_CHAR);
			}
			if (getOffset()[index] != null) {
				file.putattr("offset", new double[] { getOffset()[index] }, NexusFile.NX_FLOAT64);
			}
		}
	}

	@Override
	protected ComponentWriter getComponentWriter(final Scannable s, final String componentName, final Object object) {
		final int index = indexForcomponentName(s, componentName);
		if (getTransformation() != null && getTransformation().length > index) {
			final TransformationComponentWriter cw = new TransformationComponentWriter(index);
			getCwriter().put(componentName, cw);
			return cw;
		}

		return super.getComponentWriter(s, componentName, object);
	}

	private final int indexForcomponentName(final Scannable s, final String component) {
		final String[] all = (String[]) ArrayUtils.addAll((s.getInputNames() != null) ? s.getInputNames()
				: new String[] {}, (s.getExtraNames() != null) ? s.getExtraNames() : new String[] {});

		for (int i = 0; i < all.length; i++) {
			if (component.equals(all[i])) {
				return i;
			}
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	@Deprecated
	public String[] getDepends_on() {
		return getDependsOn();
	}

	public String[] getDependsOn() {
		return dependsOn;
	}

	@Deprecated
	public final void setDepends_on(final String[] depends_on) {
		setDependsOn(depends_on);
	}

	public final void setDependsOn(final String[] dependsOn) {
		this.dependsOn = dependsOn;
	}

	public Double[][] getVector() {
		return vector;
	}

	public final void setVector(final Double[][] vector) {
		this.vector = vector;
	}

	public String[] getTransformation() {
		return transformation;
	}

	public final void setTransformation(final String[] transformation) {
		this.transformation = transformation;
	}

	public Double[] getOffset() {
		return offset;
	}

	public final void setOffset(final Double[] offset) {
		this.offset = offset;
	}

	@Deprecated
	public final String[] getOffset_units() {
		return getOffsetUnits();
	}

	public String[] getOffsetUnits() {
		return offsetUnits;
	}

	@Deprecated
	public final void setOffset_units(final String[] offset_units) {
		setOffsetUnits(offset_units);
	}

	public final void setOffsetUnits(final String[] offsetUnits) {
		this.offsetUnits = offsetUnits;
	}
}
