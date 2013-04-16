/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.views.baton;

import java.text.SimpleDateFormat;
import java.util.List;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.UserMessage;
import gda.observable.IObserver;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MessageView extends ViewPart implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(MessageView.class);

	/**
	 * 
	 */
	public static final String ID = "uk.ac.gda.rcp.views.baton.MessageView"; //$NON-NLS-1$
	
	private StyledText history;
	private Text text;
	private KeyListener keyListener;

	private Button btnSend;

	private SelectionAdapter selectionListener;

	@Override
	public void init(final IViewSite site) throws PartInitException {
		super.init(site);
		try {
		    InterfaceProvider.getJSFObserver().addIObserver(this);
		} catch (Exception e) {
			throw new PartInitException("Cannot attach to Jython Server", e);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		
/*		if (!LocalProperties.isAccessControlEnabled()) {
			final Label error = new Label(parent, SWT.NONE);
			error.setText();
			return;
		}*/
		

		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		history = new StyledText(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SEARCH | SWT.CANCEL | SWT.MULTI | SWT.WRAP);
		history.setEditable(false);
		history.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData textCompositeData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		textCompositeData.minimumHeight = 35;
		textCompositeData.heightHint = 35;
		composite.setLayoutData(textCompositeData);
		
		text = new Text(composite, SWT.BORDER | SWT.MULTI);
		text.setToolTipText("Enter message and ENTER to send.");
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		keyListener = new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character=='\r') {
					sendMessage();
					e.doit = false;
				}
			}
		};
		text.addKeyListener(keyListener);
		text.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				text.removeKeyListener(keyListener);
			}
		});
		
		btnSend = new Button(composite, SWT.NONE);
		btnSend.setText("Send");
		selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendMessage();
			}
		};
		
		btnSend.addSelectionListener(selectionListener);
		
		btnSend.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				btnSend.removeSelectionListener(selectionListener);
			}
		});
		
		if(!LocalProperties.isBatonManagementEnabled()){
			UserMessage msg = new UserMessage(-1, "", "Baton control is not enabled for this beam line.");
			addUserMessageText(msg);
		}
		else {
			List<UserMessage> oldMessages = InterfaceProvider.getBatonStateProvider().getMessageHistory();
			if (oldMessages != null) {
				for (UserMessage msg : oldMessages) {
					addUserMessageText(msg);
				}
				scrollToEndOfHistory();
			}
		}
	}
	
	private void sendMessage() {
		InterfaceProvider.getBatonStateProvider().sendMessage(text.getText());
		text.setText("");
	}

	@Override
	public void setFocus() {
		this.text.setFocus();
	}
	
	@Override
	public void dispose() {
		try {
			InterfaceProvider.getJSFObserver().deleteIObserver(this);
		} catch (Exception e) {
			logger.error("Cannot reomve MessageView from JythonServerFacade", e);
		}
		super.dispose();
	}
	

	protected void addUserMessageText(UserMessage message) {
		
		if (history.getCharCount() > 0) {
			// add newline to end of previous message - if there is one
			history.append("\n");
		}
		
		StyleRange style = new StyleRange();
		style.font = new Font(history.getDisplay(), "Monospace", 0, SWT.NORMAL);
		style.start = history.getCharCount();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateTime = String.format("[%s]", dateFormat.format(message.getTimestamp()));
		style.length = dateTime.length();
		history.append(dateTime);
		history.setStyleRange(style);
		
		style = new StyleRange();
		style.fontStyle = SWT.BOLD;
		style.start = history.getCharCount();
		String prefix = String.format(" %s: ", message.getSourceUsername());
		style.length = prefix.length();
		history.append(prefix);
		history.setStyleRange(style);

		style = new StyleRange();
		style.start = history.getCharCount();
		style.fontStyle = SWT.BOLD;
		// CadetBlue from http://www.wilsonmar.com/1colors.htm#TopMenu
		style.foreground = new Color(this.getSite().getShell().getDisplay(),95,158,160); 
		history.append(" "); history.append(message.getMessage());
		style.length = 1 + message.getMessage().length();
		history.setStyleRange(style);
	}
	
	private void scrollToEndOfHistory() {
		history.setTopIndex(history.getLineCount() - 1);
	}

	@Override
	public void update(final Object theObserved, final Object changeCode) {
		if (changeCode instanceof UserMessage) {
			this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					UserMessage message = (UserMessage) changeCode;
					addUserMessageText(message);
					scrollToEndOfHistory();
				}
			});	
		}
	}

}
