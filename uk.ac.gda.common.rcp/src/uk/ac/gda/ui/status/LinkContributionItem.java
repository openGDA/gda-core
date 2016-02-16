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
import org.eclipse.swt.custom.CLabel;
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
public class LinkContributionItem extends ContributionItem {

	private final static int DEFAULT_CHAR_WIDTH = 40;

	private int charWidth;

	private CLabel link;

	/**
	 * The composite into which this contribution item has been placed. This
	 * will be <code>null</code> if this instance has not yet been
	 * initialized.
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
	 *            the contribution item's id, or <code>null</code> if it is to
	 *            have no id
	 */
	public LinkContributionItem(String id) {
		this(id, DEFAULT_CHAR_WIDTH);
	}

	/**
	 * Creates a status line contribution item with the given id that displays
	 * the given number of characters.
	 * 
	 * @param id
	 *            the contribution item's id, or <code>null</code> if it is to
	 *            have no id
	 * @param charWidth
	 *            the number of characters to display
	 */
	public LinkContributionItem(String id, int charWidth) {
		super(id);
		this.charWidth = charWidth;
		setVisible(false); // no text to start with
	}

	@Override
	public void fill(Composite parent) {
		statusLine = parent;

		Label sep = new Label(parent, SWT.SEPARATOR);
		link = new CLabel(statusLine, SWT.SHADOW_NONE);

		if (mouseListener!=null) {
			link.addMouseListener(mouseListener);
		}
		
		if (image !=null) {
			link.setImage(image);
		}
		
		if (tooltip !=null) {
			link.setToolTipText(tooltip);
		}
		
		if (widthHint < 0) {
			GC gc = new GC(statusLine);
			gc.setFont(statusLine.getFont());
			FontMetrics fm = gc.getFontMetrics();
			widthHint = fm.getAverageCharWidth() * charWidth;
			heightHint = fm.getHeight();
			gc.dispose();
		}

		StatusLineLayoutData data = new StatusLineLayoutData();
		data.widthHint = widthHint;
		link.setLayoutData(data);
		link.setText(text);

		data = new StatusLineLayoutData();
		data.heightHint = heightHint;
		sep.setLayoutData(data);
	}

	/**
	 * An accessor for the current location of this status line contribution
	 * item -- relative to the display.
	 * 
	 * @return The current location of this status line; <code>null</code> if
	 *         not yet initialized.
	 */
	public Point getDisplayLocation() {
		if ((link != null) && (statusLine != null)) {
			return statusLine.toDisplay(link.getLocation());
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

		if (link != null && !link.isDisposed()) {
			link.setText(this.text);
		}

		updateManager();
	}

	public void setBackground(Color background) {
		if (link != null && !link.isDisposed()) {
			link.setBackground(background);
		}
	}

	private void updateManager() {
		if (this.text.length() == 0) {
			if (isVisible()) {
				setVisible(false);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		} else {
			if (!isVisible()) {
				setVisible(true);
				IContributionManager contributionManager = getParent();

				if (contributionManager != null) {
					contributionManager.update(true);
				}
			}
		}
	}

	private String escape(String text) {
		return Util.replaceAll(text, "&", "&&");  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * @param txt
	 */
	public void setToolTipText(String txt) {
		Assert.isNotNull(text);
		
		tooltip = escape(txt);
		
		if (link != null && !link.isDisposed()) {
			link.setToolTipText(txt);
		}
		updateManager();

	}

	/**
	 * @param image
	 */
	public void setImage(Image image) {
		
		this.image = image;
		
		if (link != null && !link.isDisposed()) {
			this.link.setImage(image);
		} 
		updateManager();
	
	}

	/**
	 * @param l
	 */
	public void addMouseListener(MouseListener l) {
		if (link!=null) {
			this.link.addMouseListener(l);
			this.mouseListener = null;
		} else {
			this.mouseListener = l;
		}
	}
	
	
}
