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
import gda.rcp.GDAClientActivator;

import java.io.File;

import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.binding.IBeanService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.beans.xml.XMLHelpers;

public class SimpleScanView extends ViewPart {

	public static final String ID = "gda.simplescan.SimpleScanView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(SimpleScanView.class);
	private String path;
	SimpleScan bean = null;
	SimpleScanComposite simpleScanComposite;
	PosComposite posComposite;
	AddDevicesComposite addDevicesComposite;

	public SimpleScanView() {
	}


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
					final IBeanService service = GDAClientActivator.getService(IBeanService.class);
					final IBeanController control = service.createController(simpleScanComposite, bean);
					control.uiToBean();
					XMLHelpers.writeToXML(SimpleScan.mappingURL, bean, path);
				} catch (Exception e) {
					logger.error("Cannot map ui to bean!");
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
			bean = (SimpleScan) XMLHelpers.createFromXML(SimpleScan.mappingURL, SimpleScan.class,
					SimpleScan.schemaURL, path);
		} catch (Exception e) {
			logger.error("Could not load xml " + path + " into bean", e);
		}

        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        gl.marginHeight = 0;
        parent.setLayout(gl);
        GridData gd = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
        gd.horizontalIndent=0;
        gd.verticalIndent=0;

        parent.setLayoutData(gd);

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_composite.widthHint = 900;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 0;
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);

        posComposite = new PosComposite(composite, SWT.NONE, bean);
        addDevicesComposite = new AddDevicesComposite(composite, SWT.NONE, bean);
		simpleScanComposite = new SimpleScanComposite(parent, SWT.NONE, bean);


		addDevicesComposite.getAddScannable().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String foundScannableName = addDevicesComposite.getScannableManagerComposite().getScannableName().getText();
				ScannableManagerBean smb = new ScannableManagerBean();
				smb.setScannableName(foundScannableName);
				if (addDevicesComposite.getScannableManagerComposite().getScannableName().isFound()) {
					bean.addScannable(smb);
				}
				updateBeans();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		addDevicesComposite.getRemoveScannable().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				bean.removeScannable(addDevicesComposite.getScannables().getSelected());
				updateBeans();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});


		addDevicesComposite.getAddDetector().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String foundDetectorName = addDevicesComposite.getDetectorManagerComposite().getDetectorName().getText();
				DetectorManagerBean smb = new DetectorManagerBean();
				smb.setDetectorName(foundDetectorName);
				smb.setDetectorDescription("");
				if (addDevicesComposite.getDetectorManagerComposite().getDetectorName().isFound()) {
					bean.addDetector(smb);
					updateBeans();
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		addDevicesComposite.getRemoveDetector().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				bean.removeDetector(addDevicesComposite.getDetectors().getSelected());
				updateBeans();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		getSite().getPage().addPartListener(partListener);
	}

	private void updateBeans(){
		posComposite.setBean(bean);
		addDevicesComposite.setBean(bean);
		simpleScanComposite.setBean(bean);

		addDevicesComposite.updateScannables();
		posComposite.updateScannables();
		simpleScanComposite.updateScannables();

		addDevicesComposite.updateDetectors();
		simpleScanComposite.updateDetectors();
	}

	@Override
	public void setFocus() {
	}
}
