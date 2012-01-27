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

// this text is in file gda/data/neux/tree/package-info.java
/**
 * <code>gda.data.nexus.tree</code> is a set of code to represent data in an Nexus file in memory and supports
 * reading and writing that data to nexus files and text formats
 * 
 *      <p>
 *      A nexus file has a hierarchical structure with an item containing other items which in term
 *      contain other items.
 *      The interface class gda.data.nexus.tree.INexusTree can be used
 *      to represent this structure in memory. Each node contains a name and class as in Nexus and has a list of children 
 *      which is equivalent to the sub groups within a Nexus element. If the node holds actual data from an SDS element then
 *      the getData method with return a NexusGroupData item which contains the type of data, its dimensions and if 
 *      requested the data itself.
 *      
 *      The class gda.data.nexus.tree.NexusTreeBuilder is an implementation of gda.data.nexus.extractor.INeuxTreeProcessor that
 *      will construct a NexusTreeNode object from a Nexus file given element selection criteria e.g. All elements under 
 *      NXextry/NXinstrument. The criteria is given in the class gda.data.nexus.tree.NexusTreeNodeSelection which can
 *      be constructed in memory or read from an XML document.
 *      
 * @see gda.data.nexus.extractor.NexusExtractor
 */
package gda.data.nexus.tree;