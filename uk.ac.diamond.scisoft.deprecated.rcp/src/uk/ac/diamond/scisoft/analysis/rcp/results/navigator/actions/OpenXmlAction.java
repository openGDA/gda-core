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

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator.actions;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeNodeSelection;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import uk.ac.gda.richbeans.xml.XMLEditorManager;

public class OpenXmlAction extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(OpenXmlAction.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final Object sel = ((IStructuredSelection)selection).getFirstElement();
			if (sel!=null&&sel instanceof IFile) {
				try {
					final String       path  = ((IFile)sel).getLocation().toString();
					final INexusTree   tree  = NexusTreeBuilder.getNexusTree(path, getSelection());
					final NexusTreeNode xml  = (NexusTreeNode) tree.getChildNode(0).getChildNode(0);
					
					final List<String> files = new ArrayList<String>(7);
					for (int i = 0; i < xml.getChildCount(); i++) {
						final NexusTreeNode xmlFile = (NexusTreeNode)xml.getChildNode(i);
						final String        data    = (String)xmlFile.getData().getFirstValue();
						files.add(data);
					}
					XMLEditorManager.openXmlEditorsFromStrings(files);
					
				} catch (Throwable ne) {
					logger.error("Cannot open file "+sel, ne);
					return Boolean.FALSE;
				}
			}
		}
		
		return Boolean.TRUE;
	}

	public NexusTreeNodeSelection getSelection() throws Exception {
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

}
