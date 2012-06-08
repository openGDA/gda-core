/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

// this text is in file org.gda.tango/src/gda/device/package-info.java
/**
 * <code>gda.device.detector.nexusprocessor</code> a set of code for extending a NexusDetector to allow for processing.
 * The processing can result in addition items in the NexusProvider and plottable values
 * <p>
 * <code>NexusDetectorProcessor</code> - master NexusDetector wrapper. gets data from real detector and sends to a
 * processor <code>NexusTreeProviderProcessor</code> *
 * <p>
 * <code>NexusTreeProviderProcessor</code> - interface for processing the NexusProvider
 * <p>
 * <code>NexusTreeProviderProcessors</code> - implementation of NexusTreeProviderProcessor that delegates to a list of
 * NexusTreeProviderProcessor
 * <p>
 * <code>NexusProviderDatasetProcessor</code> - implementation of NexusTreeProviderProcessor that extracts a dataset
 * from a specified location in the NXDetectorData and passes on to a set of <code>DataSetProcessor</code>
 * <p>
 * <code>DataSetProcessor</code> - interface for processing a dataset. Process method returns
 * <code>ProcessorResults</code>
 * <p>
 * <code>ProcessorResults</code> - interface of result of processing a NexusProvider. Includes method to return
 * definitions of data to add to the NexusProvider. This data is in the form of interface <code>NexusData</code>
 * <p>
 * <code>NexusData</code> - definition of data to add to the NxDetectorData
 * <p>
 * <code>NexusDataFactory</code> - factory class for creating NexusData items
 * <p>
 * <code>DataSetPlotter</code> - DataSetProcessor that sends data to client for plotting
 * <p>
 * <code>DataSetFitter</code> - DataSetProcessor that fits gaussian in the 2d dataset
 * <p>
 * <code>ExampleDataSetProcessor</code> - Example DataSetProcessor that adds data to the NexusProvider
 */
package gda.device.detector.nexusprocessor;