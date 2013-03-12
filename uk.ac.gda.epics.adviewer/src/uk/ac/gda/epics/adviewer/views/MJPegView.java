/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.composites.MJPeg;

public class MJPegView extends ViewPart implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(MJPegView.class);

	protected MJPeg mJPeg;
	ADController config;

	public MJPegView(ADController config) {
		this.config = config;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (config == null)
			throw new Exception("Config is null");

	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		Composite composite_1=null;
		ADViewerCompositeFactory mjpegViewCompositeFactory = config.getMjpegViewCompositeFactory();
		if( mjpegViewCompositeFactory != null){
			composite_1 = new Composite(composite, SWT.NONE);
			composite_1.setLayout(new RowLayout(SWT.HORIZONTAL));
		}

		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		mJPeg = new MJPeg(composite_2, SWT.NONE);
		mJPeg.setADController(config);

		
		if( composite_1 != null){
			config.getMjpegViewCompositeFactory().createComposite(config, composite_1, this, mJPeg);
		}
		
		
		setTitleImage(config.getLiveViewImageDescriptor().createImage());
		setPartName(config.getDetectorName() + " Live View");

		createActions();
		createMenu();
		createToolbar();
		createContextMenu();
		hookGlobalActions();

	}

	protected void hookGlobalActions() {
	}

	protected void createContextMenu() {
	}

	protected void createToolbar() {
	}

	protected void createMenu() {
	}

	protected void createActions() {
	}

	@Override
	public void setFocus() {
		mJPeg.setFocus();
	}

	public static void reportErrorToUserAndLog(String s, Throwable th) {
		logger.error(s, th);
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error",
				s + ":" + th.getMessage());
	}

	public static void reportErrorToUserAndLog(String s) {
		logger.error(s);
		MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", s);
	}

	public void zoomToFit() {
		mJPeg.zoomFit();
	}

}