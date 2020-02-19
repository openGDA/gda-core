/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.rcp.views;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
/**
 * A GUI composite to display values of input and extra names of a scannable.
 * These are displayed as name (Label) and value (text box) pair in a Grid of 6 column.
 * <p>
 * Only input fields are editable to provide scannable control when it is not busy.
 * Change to any one of the input fields will trigger a motion in the scannable
 * on pressing return key immediately, so you can only do one field change at any one time.
 *
 * @since 9.16
 * @author fy65
 */
public class ScannableFieldsComposite extends AbstractPositionerComposite {

	private static final Logger logger = LoggerFactory.getLogger(ScannableFieldsComposite.class);

	// about the scannable
	private String[] inputNames;
	private String[] extraNames;
	private Map<String, String> currentPosition; // name to position mapping

	//GUI element for the scannable
	private Composite fieldsComposite; // container for display scannable fields
	private Map<String, Text> inputTextBoxes; // text box for input names
	private Map<String, Text> extraTextBoxes; //text box for extra names

	/**
	 * @param parent the parent composite on which to draw this one
	 * @param style SWT.HORIZONTAL or SWT.VERTICAL will define a horizontal or vertical (default) layout
	 */
	public ScannableFieldsComposite(Composite parent, int style) {
		// Mask out style attributes that are intended for specific controls
		super(parent, style & ~SWT.READ_ONLY);
	}

	@Override
	public void setScannable(Scannable scannable) {
		Objects.requireNonNull(scannable);
		inputNames = scannable.getInputNames();
		extraNames = scannable.getExtraNames();
		//create input boxes for input names
		inputTextBoxes = Arrays.stream(scannable.getInputNames())
				.collect(Collectors.toMap(String::trim, e -> {

					Label label = new Label(fieldsComposite, SWT.None);
					label.setText(e);
					Text text = new Text(fieldsComposite, SWT.BORDER);
					text.setEditable(true);
					GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
					text.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent key) {
							if (key.character == SWT.CR) { // enter or numpad enter pressed
								// Get the new position from the text boxes
								List<String> collect = Arrays.stream(inputNames)
										.map(e -> {
											return inputTextBoxes.get(e).getText().split(" ")[0];
										}).collect(Collectors.toList());
								move(collect);
							}
						}
					});
					text.addFocusListener(new FocusAdapter() {
						@Override
						public void focusLost(FocusEvent e) {
							// Update to ensure current position is shown when focus is lost
							scheduleUpdateReadbackJob();
						}
					});
					return text;
				}));
		//create extra boxes for extra names
		extraTextBoxes = Arrays.stream(extraNames)
				.collect(Collectors.toMap(String::trim, e -> {
			Label label = new Label(fieldsComposite, SWT.None);
			label.setText(e);
			Text text = new Text(fieldsComposite, SWT.BORDER);
			text.setEditable(false);
			text.setEnabled(false);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
			return text;
		}));
		// the following must be called last line in this method due to super.enable()
		super.setScannable(scannable);
	}

	@Override
	protected void createPositionerControl() {
		//create container to display scannable fields
		fieldsComposite= new Composite(this,  SWT.None);
		GridLayoutFactory.fillDefaults().numColumns(6).spacing(1,1).applyTo(fieldsComposite);
		//cannot create display for scannable fields here as the scannable still not defined yet!
	}

	@Override
	protected void updatePositionerControl(final Object newPosition, final boolean moving) {
		this.currentPosition = convertPosition(newPosition);
		//update field values in UI thread
		Display.getDefault().asyncExec(()-> {
			Arrays.stream(inputNames)
			.filter(e -> !inputTextBoxes.get(e).isDisposed())
			.forEach(e -> {
				inputTextBoxes.get(e).setText(currentPosition.get(e));
				inputTextBoxes.get(e).setEditable(!moving);
			});
			Arrays.stream(extraNames)
			.filter(e -> !extraTextBoxes.get(e).isDisposed())
			.forEach(e -> {
				extraTextBoxes.get(e).setText(currentPosition.get(e));
			});
		});
	}

	private  Map<String, String> convertPosition(Object newPosition) {
		//map input names and extra names to their corresponding current values
		try {
			String[] names = Stream.concat(Arrays.stream(inputNames), Arrays.stream(extraNames)).toArray(String[]::new);
			String[] values = ScannableUtils.getFormattedCurrentPositionArray(newPosition, names .length, getScannable().getOutputFormat());
			Map<String, String> collect = IntStream.range(0,names.length).boxed().collect(Collectors.toMap(i->names[i], i->values[i]));
			return collect;
		} catch (DeviceException e) {
			logger.error("Error: ScannableUtils.getFormattedCurrentPositionArray(...)", e);
		}
		return null;
	}
}
