/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.simplescan;

import java.io.File;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class SimpleScanView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(XMLHelpers.class);
	private String path;
	SimpleScan editingBean = null;
	SimpleScanComposite simpleScanComposite;
	PosComposite posComposite;
	
	private IPartListener partListener = new IPartListener() {
		@Override
		public void partActivated(IWorkbenchPart part) {}
		@Override
		public void partBroughtToTop(IWorkbenchPart part) {}
		@Override
		public void partClosed(IWorkbenchPart part) {}
		@Override
		public void partDeactivated(IWorkbenchPart part) {
			if(part instanceof SimpleScanView){
			try {
				BeanUI.uiToBean(simpleScanComposite, editingBean);
				XMLHelpers.writeToXML(SimpleScan.mappingURL, editingBean, path);
			} catch (Exception e) {
			}
			}
		}
		@Override
		public void partOpened(IWorkbenchPart part) {}
	};

	@Override
	public void dispose(){
		getSite().getPage().removePartListener(partListener);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		URL url = SimpleScan.class.getResource("simpleScan.xml");
		String dir = (new File(EclipseUtils.getAbsoluteUrl(url).getFile())).getAbsolutePath();
        try {
			editingBean = (SimpleScan) XMLHelpers.createFromXML(SimpleScan.mappingURL, SimpleScan.class,
					SimpleScan.schemaURL, dir);
		} catch (Exception e) {
			logger.error("Could not load xml " + path + " into bean", e);
		}
        
		Composite posComposite = new Composite(parent, SWT.NONE);
		GridData gd_posComposite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_posComposite.widthHint = 900;
		posComposite.setLayoutData(gd_posComposite);
		GridLayout gl_posComposite = new GridLayout(1, false);
		posComposite.setLayout(gl_posComposite);
        
        posComposite = new PosComposite(posComposite, SWT.NONE, editingBean);
		simpleScanComposite = new SimpleScanComposite(posComposite, SWT.NONE, editingBean, null);
		
		getSite().getPage().addPartListener(partListener);
	}

	@Override
	public void setFocus() {
	}
}
