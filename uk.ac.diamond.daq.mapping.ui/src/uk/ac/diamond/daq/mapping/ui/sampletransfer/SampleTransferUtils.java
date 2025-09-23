/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.gda.core.sampletransfer.SampleSelection;

public class SampleTransferUtils {
	public static final Color COLOUR_GREY = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	public static final Color COLOUR_BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	public static final Color DEFAULT_COLOUR = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

	// dialog right side settings
	public static final int WIDTH = 1000;
	public static final int STATE_COMPOSITE_WIDTH = 320;
	public static final int STEPS_COMPOSITE_WIDTH = WIDTH - STATE_COMPOSITE_WIDTH;

	public static final int HEIGHT = 900;
	public static final int SECTION_HEIGHT = 50;
	public static final int STEP_HEIGHT = 35;

	public static final int TITLE_LABEL_WIDTH = 160;
	public static final int TRANSITION_BUTTONS_HEIGHT = 100;
	public static final int TRANSITION_BUTTONS_WIDTH = 120;

	public static final int BUTTON_WIDTH = 70;


	private SampleTransferUtils()  {
		// Prevent instantiation
	}

	public static void displayError(String text, String message, Exception ex, Logger logger) {
		logger.error(message, ex);
		final MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
		messageBox.setText(text);
		messageBox.setMessage(String.format("%s: %s%nSee log file for more information", message, ex));
		messageBox.open();
	}

	public static Composite composite(Composite parent, int columns) {
		var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
        GridLayoutFactory.fillDefaults().numColumns(columns).applyTo(composite);
        return composite;
	}

	public static Composite composite(Composite parent, int columns, int width, int height) {
		var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
        GridDataFactory.fillDefaults().hint(width, height).applyTo(composite);
        GridLayoutFactory.fillDefaults().numColumns(columns).applyTo(composite);
        return composite;
	}

	public static Label icon(Composite parent, String imageName) {
		var stateIcon = LabelFactory.newLabel(SWT.NONE).create(parent);
		var image = Activator.getImage(imageName);
		stateIcon.setImage(image);
		return stateIcon;
	}

	public static ComboViewer comboViewer(Composite parent) {
		GridData gridData = new GridData();
		gridData.widthHint = 70;

		var combo = new ComboViewer(parent);
		combo.getCombo().setLayoutData(gridData);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		var values = Arrays.stream(SampleSelection.values()).filter(sample -> !sample.equals(SampleSelection.NONE)).toList();
		combo.setInput(values);
		combo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SampleSelection) element).name();
			}
		});
		return combo;
	}

	public static Text text(Composite parent) {
		GridData gridData = new GridData();
		gridData.widthHint = 70;
		var text = TextFactory.newText(SWT.BORDER).create(parent);
		text.setEnabled(false);
		text.setLayoutData(gridData);
		return text;
	}

	public static void createSeparator(Composite parent) {
	    Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
	    if (parent.getLayout() instanceof GridLayout gridLayout) {
	    	   GridDataFactory.fillDefaults().grab(true, false).span(gridLayout.numColumns, 1).applyTo(separator);
	    }
	}

	public static Button createStartStopButton(Composite composite, String text) {
		var button = ButtonFactory.newButton(SWT.BORDER).create(composite);
		button.setText(text);
		button.setEnabled(false);
		return button;
	}

	public static Button createButtonWithLayoutData(Composite composite, String text) {
		var button = ButtonFactory.newButton(SWT.BORDER).create(composite);
		var gridData = new GridData();
		gridData.widthHint = 70;
		button.setLayoutData(gridData);
		button.setText(text);
		return button;
	}

	public static Composite createButtonsComposite(Composite parent, String text) {
		var container = composite(parent, 2, STATE_COMPOSITE_WIDTH, SECTION_HEIGHT);
		createTitle(container, text);
		var gridData = new GridData(SWT.LEFT, SWT.LEFT, true, false);
		gridData.heightHint = TRANSITION_BUTTONS_HEIGHT;
		container.setLayoutData(gridData);
		return container;
	}

	public static void createTitle(Composite parent, String text) {
		var title = LabelFactory.newLabel(SWT.NONE).create(parent);
		GridDataFactory.swtDefaults().hint(TITLE_LABEL_WIDTH, SWT.DEFAULT).applyTo(title);
		title.setText(text);
		var titleFont = new Font(Display.getCurrent(), new FontData("Dialog", 12, SWT.BOLD));
		title.setFont(titleFont);
	}

	public static Text createSectionWithText(Composite parent) {
		var textBox = TextFactory.newText(SWT.WRAP | SWT.MULTI | SWT.READ_ONLY).create(parent);
		textBox.setBackground(DEFAULT_COLOUR);
		var gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.widthHint = STATE_COMPOSITE_WIDTH;
		textBox.setLayoutData(gridData);
		return textBox;
	}

	public static Label createSectionWithLabel(Composite parent, String text) {
		var container = composite(parent, 2, STATE_COMPOSITE_WIDTH, SECTION_HEIGHT);
		createTitle(container, text);
		var label = LabelFactory.newLabel(SWT.NONE).create(container);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return label;
	}

	public static String formatWord(String word) {
		return word.charAt(0) + word.substring(1).toLowerCase().replace("_", " ");
	}

	public static GridData gridData(int widthHint) {
	    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
	    gridData.widthHint = widthHint;
	    gridData.horizontalSpan = 1;
	    gridData.horizontalAlignment = GridData.FILL;
	    return gridData;
	}

	public static String formatTextToFitWidth(String text) {
	    int maxCharsPerLine = STATE_COMPOSITE_WIDTH / 2;
	    StringBuilder formattedText = new StringBuilder();
	    String[] words = text.split(" ");
	    StringBuilder line = new StringBuilder();

	    for (String word : words) {
	        if (line.length() + word.length() + 1 > maxCharsPerLine) {
	            formattedText.append(line.toString().trim()).append("\n");
	            line = new StringBuilder();
	        }
	        line.append(word).append(" ");
	    }
	    formattedText.append(line.toString().trim());

	    return formattedText.toString();
	}

	public static Label createColumnHeader(Composite parent, String text, int widthHint) {
        var label = new Label(parent, SWT.NONE);
        label.setText(text);

        GridData labelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        labelData.widthHint = widthHint;
        labelData.grabExcessHorizontalSpace = true;

        label.setLayoutData(labelData);
        return label;
    }

	public static GridData createGridData(int hAlign, int vAlign, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace, int widthHint, int heightHint) {
        GridData gridData = new GridData(hAlign, vAlign, grabExcessHorizontalSpace, grabExcessVerticalSpace);
        gridData.widthHint = widthHint;
        gridData.heightHint = heightHint;
        return gridData;
    }

}
