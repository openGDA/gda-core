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

package uk.ac.gda.beamline.synoptics.composites;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.swtdesigner.SWTResourceManager;

import gda.rcp.GDAClientActivator;

/**
 * Composite to show users the latest file collected by GDA and to allow them to select previous
 * files to review.
 * <p>
 * All file tracking/handling is handled by a {@link LatestFileController} leaving this composite
 * to just take user input.
 */
public class DetectorFileSelection extends Composite implements LatestFileDisplay {

	/** Key used to store the current index string in the text widget to allow display to change with focus */
	private static final String INDEX_SUFFIX_KEY = "suffix";
	/** Key used to store the suffix (total files) in the text widget to allow display to change with focus */
	private static final String CURRENT_INDEX_KEY = "current";
	/** Label shown on top level group */
	private static final String COMPOSITE_NAME = "Detector Files";

	private static final Image TO_LATEST = GDAClientActivator.getImageDescriptor("icons/control_end_blue.png").createImage();
	private static final Image TO_FIRST = GDAClientActivator.getImageDescriptor("icons/control_start_blue.png").createImage();
	private static final Image BACK_ONE = GDAClientActivator.getImageDescriptor("icons/control_rewind_blue.png").createImage();
	private static final Image FORWARD_ONE = GDAClientActivator.getImageDescriptor("icons/control_fastforward_blue.png").createImage();
	private static final Image PAUSE = GDAClientActivator.getImageDescriptor("icons/control_pause_blue.png").createImage();
	private static final Image RUN = GDAClientActivator.getImageDescriptor("icons/control_play_blue.png").createImage();

	private static final Color LIGHT = SWTResourceManager.getColor(SWT.COLOR_GRAY);

	/** Top level group holding all components and providing {@value #COMPOSITE_NAME} label */
	private Group top;

	/** Text box showing the path of the current file */
	private StyledText currentFileText;

	/** Text box showing the index of the current file */
	private StyledText currentFileIndexText;

	/** Controller to handle lists of available files and to plot the selected data */
	private LatestFileController controller;

	/** Button to jump to the first file collected */
	private Button firstButton;
	/** Button to select the previous file collected */
	private Button previousButton;
	/** Button to select the next file collected */
	private Button nextButton;
	/** Button to jump to the latest file collected */
	private Button latestButton;
	/** Checkbox to choose whether newly created files should be shown automatically */
	private Button autoUpdate;
	/** Checkbox to choose whether existing plots should be cleared before new data is plotted */
	private Button clearPlot;

	public DetectorFileSelection(Composite parent, int style, Function<DetectorFileSelection, LatestFileController> controllerFactory) {
		super(parent, style);
		requireNonNull(controllerFactory, "LatestFileController must not be null");
		setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		top = new Group(this, SWT.NONE);
		top.setText(COMPOSITE_NAME);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
		GridLayoutFactory.swtDefaults().applyTo(top);
		top.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		controller = controllerFactory.apply(this);
		addDisposeListener(e -> this.controller.shutdown());
		initialise();
		controller.initialise();
	}

	public void initialise() {
		currentFileText = new StyledText(top, SWT.BORDER | SWT.SINGLE);
		currentFileText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		currentFileText.addKeyListener(getKeyHandler(controller::setFilePath));

		createControls();
	}

	private void createControls() {
		Composite controls = new Composite(top, SWT.NONE);
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		controls.setLayout(new GridLayout(8, false));
		controls.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		firstButton = new Button(controls, SWT.PUSH);
		firstButton.setImage(TO_FIRST);
		firstButton.addSelectionListener(getSelectionHandler(controller::firstFile));

		previousButton = new Button(controls, SWT.PUSH);
		previousButton.setImage(BACK_ONE);
		previousButton.addSelectionListener(getSelectionHandler(controller::previousFile));

		currentFileIndexText = new StyledText(controls, SWT.BORDER | SWT.SINGLE);
		currentFileIndexText.setLayoutData(new GridData(80, SWT.DEFAULT));
		currentFileIndexText.addKeyListener(getKeyHandler(controller::setIndex));

		// Add focus listener so that total file count can be hidden while entering new values
		currentFileIndexText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				String text = Optional.ofNullable(currentFileIndexText.getData(CURRENT_INDEX_KEY)).orElse("").toString();
				currentFileIndexText.setText(text);
				currentFileIndexText.setSelection(0, text.length());
			}
			@Override
			public void focusLost(FocusEvent e) {
				formatFileIndex((String)currentFileIndexText.getData(CURRENT_INDEX_KEY), (String)currentFileIndexText.getData(INDEX_SUFFIX_KEY), false);
			}
		});

		nextButton = new Button(controls, SWT.PUSH);
		nextButton.setImage(FORWARD_ONE);
		nextButton.addSelectionListener(getSelectionHandler(controller::nextFile));

		latestButton = new Button(controls, SWT.PUSH);
		latestButton.setImage(TO_LATEST);
		latestButton.addSelectionListener(getSelectionHandler(controller::latestFile));

		autoUpdate = new Button(controls, SWT.TOGGLE);
		autoUpdate.addSelectionListener(getSelectionHandler(controller::pauseResume));

		createClearingComposite(controls);

		String[] filters = controller.getFilterKeys();
		if (filters.length > 0) {
			Combo fileFilter = new Combo(controls, SWT.DROP_DOWN | SWT.READ_ONLY);
			fileFilter.setItems(filters);
			fileFilter.addSelectionListener(getSelectionHandler(() -> controller.setFilter(fileFilter.getText())));
			fileFilter.select(0);
		}
	}

	/** Create label and checkbox for setting whether plot should be cleared before adding new data */
	private void createClearingComposite(Composite parent) {
		// Create composite for clearing label and box so that the whole area acts as the button
		Composite clearing = new Composite(parent, SWT.NONE);
		clearing.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		clearing.setLayout(new GridLayout(2, false));

		Label clearLabel = new Label(clearing, SWT.NONE);
		clearLabel.setText("Clear plot");

		MouseListener clearListener = new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				controller.toggleClear();
			}
		};
		clearing.addMouseListener(clearListener);
		clearLabel.addMouseListener(clearListener);

		clearPlot = new Button(clearing, SWT.CHECK);
		clearPlot.setToolTipText("Clear plot before plotting new data");
		clearPlot.addSelectionListener(getSelectionHandler(controller::toggleClear));

	}

	@Override
	public void setAutoUpdate(boolean updating) {
		autoUpdate.setSelection(updating);
		autoUpdate.setImage(updating ? PAUSE : RUN);
		autoUpdate.setToolTipText(updating
				? "Showing new data as it is collected - click to pause"
				: "Not plotting new files - click to resume");
	}

	@Override
	public void setClearing(boolean clearing) {
		clearPlot.setSelection(clearing);
		clearPlot.setToolTipText(clearing
				? "Clearing plot before adding new data"
				: "Adding new data to existing plot");
	}

	/**
	 * Set the current file number
	 * <p>
	 * The total number of files is used to determine which of the navigation buttons should be enabled.
	 * @param current The current (1-indexed) file number (of filtered files) or -1 if the index is unknown.
	 * @param total The total number of eligible files
	 */
	@Override
	public void setFileNumber(int current, int total) {
		String currentIndex = (total == 0 || current <= 0) ? "" : String.valueOf(current);
		String lastIndex = (total == 0 || current <= 0) ? "n/a" : " /" + total;
		boolean first = current == 1;
		boolean last = current >= total || total < 1;
		Display.getDefault().asyncExec(() -> {
			formatFileIndex(currentIndex, lastIndex, currentFileIndexText.isFocusControl());
			// If current is -1, index is unknown so button state should remain unchanged
			if (current >= 0) {
				enableButtons(!first, !last);
			}
		});
	}

	/**
	 * Display the current index (and optionally the total number of files
	 * <p>
	 * If the text widget has focus, just set the current file index. If the widget does not have focus,
	 * show the total number of files in a lighter colour as well.
	 *
	 * @param current The string to set containing the current index
	 * @param total The string to append to the current index if the text widget doesn't have focus
	 * @param focused Whether the widget has focus.
	 */
	private void formatFileIndex(String current, String total, boolean focused) {
		currentFileIndexText.setData(CURRENT_INDEX_KEY, current);
		currentFileIndexText.setData(INDEX_SUFFIX_KEY, total);
		// Can't use currentFileIndexText.isFocusControl() as it is still true when focusLost is called
		if (!focused) {
			currentFileIndexText.setText(current + total);
			currentFileIndexText.setStyleRange(new StyleRange(current.length(), total.length(), LIGHT, null, SWT.ITALIC));
		} else {
			currentFileIndexText.setText(current);
			currentFileIndexText.setSelection(current.length());
		}
	}

	/**
	 * Enable the file navigation buttons
	 * @param previous True if the first and previous file buttons should be enabled
	 * @param next True if the next and latest file buttons should be enabled
	 */
	private void enableButtons(boolean previous, boolean next) {
		previousButton.setEnabled(previous);
		firstButton.setEnabled(previous);
		nextButton.setEnabled(next);
		latestButton.setEnabled(next);
	}

	@Override
	public void setFilePath(String filepath) {
		Display.getDefault().asyncExec(() -> {
			currentFileText.setText(filepath);
			currentFileText.setSelection(filepath.length());
		});
	}

	private static KeyListener getKeyHandler(final Consumer<String> handler) {
		return new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					StyledText text = (StyledText)e.getSource();
					handler.accept(text.getText());
					e.doit = false; // Don't add new lines to text
				}
			}
		};
	}

	private static SelectionListener getSelectionHandler(final Runnable handler) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				e.doit = false; // Let the controller decide if things should be checked/selected or not.
				handler.run();
			}
		};
	}
}
