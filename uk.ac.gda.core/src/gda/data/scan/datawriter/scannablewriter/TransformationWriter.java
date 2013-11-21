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

import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class TransformationWriter extends SingleScannableWriter {

	protected String[] depends_on;
	protected Double[][] vector;
	protected String[] transformation;
	protected Double[] offset;
	protected String[] offset_units;
	
	class TransformationComponentWriter extends DefaultComponentWriter {
		
		private int index;

		public TransformationComponentWriter(int i) {
			this.index = i;
		}
		
		@Override
		protected void addCustomAttributes(NeXusFileInterface file, String scannableName, String componentName) throws NexusException {
			super.addCustomAttributes(file, scannableName, componentName);
			file.putattr("transformation", transformation[index].getBytes(), NexusFile.NX_CHAR);
			if (depends_on[index] != null)
				file.putattr("depends_on", depends_on[index].getBytes(), NexusFile.NX_CHAR);
			if (offset_units[index] != null)
				file.putattr("offset_units", offset_units[index].getBytes(), NexusFile.NX_CHAR);
			if (vector[index] != null) // TODO this needs to be an array, but we need to change NAPI for that 
				file.putattr("vector", String.format("%5.5g, %5.5g, %5g5", vector[index][0],vector[index][1], vector[index][2]).getBytes(), NexusFile.NX_CHAR);
			if (offset[index] != null)
				file.putattr("offset", new double[] {offset[index]}, NexusFile.NX_FLOAT64);
		}
	}

	@Override
	protected ComponentWriter getComponentWriter(Scannable s, String componentName, Object object) {
		int index = indexForcomponentName(s, componentName);
		if (transformation != null && transformation.length > index) {
			TransformationComponentWriter cw = new TransformationComponentWriter(index);
			cwriter.put(componentName, cw);
			return cw;
		}
		
		return super.getComponentWriter(s, componentName, object);
	}

	public String[] getDepends_on() {
		return depends_on;
	}

	public void setDepends_on(String[] depends_on) {
		this.depends_on = depends_on;
	}

	public Double[][] getVector() {
		return vector;
	}

	public void setVector(Double[][] vector) {
		this.vector = vector;
	}

	public String[] getTransformation() {
		return transformation;
	}

	public void setTransformation(String[] transformation) {
		this.transformation = transformation;
	}

	public Double[] getOffset() {
		return offset;
	}

	public void setOffset(Double[] offset) {
		this.offset = offset;
	}

	public String[] getOffset_units() {
		return offset_units;
	}

	public void setOffset_units(String[] offset_units) {
		this.offset_units = offset_units;
	}	
}