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

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
	AddDevicesComposite addDevicesComposite;
	
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
        path = LocalProperties.getConfigDir() + File.separator+ "templates" + File.separator+ "simpleScan.xml";
        try {
			editingBean = (SimpleScan) XMLHelpers.createFromXML(SimpleScan.mappingURL, SimpleScan.class,
					SimpleScan.schemaURL, path);
		} catch (Exception e) {
			logger.error("Could not load xml " + path + " into bean", e);
		}
        
        GridLayout gl = new GridLayout(1, false);
        parent.setLayout(gl);
        GridData gd = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
        gd.horizontalIndent=0;
        gd.verticalIndent=0;
        
        parent.setLayoutData(gd);
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite.widthHint = 900;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(2, false);
		composite.setLayout(gl_composite);
        
        posComposite = new PosComposite(composite, SWT.NONE, editingBean);
        addDevicesComposite = new AddDevicesComposite(composite, SWT.NONE, editingBean);

		simpleScanComposite = new SimpleScanComposite(parent, SWT.NONE, editingBean);
		
		getSite().getPage().addPartListener(partListener);
	}

	@Override
	public void setFocus() {
	}
}
