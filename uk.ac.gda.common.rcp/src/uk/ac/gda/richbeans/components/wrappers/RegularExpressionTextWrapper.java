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

package uk.ac.gda.richbeans.components.wrappers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import swing2swt.layout.BorderLayout;

import com.swtdesigner.SWTResourceManager;

public class RegularExpressionTextWrapper extends TextWrapper {

	protected CLabel messageLabel;
	protected Image errorImage, nameImage;
	protected Pattern pattern;
	private ModifyListener modifyListener;
	private boolean toolTipTextOverridden = false;

	/**
	 * Create widget which checks name defined is a findable instance of the class passed in.
	 * 
	 * @param parent
	 * @param style
	 * @param pattern
	 */
	public RegularExpressionTextWrapper(Composite parent, int style, final Pattern pattern) {

		super(parent, style);

		this.pattern = pattern;
		this.messageLabel = new CLabel(this, SWT.NONE);
		messageLabel.setLayoutData(BorderLayout.EAST);

		this.errorImage = SWTResourceManager.getImage(RegularExpressionTextWrapper.class, "/icons/error.png");
		this.nameImage = SWTResourceManager.getImage(RegularExpressionTextWrapper.class, "/icons/tick.png");

		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						checkValue();
					}
				});
			}
		};
		text.addModifyListener(modifyListener);
	}

	@Override
	public void dispose() {
		if (text != null && !text.isDisposed()) {
			text.removeModifyListener(modifyListener);
			text.dispose();
		}
		super.dispose();
	}

	@Override
	public void setValue(final Object value) {
		super.setValue(value);
		checkValue();
	}

	protected void checkValue() {

		if (!isActivated() || !isOn())
			return;
		final Object newValue = getValue();
		if (newValue == null || "".equals(newValue)) {
			setWrongValue(newValue);
			return;
		}

		try {
			final Matcher matcher = pattern.matcher((String) newValue);
			if (matcher.matches()) {
				setRightValue(newValue);
			} else {
				setWrongValue(newValue);
			}
		} catch (Exception ne) {
			setWrongValue(null);
		}

		layout();
		redraw();
	}

	protected void setRightValue(Object value) {
		messageLabel.setImage(getNameImage());
		text.setForeground(BLACK);
		if (!toolTipTextOverridden) {
			setToolTipText("The value '" + value + "' is valid.");
		}
	}

	protected void setWrongValue(Object value) {
		messageLabel.setImage(getErrorImage());
		text.setForeground(RED);
		if (!toolTipTextOverridden) {
			setToolTipText("The value '" + value + "' is not valid.");
		}
	}

	@Override
	public void setToolTipText(final String text) {
		super.setToolTipText(text);
		this.messageLabel.setToolTipText(text);
		this.text.setToolTipText(text);
		toolTipTextOverridden = true;
	}

	public Image getErrorImage() {
		return errorImage;
	}

	public void setErrorImage(Image errorImage) {
		this.errorImage = errorImage;
	}

	public Image getNameImage() {
		return nameImage;
	}

	public void setNameImage(Image nameImage) {
		this.nameImage = nameImage;
	}

}
