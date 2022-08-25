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

package gda.rcp.ncd.widgets;

import static org.eclipse.swt.SWT.CENTER;
import static org.eclipse.swt.SWT.FILL;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.observable.IObserver;
import gda.rcp.ncd.Activator;
import uk.ac.diamond.daq.concurrent.Async;

public class ShutterGroup implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(ShutterGroup.class);

	private static final Map<String, String> status2action = Map.of(
			"Open", "Close",
			"Closed", "Open",
			"Close", "Open", // this one is for testing with the demented dummy
			"Fault", "Reset",
			"Opening", "Wait",
			"Closing", "Wait"
	);

	private final EnumPositioner shutter;
	private final Button button;
	private final Label label;
	final Color red;
	final Color defaultColor;
	final Color green;

	private Group group;

	public ShutterGroup(Composite parent, int style, final EnumPositioner shutter) {

		group = new Group(parent, style);
		group.setText(shutter.getName());
		group.setLayoutData(new GridData(FILL, FILL, true, false));

		this.shutter = shutter;

		var layout = new GridLayout(2, false);
		group.setLayout(layout);
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		label = new Label(group, SWT.NONE);
		label.setText("  Fault  ");
		label.setLayoutData(new GridData(FILL, CENTER, true, true));

		button = new Button(group, SWT.NONE);
		button.setLayoutData(new GridData(FILL, FILL, false, true));
		button.setText("  Wait  ");
		button.setToolTipText(String.format("Operate %s", shutter.getName()));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// do what is says on the tin
				final String action = button.getText();

				if (action.equals("Wait")) {
					return;
				}

				Async.execute(() -> {
						try {
							shutter.asynchronousMoveTo(action);
						} catch (DeviceException de) {
							// Create the required Status object
							final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error Operating "
									+ shutter.getName(), de);

							Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getCurrent().getActiveShell(), "DeviceException", "Error Operating "
											+ shutter.getName(), status));
						}
					}, "%s %s button selected", shutter.getName(), action);
			}
		});

		red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		green = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
		defaultColor = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);
		shutter.addIObserver(this);
		update(null, null);
		group.addDisposeListener(e -> shutter.deleteIObserver(this));
	}
	@Override
	public void update(Object theObserved, Object changeCode) {
		Async.execute(() -> {
			try {
				// TODO I agree it is not too good requesting more information in the update
				// we should improve the changeCode
				final String status = shutter.getPosition().toString();
				final String nextaction = status2action.get(status);

				Display dis = Display.getDefault();
				dis.asyncExec(() -> {
					if ("close".equalsIgnoreCase(status) || "closed".equalsIgnoreCase(status)) {
						label.setText("Closed");
						group.setBackground(red);
					} else if ("FAULT".equals(status)) {
						label.setText("FAULT");
						group.setBackground(red);
					} else {
						label.setText(status);
						group.setBackground(defaultColor);
					}

					if (nextaction != null) {
						button.setText(nextaction);
					} else {
						// something fishy
						button.setText("Reset");
					}
				});
			} catch (DeviceException e) {
				logger.warn("could not get status for {}", shutter.getName(), e);
			}
		});
	}
}