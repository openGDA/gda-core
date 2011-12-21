/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.util;

import gda.data.nexus.Activator;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNodeSelection;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import uk.ac.diamond.scisoft.analysis.io.NexusLoader;
import uk.ac.gda.monitor.ProgressMonitorWrapper;

/**
 * This is a UI util
 */
public class NexusUtils {
	
	private static Logger logger = LoggerFactory.getLogger(NexusUtils.class);

	/**
	 * Load tree
	 * @param path
	 * @param monitor
	 * @return ScanFileHolder
	 * @throws Exception
	 */
	public static INexusTree loadTree(final String path, IProgressMonitor monitor) throws Exception {
		return NexusUtils.loadTree(path, null, monitor);
	}

	/**
	 * Load tree
	 * @param path
	 * @param sel may be null 
	 * @param monitor
	 * @return ScanFileHolder
	 * @throws Exception
	 */
	public static INexusTree loadTree(final String path, NexusTreeNodeSelection sel, IProgressMonitor monitor) throws Exception {
	
		if (sel==null) sel = NexusTreeNodeSelection.GET_ALL;
	
		// Need to ensure that neutron.nexus.NexusException is loaded by nexus first.
		final Activator activator = Activator.getDefault();
		if (activator!=null) {
			final Bundle bundle = activator.getBundle();
			if (bundle!=null) bundle.update();
		}
		
		monitor.worked(1);
		if (monitor.isCanceled()) return null;
	
		monitor.worked(1);
		if (monitor.isCanceled()) return null;
	
		monitor.worked(1);
		if (monitor.isCanceled()) return null;
	
		long start = System.nanoTime();
	
		monitor.worked(1);
		if (monitor.isCanceled()) return null;
		INexusTree tree = new NexusLoader(path, NexusTreeNodeSelection.SKIP, sel, null).loadTree(new ProgressMonitorWrapper(monitor));
		monitor.worked(3);
	
		logger.info("Loading tree took {}s", (System.nanoTime() - start)*1e-9);
	
		return tree;
	}

	public static NexusTreeNodeSelection getSel() throws Exception{
		String xml = "<?xml version='1.0' encoding='UTF-8'?>" +
		"<nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXentry</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXdata</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>SDS</nxClass><wanted>2</wanted><dataType>1</dataType>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXinstrument</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXdetector</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>SDS</nxClass><wanted>2</wanted><dataType>1</dataType>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>";
		return NexusTreeNodeSelection.createFromXML(new InputSource(new StringReader(xml)));
	}

	public static Map<String, Integer> getDataSetSizes(final String path, final List<String> sets, IProgressMonitor monitor) throws Exception {
		Map< String, INexusTree> trees = NexusLoader.getDatasetNexusTrees(path, sets, false, new ProgressMonitorWrapper(monitor));
		final Map<String,Integer> ret = new HashMap<String, Integer>(sets.size());
		for (Entry<String, INexusTree> tree : trees.entrySet()) {
			ret.put(tree.getKey(),
					NexusExtractor.calcTotalLength(tree.getValue().getData().dimensions));
		}

		return ret;
	}

}
