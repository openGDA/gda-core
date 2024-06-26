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

package uk.ac.gda.analysis.mscan;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusObjectProvider;
import org.eclipse.january.dataset.Dataset;

import gda.data.scan.datawriter.NexusScanDataWriter;

/**
 * Consumes datasets provided during a scan in order to perform processing.
 * This form of processing within GDA and the scan is intended to be for
 * lightweight processing only.
 */
public interface MalcolmSwmrProcessor<T extends NXobject> {

	/**
	 * Prepare processor to receive data. For example create the lazy datasets
	 * and add to the {@code NexusObjectWrapper}
	 * @param info the current scan info
	 * @param nexusProvider Nexus provider to write datasets into
	 */
	void initialise(NexusScanInfo info, AbstractNexusObjectProvider<T> nexusProvider);

	/**
	 * Perform processing for the data. E.g calculate a statistic
	 * and write to the {@code NXdata}
	 * @param data current detector frame to process
	 * @param metaSlice metadata fror this frame
	 */
	void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice);

	/**
	 * Check if processor is currently enabled
	 */
	boolean isEnabled();

	/**
	 * Set enabled state of processor
	 */
	void setEnabled(boolean enabled);

	/**
	 * The name of the {@link NXdata} group to add this field to. This can be used
	 * to group stats fields together in a single {@code NXdata} group.
	 * This property only applies when using new nexus writing (i.e. {@link NexusScanDataWriter}).
	 * @return the name of the data group
	 */
	String getDataGroupName();

	/**
	 * Set name of {@link NXdata} group to add this field to.
	 * @see #getDataGroupName() the name of the data group
	 * @param dataGroupName
	 */
	void setDataGroupName(String dataGroupName);

}
