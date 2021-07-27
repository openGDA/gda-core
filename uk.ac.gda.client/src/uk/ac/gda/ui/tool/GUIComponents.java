/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool;

import static uk.ac.gda.ui.tool.ClientSWTElements.DEFAULT_LABEL_SIZE;
import static uk.ac.gda.ui.tool.ClientSWTElements.DEFAULT_TEXT_SIZE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;
import static uk.ac.gda.ui.tool.ClientSWTElements.createCombo;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyDoubleText;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyIntegerText;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyPositiveDoubleText;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyPositiveIntegerText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Common components for the GUI.
 *
 * @author Maurizio Nagni
 */
public class GUIComponents {

	private GUIComponents() {
	}

	/**
	 * Returns a label/text pair for a text field with no validation
	 * @param parent
	 * @param labelStyle
	 * @param textStyle
	 * @param labelMsg the text label. If {@code null} the label is not associated
	 * @param textTooltip
	 * @return the text widget
	 */
	public static Text textContent(Composite parent, int labelStyle, int textStyle, ClientMessages labelMsg, ClientMessages textTooltip) {
		return verifiedContent(parent, labelStyle, textStyle, labelMsg, textTooltip, null);
	}

	/**
	 * Returns a label/text pair for a text field validated for positive integers
	 * @param parent
	 * @param labelStyle
	 * @param textStyle
	 * @param labelMsg the text label. If {@code null} the label is not associated
	 * @param textTooltip
	 * @return the text widget
	 */
	public static Text integerContent(Composite parent, int labelStyle, int textStyle,
			ClientMessages labelMsg, ClientMessages textTooltip) {
		return verifiedContent(parent, labelStyle, textStyle, labelMsg, textTooltip, verifyOnlyIntegerText);
	}

	/**
	 * Returns a label/text pair for a text field validated for positive integers
	 * @param parent
	 * @param labelStyle
	 * @param textStyle
	 * @param labelMsg the text label. If {@code null} the label is not associated
	 * @param textTooltip
	 * @return the text widget
	 */
	public static Text integerPositiveContent(Composite parent, int labelStyle, int textStyle,
			ClientMessages labelMsg, ClientMessages textTooltip) {
		return verifiedContent(parent, labelStyle, textStyle, labelMsg, textTooltip, verifyOnlyPositiveIntegerText);
	}

	/**
	 * Returns a label/text pair for a text field validated for double
	 * @param parent
	 * @param labelStyle
	 * @param textStyle
	 * @param labelMsg the text label. If {@code null} or {@link ClientMessages#EMPTY_MESSAGE} the label is not associated
	 * @param textTooltip
	 * @return the text widget
	 */
	public static Text doubleContent(Composite parent, int labelStyle, int textStyle,
			ClientMessages labelMsg, ClientMessages textTooltip) {
		return doubleContent(parent, labelStyle, textStyle, ClientMessagesUtility.getMessage(labelMsg), textTooltip);
	}

	/**
	 * Returns a label/text pair for a text field validated for double
	 * @param parent
	 * @param labelStyle
	 * @param textStyle
	 * @param labelMsg the text label. If {@code null} or {@code length == 0} the label is not associated
	 * @param textTooltip
	 * @return the text widget
	 */
	public static Text doubleContent(Composite parent, int labelStyle, int textStyle,
			String labelMsg, ClientMessages textTooltip) {
		return verifiedContent(parent, labelStyle, textStyle, labelMsg, textTooltip, verifyOnlyDoubleText);
	}

	/**
	 * Returns a label/text pair for a text field validated for double positive
	 * @param parent
	 * @param labelStyle
	 * @param textStyle
	 * @param labelMsg the text label. If {@code null} the label is not associated
	 * @param textTooltip
	 * @return the text widget
	 */
	public static Text doublePositiveContent(Composite parent, int labelStyle, int textStyle,
			ClientMessages labelMsg, ClientMessages textTooltip) {
		return verifiedContent(parent, labelStyle, textStyle, labelMsg, textTooltip, verifyOnlyPositiveDoubleText);
	}

	private static Text verifiedContent(Composite parent, int labelStyle, int textStyle,
			ClientMessages labelMsg, ClientMessages textTooltip, VerifyListener verifyListener) {
		return verifiedContent(parent, labelStyle, textStyle, ClientMessagesUtility.getMessage(labelMsg), textTooltip, verifyListener);
	}

	private static Text verifiedContent(Composite parent, int labelStyle, int textStyle,
			String labelMsg, ClientMessages textTooltip, VerifyListener verifyListener) {
		var mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 2);
		createClientGridDataFactory().grab(true, true).applyTo(mainComposite);

		if (labelMsg != null && labelMsg.length() > 0) {
			labelComponent(mainComposite,  labelStyle, labelMsg);
		}

		var text = createClientText(mainComposite, textStyle, textTooltip, verifyListener);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(text);
		return text;
	}

	/**
	 * Returns a label/label pair where the latter may be modified programmatically
	 * @param parent
	 * @param labelStyle
	 * @return the second label widget
	 */
	public static Label labelledLabelContent(Composite parent, int labelStyle, ClientMessages firstLabelMsg, ClientMessages secondLabelMsg) {
		var mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 2);
		createClientGridDataFactory().grab(true, true).applyTo(mainComposite);

		labelComponent(mainComposite, labelStyle, firstLabelMsg);
		return labelComponent(mainComposite, labelStyle, secondLabelMsg);
	}

	/**
	 * Returns a label/label pair where the latter may be modified programmatically
	 * @param parent
	 * @param labelStyle
	 * @return the second label widget
	 */
	public static Combo labelledComboContent(Composite parent, int labelStyle, ClientMessages labelMsg, ClientMessages comboTooltip) {
		var mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 2);
		createClientGridDataFactory().grab(true, true).applyTo(mainComposite);

		labelComponent(mainComposite, labelStyle, labelMsg);
		return createCombo(mainComposite, SWT.READ_ONLY, new String[0], comboTooltip);
	}

	/**
	 * Returns a label aligned on the left(h), center(v), indented 5 on the right with {@code ClientSWTElements#DEFAULT_LABEL_SIZE} width
	 * @param parent
	 * @param labelStyle
	 * @param labelMsg
	 * @return the label widget
	 */
	public static Label labelComponent(Composite parent,  int labelStyle, ClientMessages labelMsg) {
		return labelComponent(parent,  labelStyle, ClientMessagesUtility.getMessage(labelMsg));
	}

	/**
	 * Returns a label aligned on the left(h), center(v), indented 5 on the right with {@code ClientSWTElements#DEFAULT_LABEL_SIZE} width
	 * @param parent
	 * @param labelStyle
	 * @param labelMsg
	 * @return the label widget
	 */
	public static Label labelComponent(Composite parent,  int labelStyle, String labelMsg) {
		var label = createClientLabel(parent, labelStyle, labelMsg);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_LABEL_SIZE).indent(5, SWT.DEFAULT).applyTo(label);
		return label;
	}

	/**
	 * Returns a radio aligned on the left(h), center(v), indented 5 on the right
	 * @param parent
	 * @param buttonMsg
	 * @param tooltip
	 * @return the label widget
	 */
	public static Button radioComponent(Composite parent, ClientMessages buttonMsg, ClientMessages tooltip) {
		return buttonComponent(parent, buttonMsg, tooltip,  SWT.RADIO);
	}

	/**
	 * Returns a checkbox aligned on the left(h), center(v), indented 5 on the right
	 * @param parent
	 * @param buttonMsg
	 * @param tooltip
	 * @return the label widget
	 */
	public static Button checkComponent(Composite parent, ClientMessages buttonMsg, ClientMessages tooltip) {
		return buttonComponent(parent, buttonMsg, tooltip,  SWT.CHECK);
	}

	private static Button buttonComponent(Composite parent, ClientMessages buttonMsg, ClientMessages tooltip, int style) {
		var button = createClientButton(parent, style, buttonMsg, tooltip);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(button);
		return button;
	}
}