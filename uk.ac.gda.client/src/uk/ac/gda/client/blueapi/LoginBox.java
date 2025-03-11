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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.bluesky.api.BlueApiAuth;
import uk.ac.diamond.daq.bluesky.api.BlueApiAuth.AuthDetails;
import uk.ac.diamond.daq.bluesky.api.BlueApiEvent;
import uk.ac.diamond.daq.msgbus.MsgBus;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class LoginBox extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(LoginBox.class);
	private static final Image COPY = SWTResourceManager.getImage(LoginBox.class, "/icons/page_copy.png");

	/** The index of this client as tracked by the Jython Server */
	private final int clientId;
	private final AuthDetails details;

	/**
	 * Flag to indicate whether messages should be ignored or not - prevents errors when window is closed while handling
	 * messages
	 */
	private volatile boolean alive = true;

	public LoginBox(Shell parentShell, BlueApiAuth auth, int client) {
		super(parentShell);
		clientId = client;
		details = auth.initLogin(clientId);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		var root = (Composite) super.createDialogArea(parent);
		if (details == null) {
			initUnsupported(root);
		} else {
			initLogin(root);
		}
		root.requestLayout();
		return root;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Log in to blueAPI");
	}

	private void initLogin(Composite loginDetails) {
		MsgBus.subscribe(this);
		loginDetails.addDisposeListener(evt -> {
			alive = false;
			MsgBus.unsubscribe(this);
		});
		if (details == null) {
			// This should be unreachable
			throw new IllegalStateException("No login details are available");
		}
		loginDetails.setLayout(new GridLayout(1, false));

		var text = new Label(loginDetails, SWT.WRAP);
		text.setText("If the browser does not open, navigate to the address below to log in");
		text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Composite for URL + copy button
		var urlComposite = new Composite(loginDetails, SWT.NONE);
		urlComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		urlComposite.setLayout(new GridLayout(2, false));

		var urlLink = new StyledText(urlComposite, SWT.BORDER | SWT.WRAP);
		urlLink.setText(details.fullUrl());
		urlLink.setEditable(false);
		urlLink.setToolTipText("Click to open in browser");
		urlLink.setEnabled(true);
		urlLink.setCaret(null);

		var urlLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		urlLayoutData.minimumWidth = 400;
		urlLink.setLayoutData(urlLayoutData);

		var copyButton = new Button(urlComposite, SWT.PUSH);
		copyButton.setToolTipText("Copy URL to clipboard");
		copyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		copyButton.setImage(COPY);

		copyButton.addListener(SWT.Selection, e -> {
			var clipboard = new Clipboard(loginDetails.getDisplay());
			clipboard.setContents(new Object[] { details.fullUrl() }, new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
		});

		openBrowser(details.fullUrl());
	}

	private void initUnsupported(Composite parent) {
		var warning = new Label(parent, SWT.NONE);
		warning.setText("No authorisation configured on this instance of GDA");
	}

	private void openBrowser(String url) {
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URI(url).toURL());
		} catch (PartInitException | MalformedURLException | URISyntaxException e) {
			logger.error("Failed to open the external browser with the provided URL", e);
		}
	}

	@Subscribe
	private void event(BlueApiEvent evt) {
		logger.debug("Received event: {}", evt);
		if (alive && evt.matches(clientId, details.session())) {
			logger.debug("Message from server: {}", evt);
			var message = switch (evt) {
			case BlueApiEvent.Login login -> "Logged in as %s".formatted(login.user());
			case BlueApiEvent.Denied denied -> "User denied access - please try again if blueAPI authentication is required";
			case BlueApiEvent.Error err -> "Login failed - please try again%n%s".formatted(err.message());
			case BlueApiEvent.Timeout timeout -> "Login timed out - please try again";
			case BlueApiEvent.Logout logout -> "User was logged out while logging in - please raise issue";
			};
			showOutcomeMessage(message);
		}
	}

	private void showOutcomeMessage(String message) {
		var parent = (Composite) getDialogArea();
		parent.getDisplay().asyncExec(() -> {
			for (var child : parent.getChildren()) {
				child.dispose();
			}
			var messageLabel = new Label(parent, SWT.NONE);
			messageLabel.setText(message);

			// Pretend cancel button is ok for a while
			getButton(IDialogConstants.CANCEL_ID).setText(IDialogConstants.OK_LABEL);

			parent.requestLayout();
		});
	}

	@Override
	public boolean close() {
		alive = false;
		cancelLogin();
		return super.close();
	}

	private void cancelLogin() {
		if (details == null) {
			return;
		}
		ServiceProvider.getOptionalService(BlueApiAuth.class).ifPresent(a -> {
			try {
				a.cancelLogin(clientId, details.session());
			} catch (Exception e) {
				// we tried
				logger.debug("Failed to cancel login attempt - it will timeout eventually");
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
}
