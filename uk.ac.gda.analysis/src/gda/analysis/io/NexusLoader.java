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

package gda.analysis.io;

import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.IMonitor;

import gda.data.nexus.extractor.NexusExtractorException;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNodeSelection;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * This duplicates static methods to allow direct access to the methods from this class.
 * This is here simply to avoid breakages in scripts.
 * @deprecated use {@link uk.ac.diamond.scisoft.analysis.io.NexusLoader}
 */
@Deprecated(since="at least 2012")
public class NexusLoader extends uk.ac.diamond.scisoft.analysis.io.NexusLoader {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(NexusLoader.class);

	private NexusLoader() {

	}

	public static Map<String, INexusTree> getDataSetNodes(INexusTree tree, List<String> dataSetNames) {
		logger.deprecatedMethod("getDataSetNodes(INexusTree, List<String>)", null, "uk.ac.diamond.scisoft.analysis.io.NexusLoader.getDatasetNodes");
		return uk.ac.diamond.scisoft.analysis.io.NexusLoader.getDatasetNodes(tree, dataSetNames);
	}

	public static INexusTree getTreeForDataSetNames(String filename, List<String> dataSetNames, boolean withData, IMonitor mon) throws NexusException, NexusExtractorException {
		logger.deprecatedMethod("getTreeForDataSetNames(String, List<String>, boolean, IMonitor)", null, "gda.data.nexus.tree.NexusTreeBuilder.getNexusTree");
		return NexusTreeBuilder.getNexusTree(filename, NexusTreeNodeSelection.createTreeForDataSetNames(dataSetNames, withData), mon);
	}

	/*
	 * Helper method to extract DataSetNames from a NexusFile that can then be used to build a list of wanted names
	 * in subsequent calls to NexusLoader(file, dataSetNames)
	 */
	public static List<String> getDataSetNames( String nexusFilename, IMonitor mon) throws NexusException, NexusExtractorException, Exception {
		logger.deprecatedMethod("getDataSetNames(String, IMonitor)", null, "uk.ac.diamond.scisoft.analysis.io.NexusLoader.getDatasetNames");
		return uk.ac.diamond.scisoft.analysis.io.NexusLoader.getDatasetNames(nexusFilename, mon);
	}

	/*
	 * Helper method to extract sizes of specified datasets
	 */
	public static Map<String, INexusTree> getDataSetNexusTrees( String nexusFilename, List<String> dataSetNames, boolean withData, IMonitor mon) throws NexusException, NexusExtractorException, Exception{
		logger.deprecatedMethod("getDataSetNexusTrees(NexusFIlename, List<String>, boolean, IMonitor)", null, "uk.ac.diamond.scisoft.analysis.io.NexusLoader.getDatasetNexusTrees(String, List<String>, boolean, IMonitor)");
		INexusTree tree = getTreeForDataSetNames(nexusFilename, dataSetNames, withData, mon);
		Map<String, INexusTree> nodes =  getDataSetNodes(tree, dataSetNames);
		return nodes;
	}
}
