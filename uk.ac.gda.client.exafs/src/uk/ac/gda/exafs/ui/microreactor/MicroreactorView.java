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

package uk.ac.gda.exafs.ui.microreactor;

import gda.jython.JythonServerFacade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.beans.exafs.i20.MicroreactorParameters;

public class MicroreactorView extends ViewPart {
	public MicroreactorView() {
	}
	
	private MicroreactorParametersComposite microreactorParameters;
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		this.microreactorParameters = new MicroreactorParametersComposite(parent, SWT.NONE);
		microreactorParameters.setEditorClass(MicroreactorParameters.class);
		
		Button setMasses = new Button(parent, SWT.PUSH);
		setMasses.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		setMasses.setText("Set Masses");
		
		setMasses.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String masses =  (String) microreactorParameters.getMasses().getValue();
				JythonServerFacade.getInstance().runCommand("cirrus.setMasses(["+masses+"])");
			}
		});
		
	}

	@Override
	public void setFocus() {
		
		
	}

}
