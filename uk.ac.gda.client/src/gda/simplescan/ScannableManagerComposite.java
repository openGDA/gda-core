/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.device.Scannable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.components.wrappers.FindableNameWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class ScannableManagerComposite extends Composite {

	private FindableNameWrapper scannableName;

	public ScannableManagerComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 1;
		setLayout(gridLayout);
		scannableName = new FindableNameWrapper(this, SWT.BORDER, Scannable.class, false);
		scannableName.on();
		GridData gd_scannableName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		scannableName.setLayoutData(gd_scannableName);
		scannableName.addValueListener(new ValueAdapter() {
			@Override
			public void valueChangePerformed(ValueEvent e) {

			}
		});
	}

	public void selectionChanged(ScannableManagerBean bean) {
		String name = scannableName.getValue().toString();
		if (name != null && bean != null) {
			bean.setScannableName(name);
			scannableName.refresh();
		}
	}
	
	public FindableNameWrapper getScannableName() {
		return scannableName;
	}
}
