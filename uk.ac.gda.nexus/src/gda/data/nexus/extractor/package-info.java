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

// this text is in file gda/data/neux/extractor/package-info.java
/**
 * <code>gda.data.nexus.extractor</code> is a set of code for general purpose extraction of data from nexus files.
 * An example of its use is in gda.data.nexus.tree.NexusTreeBuilder.
 * 
 * 
 *      <p>
 *      A nexus file has a hierarchical structure with an item containing other items which in term
 *      contain other items.
 *      The design for extracting data from nexus files is similar to that used in expat for handling xml files. The user 
 *      class of NexusExtractor implements the 2 methods of INexusTreeProcessor e.g. beginElement and endElement
 *      As each item is discovered in a nexus file beginElement is called with the name and class of the item e.g.
 *      heading1 and SDS is the item that contains the data for dataset heading1.
 *      Dependent on the response from beginElement NexusExtractor will either stop processing, skip the current item or
 *      re-iterate into its children.
 *      On completing an item NexusExtractor calls the endElement method.
 *      Whilst in the beginElement method the processor can call NexusExtractor.getDataForCurrentProcessedGroup to
 *      obtain the contains of the data in the current group.
 *      
 * @see gda.data.nexus.tree.NexusTreeBuilder
 *      
 */
package gda.data.nexus.extractor;