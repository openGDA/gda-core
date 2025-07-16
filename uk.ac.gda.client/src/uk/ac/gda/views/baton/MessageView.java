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

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.UserMessage;
import gda.rcp.GDAClientActivator;

/**
 *
 */
public final class MessageView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(MessageView.class);
	private static final boolean PLAY_SOUNDS = LocalProperties.check("gda.messageView.playAudioNotification");

	public static final String ID = "uk.ac.gda.rcp.views.baton.MessageView";

	private final Function<Composite, StyledText> historyFactory;
	private final Function<Composite, Text> textFactory;
	private final Function<UserMessage, Runnable> requestForActionInResponseToMessage;
	private Optional <Runnable> setFocusAction = Optional.empty();
	private StyledText history;

	public MessageView() {
		Consumer<UserMessage> messageReaction =
			PLAY_SOUNDS ? this::reactToMessagesWithSound : this::reactToMessagesSilently;
		requestForActionInResponseToMessage = usrMsg -> () -> messageReaction.accept(usrMsg);
		historyFactory =
			container -> new StyledText(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SEARCH | SWT.CANCEL | SWT.MULTI | SWT.WRAP);
		textFactory =
			composite -> new Text(composite, SWT.BORDER | SWT.MULTI);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		try {
			InterfaceProvider.getJSFObserver().addIObserver(this::update);
		} catch (Exception e) {
			throw new PartInitException("Cannot attach to Jython Server", e);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		var container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());

		history = historyFactory.apply(container);
		history.setEditable(false);
		history.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		var composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData textCompositeData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		textCompositeData.minimumHeight = 35;
		textCompositeData.heightHint = 35;
		composite.setLayoutData(textCompositeData);

		var text = textFactory.apply(composite);
		text.setToolTipText("Enter message and ENTER to send.");
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		setFocusAction = Optional.of(text::setFocus);

		var keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character=='\r') {
					sendMessage(text);
					e.doit = false;
				}
			}
		};
		text.addKeyListener(keyListener);
		text.addDisposeListener( e -> {
			text.removeKeyListener(keyListener);
			setFocusAction = Optional.empty();
		});

		var sendButton = new Button(composite, SWT.NONE);
		sendButton.setText("Send");
		var sendButtonSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendMessage(text);
			}
		};

		sendButton.addSelectionListener(sendButtonSelectionListener);
		sendButton.addDisposeListener(e -> sendButton.removeSelectionListener(sendButtonSelectionListener) );

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

	private void sendMessage(Text text) {
		InterfaceProvider.getBatonStateProvider().sendMessage(text.getText());
		text.setText("");
	}

	@Override
	public void setFocus() {
		setFocusAction.ifPresent(Runnable::run);
	}

	@Override
	public void dispose() {
		try {
			InterfaceProvider.getJSFObserver().deleteIObserver(this::update);
		} catch (Exception e) {
			logger.error("Cannot reomve MessageView from JythonServerFacade", e);
		}
		super.dispose();
	}


	protected void addUserMessageText(UserMessage message) {
		applyNewlineToPriorMessage();
		appendTimestamp(message);
		identifyMessageAuthor(message);
		applyTextStyling(message);
	}

	private void applyNewlineToPriorMessage() {
		if (history.getCharCount() > 0) {
			// add newline to end of previous message - if there is one
			history.append("\n");
		}
	}

	private void applyTextStyling(UserMessage message) {
		var style = new StyleRange();
		style.start = history.getCharCount();
		style.fontStyle = SWT.BOLD;

		style.foreground = getForegroundColour();
		history.append(" "); history.append(message.getMessage());
		style.length = 1 + message.getMessage().length();
		history.setStyleRange(style);
	}

	private void identifyMessageAuthor(UserMessage message) {
		var style = new StyleRange();
		style.fontStyle = SWT.BOLD;
		style.start = history.getCharCount();
		String prefix = String.format(" %s: ", message.getSourceUsername());
		style.length = prefix.length();
		history.append(prefix);
		history.setStyleRange(style);
	}

	private void appendTimestamp(UserMessage message) {
		var style = new StyleRange();
		style.font = new Font(history.getDisplay(), "Monospace", 0, SWT.NORMAL);
		style.start = history.getCharCount();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateTime = String.format("[%s]", dateFormat.format(message.getTimestamp()));
		style.length = dateTime.length();
		history.append(dateTime);
		history.setStyleRange(style);
	}

	private Color getForegroundColour() {
		var siteShellDisplay = getSite().getShell()
										.getDisplay();
		// RGB of CadetBlue from http://www.wilsonmar.com/1colors.htm#TopMenu
		return new Color(siteShellDisplay, 95, 158, 160);
	}

	private void scrollToEndOfHistory() {
		history.setTopIndex(history.getLineCount() - 1);
	}

	private void update(@SuppressWarnings("unused") Object ignored, Object changeCode) {
		if (changeCode instanceof UserMessage userMessage) {
			var display = getDisplay();
			Runnable actionInResponseToMsg = requestForActionInResponseToMessage.apply(userMessage);
			var warningMessage = "Could not react to user message as Display resource unavailable for asynchronous access to the UI thread.";
			Runnable warnIfNoResponseIsPossible = () -> logger.warn(warningMessage);
			display.ifPresentOrElse(d -> d.asyncExec(actionInResponseToMsg),
									warnIfNoResponseIsPossible);
		}
	}

	private void reactToMessagesSilently(UserMessage message) {
		addUserMessageText(message);
		scrollToEndOfHistory();
	}

	private void reactToMessagesWithSound(UserMessage message) {
		reactToMessagesSilently(message);
		var display = getDisplay();
		display.ifPresent(d -> playSound("sounds/ringtone_sonar.wav", d::beep));
	}

	private static void playSound(String audioUrl, Runnable fallbackBeep) {
		var playedSound = false;
		var bundle = Platform.getBundle(GDAClientActivator.PLUGIN_ID); //get resource from a bundle
		var relativeResoucePath = new Path(audioUrl); //relative path of resource in a bundle
		var foundResources = FileLocator.find(bundle, relativeResoucePath, null); //find a resource in a bundle
			// Any jar packed files are copied to temporary location for access using File.
		try (var clip = AudioSystem.getClip()) {
			var fileURL = FileLocator.toFileURL(foundResources);
			playedSound = playAudioClip(clip, fileURL);
		} catch (LineUnavailableException | IOException e) {
			logger.error("Could not play message sound !", e);
		}
		if (!playedSound) {
			fallbackBeep.run();
		}
	}

	private static boolean playAudioClip(Clip clip, URL fileURL) throws IOException, LineUnavailableException {
		try (var urlSourcedStream = fileURL.openStream();
				var inputStream = AudioSystem.getAudioInputStream(urlSourcedStream)) {
			clip.open(inputStream);
			clip.start();
			return true;
		} catch (UnsupportedAudioFileException uafe) {
			logger.error("Message sound unavailable - unsupported audio", uafe);
			return false;
		}
	}

	private static Optional<Display> getDisplay() {
		var maybeDisplay = Display.getDefault();
		return Optional.ofNullable(maybeDisplay);
	}
}
