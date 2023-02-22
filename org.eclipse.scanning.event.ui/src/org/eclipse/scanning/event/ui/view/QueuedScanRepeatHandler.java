/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event.ui.view;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.JobQueueConfiguration;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.IRerunHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This rerun handler asks the user to specify number of repetitions of the scan they want to submit.
 */
public class QueuedScanRepeatHandler implements IRerunHandler<StatusBean> {

	private static final Logger logger = LoggerFactory.getLogger(QueuedScanRepeatHandler.class);
	private IJobQueue<StatusBean> jobQueueProxy;

	@Override
	public void init(IEventService eventService, JobQueueConfiguration conf) {
		try {
			jobQueueProxy = eventService.createJobQueueProxy(conf.getUri(), conf.getSubmissionQueue(), EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		} catch (Exception e) {
			logger.error("Cannot create proxy to queue {}", conf.getSubmissionQueue(), e);
		}
	}

	@Override
	public boolean isHandled(StatusBean bean) {
		try {
			bean.getClass().getDeclaredConstructor();
		}catch(NoSuchMethodException | SecurityException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean handleRerun(List<StatusBean> scans) throws Exception {

		var dialog = new RepeatsDialog(Display.getDefault().getActiveShell());
		if (dialog.open() == Window.OK) {
			repeat(scans, dialog.getNumberOfRepeats());
		}
		return true;
	}

	private void repeat(List<StatusBean> scans, int repeats) {
		IntStream.range(0, repeats).forEach(repeat -> {
			for (var scan : scans) {
				try {
					submit(duplicate(scan));
				} catch (EventException e) {
					logger.error("Cannot submit duplicate of scan to queue.", e);
				}catch(Exception ex) {
					logger.error("Failed to create duplicate of StatusBean.",ex);
				}
			}
		});
	}

	protected StatusBean duplicate(StatusBean bean) throws  Exception {
		var duplicate = bean.getClass().getDeclaredConstructor().newInstance();
		duplicate.merge(bean);
		duplicate.setUniqueId(UUID.randomUUID().toString());
		duplicate.setMessage("Rerun of " + bean.getName());
		duplicate.setStatus(Status.SUBMITTED);
		duplicate.setPercentComplete(0.0);
		return duplicate;
	}

	private void submit(StatusBean bean) throws EventException {
		bean.setSubmissionTime(System.currentTimeMillis());
		jobQueueProxy.submit(bean);
	}

	private class RepeatsDialog extends Dialog {
		private int repeats = 1;
		protected RepeatsDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			Label lbl = new Label(composite, SWT.NONE);
			lbl.setText("Number of Repeats:");

			Spinner spinner = new Spinner(composite, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(spinner);
			spinner.setMinimum(1);
			spinner.setMaximum(100);
			spinner.setSelection(1);
			spinner.setIncrement(1);
			spinner.addModifyListener(modify -> repeats = spinner.getSelection());
			return composite;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Repeat scans");
		}

		public int getNumberOfRepeats() {
			return repeats;
		}
	}
}
