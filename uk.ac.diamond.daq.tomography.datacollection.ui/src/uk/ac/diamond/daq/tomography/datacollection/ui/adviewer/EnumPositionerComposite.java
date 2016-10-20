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

package uk.ac.diamond.daq.tomography.datacollection.ui.adviewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import uk.ac.gda.common.rcp.util.GridUtils;

public class EnumPositionerComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerComposite.class);
	private EnumPositioner positioner;
	private IObserver observer;
	private Group group;
	private Combo pcom;
	private String currentPos;

	public EnumPositionerComposite(Composite parent, int style, String title, final String confirmSelectionMsgTemplate, final String jobTitle,
			final String setCmd) {
		super(parent, style);
		GridLayoutFactory fillDefaults = GridLayoutFactory.fillDefaults();
		fillDefaults.applyTo(this);

		group = new Group(this, SWT.NONE);
		group.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		group.setText(title);
		fillDefaults.applyTo(group);

		pcom = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.CENTER | SWT.READ_ONLY);
		pcom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String newVal = pcom.getText();
				if (newVal.equals(currentPos))
					return;
				pcom.setText(currentPos);
				int open = SWT.YES;
				if (confirmSelectionMsgTemplate != null) {
					String msg = String.format(confirmSelectionMsgTemplate, newVal);
					MessageBox box = new MessageBox(EnumPositionerComposite.this.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					// box.setMessage("Are you sure you want to change the binning to '" + newVal
					// +"'. The detector will respond when acquisition is restarted.");
					box.setMessage(msg);
					open = box.open();
				}
				if (open == SWT.YES) {
					Job job = new Job(jobTitle) {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								if (setCmd != null) {
									InterfaceProvider.getCommandRunner().evaluateCommand(String.format(setCmd, newVal));
								} else if (positioner != null) {
									positioner.moveTo(newVal);
								}
								return Status.OK_STATUS;
							} catch (DeviceException e) {
								logger.error("Error changing value", e);
							}
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				}
			}
		});
		GridDataFactory.fillDefaults().minSize(100, SWT.DEFAULT).applyTo(pcom);
		pcom.setItems(new String[] {});
		pcom.setVisible(true);

		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (positioner != null && observer != null)
					positioner.deleteIObserver(observer);
			}
		});
	}

	private void updateDisplay() {
		observer.update(positioner, null);
	}

	public void setEnumPositioner(EnumPositioner s) {
		positioner = s;
		try {
			pcom.removeAll();
			for (String pos : positioner.getPositions()) {
				pcom.add(pos);
			}
		} catch (DeviceException e1) {
			logger.error("Error getting positions from " + positioner.getName(), e1);
		}

		observer = new IObserver() {
			@Override
			public void update(Object source, final Object arg) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						try {
							currentPos = (String) positioner.getPosition();
							pcom.setText(currentPos);
							GridUtils.layout(group);
						} catch (DeviceException e) {
							logger.error("Error reading position of " + positioner.getName(), e);
						}
					}
				});
			}
		};
		positioner.addIObserver(observer);
		updateDisplay();
	}
}
