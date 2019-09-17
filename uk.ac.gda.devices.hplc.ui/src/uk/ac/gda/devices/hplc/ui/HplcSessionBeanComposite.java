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

package uk.ac.gda.devices.hplc.ui;

import gda.factory.Finder;
import gda.util.RemoteCommandRunner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.UIHelper;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class HplcSessionBeanComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HplcSessionBeanComposite.class);

	private NumberBox sampleStorageTemperature;
	private HplcSampleFieldComposite measurements;
	private GridData layoutData;

	public HplcSessionBeanComposite(Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth=5;
		setLayout(layout);
		Composite composite = new Composite(this, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.marginWidth=0;
		rowLayout.marginTop=0;
		rowLayout.marginBottom=0;
		rowLayout.marginLeft=0;
		rowLayout.marginRight=0;
		rowLayout.spacing=5;
		composite.setLayout(rowLayout);
		
		Button btnNewSample = new Button(composite, SWT.NONE);
		btnNewSample.setText("New Sample");
		btnNewSample.setToolTipText("add line(s) for new sample(s)");
		btnNewSample.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				if (measurements != null)
					measurements.addSample();
			}
		});
		
		Button btnDelete = new Button(composite, SWT.NONE);
		btnDelete.setText("Delete");
		btnDelete.setToolTipText("remove selected rows");
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				if (measurements != null)
					measurements.deleteSelection();			
			}
		});
		layoutData = new GridData(SWT.TRAIL, SWT.FILL, true, false, 1, 1);

		Composite runControls = new Composite(this, SWT.NONE);
		runControls.setLayout(new GridLayout(2, false));
		runControls.setLayoutData(layoutData);
		
		layoutData = new GridData(SWT.TRAIL, SWT.FILL, true, false, 1, 1);

		layoutData = new GridData(SWT.TRAIL, SWT.FILL, true, false, 1, 1);
		Button btnQueueExperiment = new Button(runControls, SWT.NONE);
		btnQueueExperiment.setLayoutData(layoutData);
		btnQueueExperiment.setText("Run Experiment");
		btnQueueExperiment.setToolTipText("Save file and run experiment");
		btnQueueExperiment.addSelectionListener(new SelectionAdapter() {
			private RemoteCommandRunner runner  = Finder.getInstance().find("HPLCRunner");
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IProgressMonitor monitor = new NullProgressMonitor();
					editor.doSave(monitor);
					if (monitor.isCanceled()) {
						return;
					}
					runner.runCommand(editor.getPath().replace(",", "\\'"));
				} catch (Exception ex) {
					logger.error("Couldn't run HPLC experiment", ex);
					UIHelper.showError("Couldn't run HPLC experiment", ex);
				}
			}
		});
		measurements = new HplcSampleFieldComposite(this, SWT.NONE, editor);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan=3;
		measurements.setLayoutData(layoutData);
	}

	public FieldComposite getSampleStorageTemperature() {
		return sampleStorageTemperature;
	}

	public FieldComposite getMeasurements() {
		return measurements;
	}
}
