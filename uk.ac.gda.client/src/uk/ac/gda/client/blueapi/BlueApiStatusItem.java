/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.gda.client.blueapi;

import java.util.Optional;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.swtdesigner.SWTResourceManager;

import gda.jython.JythonServerFacade;
import gda.rcp.ApplicationActionBarAdvisor;
import uk.ac.diamond.daq.bluesky.api.BlueApiAuth;
import uk.ac.diamond.daq.bluesky.api.BlueApiEvent;
import uk.ac.diamond.daq.bluesky.api.BlueApiEvent.Login;
import uk.ac.diamond.daq.bluesky.api.BlueApiEvent.Logout;
import uk.ac.diamond.daq.msgbus.MsgBus;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class BlueApiStatusItem extends ContributionItem {
	private static final Logger logger = LoggerFactory.getLogger(BlueApiStatusItem.class);

	private static final Image CROSS = SWTResourceManager.getImage(ApplicationActionBarAdvisor.class,
			"icons/cross.png");
	private static final Image TICK = SWTResourceManager.getImage(ApplicationActionBarAdvisor.class, "icons/tick.png");

	private final int clientId;
	private String user;

	private final BlueApiAuth auth;

	private CLabel label;

	public static Optional<BlueApiStatusItem> build() {
		return ServiceProvider.getOptionalService(BlueApiAuth.class).filter(BlueApiAuth::authEnabled)
				.map(BlueApiStatusItem::new);
	}

	public BlueApiStatusItem(BlueApiAuth auth) {
		this.auth = auth;
		JythonServerFacade jsf = JythonServerFacade.getInstance();
		clientId = jsf.getClientID();
		user = auth.loggedInUser(clientId);
		MsgBus.subscribe(this);
	}

	@Override
	public void fill(Composite parent) {
		final Label sep = new Label(parent, SWT.SEPARATOR);
		label = new CLabel(parent, SWT.NONE);

		refreshLabel();

		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				handleClick();
			}
		});

		final GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		final FontMetrics fm = gc.getFontMetrics();
		var widthHint = (int) fm.getAverageCharacterWidth() * 22;
		var heightHint = fm.getHeight();
		gc.dispose();

		final StatusLineLayoutData linkLayoutData = new StatusLineLayoutData();
		linkLayoutData.widthHint = widthHint;
		label.setLayoutData(linkLayoutData);

		final StatusLineLayoutData separatorLayoutData = new StatusLineLayoutData();
		separatorLayoutData.heightHint = heightHint;
		sep.setLayoutData(separatorLayoutData);
	}

	private void refreshLabel() {
		if (user == null) {
			label.setImage(CROSS);
			label.setText("BlueAPI: --");
			label.setToolTipText("Double click to log in");
		} else {
			label.setImage(TICK);
			label.setText("BlueAPI: " + user);
			label.setToolTipText("Double click to log out");
		}
	}

	@Override
	public void dispose() {
		MsgBus.unsubscribe(this);
		super.dispose();
	}

	private void handleClick() {
		var shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		if (user == null) {
			// login
			var login = new LoginBox(shell, auth, clientId);
			login.open();
		} else {
			// logout
			var logout = MessageDialog.openConfirm(shell, "BlueAPI Logout?", "Log out user " + user + "?");
			if (logout) {
				auth.logout(clientId);
			}
		}
	}

	@Subscribe
	private void updateAuth(BlueApiEvent evt) {
		logger.debug("Received evt: {}", evt);
		if (evt.client() == clientId) {
			Display.getDefault().asyncExec(() -> {
				if (evt instanceof Login login) {
					user = login.user();
				} else if (evt instanceof Logout) {
					user = null;
				}
				refreshLabel();
				getParent().update(false);
			});
		}
	}
}
