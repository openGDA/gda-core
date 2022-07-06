/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.status;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A contribution item to be used with status line managers.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 3.4
 */
public final class LinkContributionItem extends ContributionItem {

	private final static int DEFAULT_CHAR_WIDTH = 40;

	private int charWidth;

	private final LinkContributionWidget widget;

	/**
	 * The composite into which this contribution item has been placed. This will be <code>null</code> if this instance has not yet been initialized.
	 */
	private Composite statusLine = null;

	private String text = Util.ZERO_LENGTH_STRING;

	private int widthHint = -1;

	private int heightHint = -1;

	private MouseListener mouseListener;

	private Image image;

	private String tooltip;

	/**
	 * Creates a status line contribution item with the given id.
	 *
	 * @param id
	 *            the contribution item's id, or <code>null</code> if it is to have no id
	 * @param widget
	 *            object that wraps the required widget (CLabel, Button etc.
	 */
	public LinkContributionItem(String id, LinkContributionWidget widget) {
		this(id, widget, DEFAULT_CHAR_WIDTH);
	}

	/**
	 * Creates a status line contribution item with the given id that displays the given number of characters.
	 *
	 * @param id
	 *            the contribution item's id, or <code>null</code> if it is to have no id
	 * @param widget
	 *            object that wraps the required widget (CLabel, Button etc.
	 * @param charWidth
	 *            the number of characters to display
	 */
	public LinkContributionItem(String id, LinkContributionWidget widget, int charWidth) {
		super(id);
		this.widget = widget;
		this.charWidth = charWidth;
		setVisible(false); // no text to start with
	}

	@Override
	public void fill(Composite parent) {
		statusLine = parent;

		final Label sep = new Label(parent, SWT.SEPARATOR);
		widget.create(parent);

		if (mouseListener != null) {
			widget.addMouseListener(mouseListener);
		}

		if (image != null) {
			widget.setImage(image);
		}

		if (tooltip != null) {
			widget.setToolTipText(tooltip);
		}

		if (widthHint < 0) {
			final GC gc = new GC(statusLine);
			gc.setFont(statusLine.getFont());
			final FontMetrics fm = gc.getFontMetrics();
			widthHint = fm.getAverageCharWidth() * charWidth;
			heightHint = fm.getHeight();
			gc.dispose();
		}

		final StatusLineLayoutData linkLayoutData = new StatusLineLayoutData();
		linkLayoutData.widthHint = widthHint;
		widget.setLayoutData(linkLayoutData);
		widget.setText(text);

		final StatusLineLayoutData separatorLayoutData = new StatusLineLayoutData();
		separatorLayoutData.heightHint = heightHint;
		sep.setLayoutData(separatorLayoutData);
	}

	/**
	 * An accessor for the current location of this status line contribution item -- relative to the display.
	 *
	 * @return The current location of this status line; <code>null</code> if not yet initialized.
	 */
	public Point getDisplayLocation() {
		if (widget.isCreated() && statusLine != null) {
			return statusLine.toDisplay(widget.getLocation());
		}
		return null;
	}

	/**
	 * Retrieves the text that is being displayed in the status line.
	 *
	 * @return the text that is currently being displayed
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text to be displayed in the status line.
	 *
	 * @param text
	 *            the text to be displayed, must not be <code>null</code>
	 */
	public void setText(String text) {
		Assert.isNotNull(text);

		this.text = escape(text);

		if (widget.isCreated() && !widget.isDisposed()) {
			widget.setText(this.text);
		}

		updateManager();
	}

	public void setBackground(Color background) {
		if (widget.isCreated() && !widget.isDisposed()) {
			widget.setBackground(background);
		}
	}

	private void updateManager() {
		if (text.length() == 0) {
			if (isVisible()) {
				setVisible(false);
				final IContributionManager contributionManager = getParent();
				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		} else {
			if (!isVisible()) {
				setVisible(true);
				final IContributionManager contributionManager = getParent();
				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		}
	}

	private String escape(String text) {
		return Util.replaceAll(text, "&", "&&");
	}

	public void setToolTipText(String txt) {
		Assert.isNotNull(text);

		tooltip = escape(txt);

		if (widget.isCreated() && !widget.isDisposed()) {
			widget.setToolTipText(txt);
		}
		updateManager();

	}

	public void setImage(Image image) {
		this.image = image;

		if (widget.isCreated() && !widget.isDisposed()) {
			widget.setImage(image);
		}
		updateManager();
	}

	public void addMouseListener(MouseListener l) {
		if (widget.isCreated()) {
			widget.addMouseListener(l);
			mouseListener = null;
		} else {
			mouseListener = l;
		}
	}
}
