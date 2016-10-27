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

package uk.ac.gda.devices.bssc.ui;

import gda.commandqueue.JythonCommandCommandProvider;
import gda.commandqueue.Queue;

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

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class BSSCSessionBeanComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(BSSCSessionBeanComposite.class);

	private NumberBox sampleStorageTemperature;
	private MeasurementsFieldComposite measurements;
	private GridData layoutData;

	public BSSCSessionBeanComposite(Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);
		GridLayout layout = new GridLayout(2, false);
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
				// TODO
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
		
		Button btnQueueExperiment = new Button(this, SWT.NONE);
		layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		btnQueueExperiment.setLayoutData(layoutData);
		btnQueueExperiment.setText("Queue Experiment");
		btnQueueExperiment.setToolTipText("save file and queue for execution (will start immediately if queue running");
		btnQueueExperiment.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {
					IProgressMonitor monitor = new NullProgressMonitor();
					editor.doSave(monitor);
					if (monitor.isCanceled())
						return;
					Queue queue = CommandQueueViewFactory.getQueue();
					if (queue != null) {
						queue.addToTail(new JythonCommandCommandProvider(String.format("import BSSC; BSSC.BSSCRun(\"%s\").run()", editor.getPath()), editor.getTitle(), editor.getPath()));
					} else {
						logger.warn("No queue received from CommandQueueViewFactory");
					}
				} catch (Exception e1) {
					logger.error("Error adding command to the queue", e1);
				}
			}
		});
		
		measurements = new MeasurementsFieldComposite(this, SWT.NONE, editor);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan=2;
		measurements.setLayoutData(layoutData);
	}

	public FieldComposite getSampleStorageTemperature() {
		return sampleStorageTemperature;
	}

	public FieldComposite getMeasurements() {
		return measurements;
	}
}