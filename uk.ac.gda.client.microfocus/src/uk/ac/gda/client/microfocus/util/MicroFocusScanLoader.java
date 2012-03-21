/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.util;

import gda.data.nexus.extractor.NexusExtractorException;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeNodeSelection;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.nexusformat.NexusException;
import org.xml.sax.InputSource;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentProjectNature;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.util.io.FileUtils;

public class MicroFocusScanLoader {
	public static NexusTreeNodeSelection getSelection() throws Exception {
		String xml = "<?xml version='1.0' encoding='UTF-8'?>" +
		"<nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXentry</nxClass><wanted>1</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXsample</nxClass><name>xml</name><wanted>1</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>SDS</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>";
		return NexusTreeNodeSelection.createFromXML(new InputSource(new StringReader(xml)));
	}
	public java.util.List<String> loadMapXmlForView(String filePath) throws NexusException, NexusExtractorException, Exception
	{
		final INexusTree   tree  = NexusTreeBuilder.getNexusTree(filePath, MicroFocusScanLoader.getSelection());
		
		if (tree.getChildNode(0) == null || tree.getChildNode(0).getNumberOfChildNodes() == 0) {
			return new ArrayList<String>(0);
		}
		final NexusTreeNode xml  = (NexusTreeNode) tree.getChildNode(0).getChildNode(0);
			
		final java.util.List<String> files = new ArrayList<String>(7);
		for (int i = 0; i < xml.getChildCount(); i++) {
			final NexusTreeNode xmlFile = (NexusTreeNode)xml.getChildNode(i);
			final String        data    = (String)xmlFile.getData().getFirstValue();
			files.add(data);
		}
		return files;
	
		
		
	}
	
	public void loadMapXmlForScan(IFolder folderToLoad, String filePath) throws NexusException, NexusExtractorException, Exception {
		IExperimentObjectManager man = ExperimentProjectNature.createNewEmptyScan(folderToLoad, "loaded", null);
		ScanObject obj = (ScanObject) man.createNewExperiment(filePath.substring(filePath.lastIndexOf(File.separator), filePath.indexOf(".nxs")));
		final INexusTree   tree  = NexusTreeBuilder.getNexusTree(filePath, MicroFocusScanLoader.getSelection());
		final NexusTreeNode xml  = (NexusTreeNode) tree.getChildNode(0).getChildNode(0);
			
		final java.util.List<String> files = new ArrayList<String>(7);
		for (int i = 0; i < xml.getChildCount(); i++) {
			final NexusTreeNode xmlFile = (NexusTreeNode)xml.getChildNode(i);
			final String        data    = (String)xmlFile.getData().getFirstValue();
			files.add(data);
			if(data.contains(MicroFocusScanParameters.class.getSimpleName())){
				File scanfile = FileUtils.getUnique(folderToLoad.getLocation().toFile(), MicroFocusScanParameters.class.getSimpleName(),"xml");
				FileUtils.write(scanfile, data);
				obj.setScanFileName(scanfile.getName());
			}
			else if(data.contains(DetectorParameters.class.getSimpleName())){
				File scanfile = FileUtils.getUnique(folderToLoad.getLocation().toFile(), DetectorParameters.class.getSimpleName(),"xml");
				FileUtils.write(scanfile, data);
				obj.setDetectorFileName(scanfile.getName());
			}
			else if(data.contains(OutputParameters.class.getSimpleName())){
				File scanfile =FileUtils.getUnique(folderToLoad.getLocation().toFile(), OutputParameters.class.getSimpleName(),"xml");
				FileUtils.write(scanfile, data);
				obj.setOutputFileName(scanfile.getName());
			}
				
		}
		obj.setSampleFileName("None");
		man.write();
		
		
	}		

}
