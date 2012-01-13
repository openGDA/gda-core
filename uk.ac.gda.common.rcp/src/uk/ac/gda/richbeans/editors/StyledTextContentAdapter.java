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

package uk.ac.gda.richbeans.editors;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * An {@link IControlContentAdapter} for SWT Text controls. This is a
 * convenience class for easily creating a {@link ContentProposalAdapter} for
 * text fields.
 * 
 * @since 3.2
 */
public class StyledTextContentAdapter implements IControlContentAdapter,IControlContentAdapter2 {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter#getControlContents(org.eclipse
	 * .swt.widgets.Control)
	 */
	@Override
	public String getControlContents(Control control) {
		return ((StyledText)control).getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter#getCursorPosition(org.eclipse
	 * .swt.widgets.Control)
	 */
	@Override
	public int getCursorPosition(Control control) {
		return ((StyledText)control).getCaretOffset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter#getInsertionBounds(org.eclipse
	 * .swt.widgets.Control)
	 */
	@Override
	public Rectangle getInsertionBounds(Control control) {
		StyledText text= (StyledText)control;
		Point caretOrigin= text.getLocationAtOffset(text.getCaretOffset());
		return new Rectangle(caretOrigin.x + text.getClientArea().x, caretOrigin.y + text.getClientArea().y + 3, 1, text.getLineHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter#insertControlContents(org.eclipse
	 * .swt.widgets.Control, java.lang.String, int)
	 */
	@Override
	public void insertControlContents(Control control, String contents, int cursorPosition) {
		StyledText text= ((StyledText)control);
		text.insert(contents);
		cursorPosition= Math.min(cursorPosition, contents.length());
		text.setCaretOffset(text.getCaretOffset() + cursorPosition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter#setControlContents(org.eclipse
	 * .swt.widgets.Control, java.lang.String, int)
	 */
	@Override
	public void setControlContents(Control control, String term, int cursorPosition) {
		
		String text = ((StyledText)control).getText();
		
		final String after = text.substring(((StyledText)control).getCaretOffset(), text.length());
		text = text.substring(0, ((StyledText)control).getCaretOffset());
		
		// We just add the maximum ammount matched from the term
		for (int i = term.length(); i > 0; i--) {
			final String subterm = term.substring(0,i);
			if (text.endsWith(subterm)) {
				term = term.substring(i,term.length());
				break;
			}
		}
		
		final StringBuffer buf = new StringBuffer();
		buf.append(text);
		buf.append(term);
		final int len = buf.length();
		buf.append(after);
		((StyledText)control).setText(buf.toString());
		((StyledText)control).setCaretOffset(len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter#setCursorPosition(org.eclipse
	 * .swt.widgets.Control, int)
	 */
	@Override
	public void setCursorPosition(Control control, int index) {
		((StyledText)control).setCaretOffset(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter2#getSelection(org.eclipse.swt
	 * .widgets.Control)
	 */
	@Override
	public Point getSelection(Control control) {
		return ((StyledText)control).getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.fieldassist.IControlContentAdapter2#setSelection(org.eclipse.swt
	 * .widgets.Control, org.eclipse.swt.graphics.Point)
	 */
	@Override
	public void setSelection(Control control, Point range) {
		((StyledText)control).setSelection(range);
	}
}
