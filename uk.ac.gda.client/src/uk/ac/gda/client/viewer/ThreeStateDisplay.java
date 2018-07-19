/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.viewer;

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Minimal widget capable of representing three states in the form of green, yellow or red icon plus a state message.
 * <p>
 * The messages for each state are set in the constructor, and the methods {@link #setGreen()}, {@link #setYellow()}
 * and {@link #setRed()} are called to toggle between states.
 */
public class ThreeStateDisplay {

	private static final String ICONS_DIR = "/icons/status/";

	private static final Image ICON_GREEN = new Image(Display.getDefault(), ThreeStateDisplay.class.getResourceAsStream(ICONS_DIR + "green.png"));
	private static final Image ICON_YELLOW = new Image(Display.getDefault(), ThreeStateDisplay.class.getResourceAsStream(ICONS_DIR + "yellow.png"));
	private static final Image ICON_RED = new Image(Display.getDefault(), ThreeStateDisplay.class.getResourceAsStream(ICONS_DIR + "red.png"));

	private final String messageGreen;
	private final String messageYellow;
	private final String messageRed;

	private final Label icon;
	private final Label text;

	/**
	 * The message for each state is assigned on instantiation. {@code null}s converted to empty strings.
	 * <p>
	 * Initial state is green.
	 *
	 * @param parent the composite on which to draw the widget
	 * @param messageGreen the message displayed after calling {@link #setGreen()}
	 * @param messageYellow the message displayed after calling {@link #setYellow()}
	 * @param messageRed the message displayed after calling {@link #setRed()}
	 */
	public ThreeStateDisplay(Composite parent, String messageGreen, String messageYellow, String messageRed) {
		this.messageGreen = messageGreen == null ? "" : messageGreen;
		this.messageYellow = messageYellow == null ? "" : messageYellow;
		this.messageRed = messageRed == null ? "" : messageRed;

		Composite display = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(display);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(1, SWT.DEFAULT).applyTo(display);

		icon = new Label(display, SWT.NONE);
		text = new Label(display, SWT.NONE);
		calculateMinimumSize();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(text);

		setGreen();
	}

	/**
	 * Display green icon and corresponding message
	 */
	public void setGreen() {
		icon.setImage(ICON_GREEN);
		setMessage(messageGreen);
	}

	/**
	 * Display yellow icon and corresponding message
	 */
	public void setYellow() {
		icon.setImage(ICON_YELLOW);
		setMessage(messageYellow);
	}

	/**
	 * Display red icon and corresponding message
	 */
	public void setRed() {
		icon.setImage(ICON_RED);
		setMessage(messageRed);
	}

	private void setMessage(String message) {
		// there may be a use case for this to be made API
		// to e.g. stay in one state but change the message
		// but the widgets would need to dynamically resize
		if (message == null) return;
		text.setText(message);
	}

	private void calculateMinimumSize() {
		String longestMessage = Arrays.asList(messageGreen, messageYellow, messageRed).stream()
									.reduce((a, b) -> a.length() > b.length() ? a : b).orElse("");

		text.setText(longestMessage);
		text.setText("");
	}
}
