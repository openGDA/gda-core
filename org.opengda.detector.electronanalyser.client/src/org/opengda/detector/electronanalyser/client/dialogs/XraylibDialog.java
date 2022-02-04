/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.dialogs;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.opengda.detector.electronanalyser.client.views.XraylibComposite;


public class XraylibDialog extends Dialog {

	private IPlottingSystem<Composite> plottingSystem;
	private XraylibComposite xraylibComposite;

	public XraylibDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.MODELESS | SWT.BORDER);
		setBlockOnOpen(false);
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		xraylibComposite = new XraylibComposite(container, 0, plottingSystem);

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Xraylib Database - Plotting controls");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    createButton(parent, IDialogConstants.OK_ID, "About", false);
	    createButton(parent, IDialogConstants.CLOSE_ID,
	    		IDialogConstants.CLOSE_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.CLOSE_ID == buttonId) {
			xraylibComposite.removeAllRegions();
			xraylibComposite.cleanUp();
			close();
		} else if (IDialogConstants.OK_ID == buttonId) {
			// Open dialog
			MessageDialog dialog = new MessageDialog(getShell(),
					"About xraylib...",
					null,
					"xraylib is a BSD-licensed open-source library for interactions of X-rays with matter. " +
					"It supports all major platforms and can be used from many programming languages, such as C/C++, Fortran, Python, Java and others... " +
					"For more information about xraylib, please click the following links.",
					MessageDialog.INFORMATION,
					new String[]{"Ok"},
					0) {
				@Override
				protected Control createCustomArea(Composite parent) {
					Composite composite = new Composite(parent, SWT.NONE);
					composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					composite.setLayout(new GridLayout());
					SelectionListener listener = new SelectionListener() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							Program.launch(e.text);
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							Program.launch(e.text);
						}
					};
					Link link = new Link(composite, SWT.WRAP);
				    link.setText("<a href=\"https://github.com/tschoonj/xraylib\">Official website</a>");
				    link.addSelectionListener(listener);
				    link.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
					link = new Link(composite, SWT.WRAP );
				    link.setText("<a href=\"http://lvserver.ugent.be/xraylib-web\">Online calculator</a>");
				    link.addSelectionListener(listener);
				    link.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
					link = new Link(composite, SWT.WRAP );
				    link.setText("<a href=\"http://dx.doi.org/10.1016/j.sab.2011.09.011\">xraylib article</a>");
				    link.addSelectionListener(listener);
				    link.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
					return composite;
				}
			};
			dialog.open();
		}
	}

	public void setPlottingSystem(IPlottingSystem<Composite> plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

}
