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

package gda.data.nexus.tree;

/**
 * This interface is used by NexusDetectors to tag data that should be 
 * linked into the NXdata section in the NeXus file. That would be done 
 * with data that the user could want to plot and helps to distinguish between 
 * data of that kind and other detector related information (like pixel size 
 * information or similar static data).
 */
public interface PerScanDataLeaf extends INexusTree {

}
