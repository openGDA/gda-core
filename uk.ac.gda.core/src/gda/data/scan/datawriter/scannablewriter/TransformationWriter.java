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

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;

import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.NexusDataWriterConfiguration;

/**
 * An instance of this class that when part of the location map for a scannable,
 * will add attributes required by the {@link NXtransformations} nexus class.
 * In order to use this class, declare an instance of this class as the value for the
 * entry for your scannable in the location map in the.
 * Each attribute can take an array of values (or a 2d array for offset and vector),
 * one for each component (i.e. input or extra name) of the scannable.
 * In most cases only one value will be required (or a 1d array
 * {@link NexusDataWriterConfiguration#setLocationMap(java.util.Map)}
 * or {@link NexusDataWriter#setLocationmap(java.util.Map)}.
 * The path(s) attribute define a data node within an {@link NXtransformations} group,
 * e.g.
 * <pre>{@code
 * <entry key="theta">
 *     <bean class="gda.data.scan.datawriter.scannablewriter.TransformationWriter">
 *         <property name="paths"
 *                 value="instrument:NXinstrument/transformations:NXtransformations/theta" />
 *         <property name="depends_on" value="entry1/instrument/transformations/gamma" />
 *         <property name="transformation" value="rotation" />
 *         <property name="units" value="deg" />
 *         <property name="vector">
 *             <list>
 *                 <value>0</value>
 *                 <value>1</value>
 *                 <value>0</value>
 *             </list>
 *         </property>
 *         <property name="offset">
 *             <list>
 *                 <value>2.5</value>
 *                 <value>0.0</value>
 *                 <value>-7.5</value>
 *             </list>
 *         </property>
 *         <property name="offset_units" value="mm"/>
 *     </bean>
 * </entry>}
 *</pre>
 */
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
		protected void addCustomAttributes(final NexusFile file, Node node, final String scannableName,
				final String componentName) throws NexusException {

			super.addCustomAttributes(file, node, scannableName, componentName);
			NexusUtils.writeStringAttribute(file, node, "transformation_type", transformation[index]);
			if (dependsOn != null && dependsOn[index] != null) {
				NexusUtils.writeStringAttribute(file, node, "depends_on", dependsOn[index]);
			}
			if (offsetUnits != null && offsetUnits[index] != null) {
				NexusUtils.writeStringAttribute(file, node, "offset_units", offsetUnits[index]);
			}
			if (vector != null && vector[index] != null) {
				NexusUtils.writeDoubleAttribute(file, node, "vector", vector[index]);
			}
			if (offset != null && offset[index] != null) {
				NexusUtils.writeDoubleAttribute(file, node, "offset", offset[index]);
			}
		}
	}

	@Override
	protected ComponentWriter getComponentWriter(final String componentName, final Object object, int i) {
		if (transformation != null && transformation.length > i) {
			final TransformationComponentWriter cw = new TransformationComponentWriter(i);
			getCwriter().put(componentName, cw);
			return cw;
		}

		return super.getComponentWriter(componentName, object, i);
	}

	@Deprecated
	public String[] getDepends_on() {
		return getDependsOn();
	}

	@Deprecated
	public void setDepends_on(final String... dependsOn) {
		setDependsOn(dependsOn);
	}

	public String[] getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(final String... dependsOn) {
		this.dependsOn = dependsOn;
	}

	public Double[][] getVector() {
		return vector;
	}

	public void setVector(final Double[]... vector) {
		this.vector = vector;
	}

	public String[] getTransformation() {
		return transformation;
	}

	public void setTransformation(final String... transformation) {
		this.transformation = transformation;
	}

	public Double[][] getOffset() {
		return offset;
	}

	public void setOffset(final Double[]... offset) {
		this.offset = offset;
	}

	@Deprecated
	public String[] getOffset_units() {
		return getOffsetUnits();
	}

	@Deprecated
	public void setOffset_units(final String... offsetUnits) {
		setOffsetUnits(offsetUnits);
	}

	public String[] getOffsetUnits() {
		return offsetUnits;
	}

	public void setOffsetUnits(final String... offsetUnits) {
		this.offsetUnits = offsetUnits;
	}
}
