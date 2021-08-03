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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

/**
 * A GUI composite to display values of input and extra names of a scannable. These are displayed as name (Label), value
 * (text box), and unit (Label) if unit is available in a {@link RowLayout}.
 * <p>
 * Only input fields are editable to provide scannable control when it is not busy. Change to any one of the input
 * fields will trigger a motion in the scannable on pressing return key immediately, so you can only do one field change
 * at any one time.
 *
 * @since 9.16
 * @author fy65
 */
public class ScannableFieldsComposite extends AbstractPositionerComposite {

	private static final Logger logger = LoggerFactory.getLogger(ScannableFieldsComposite.class);

	private String[] inputNames;
	private String[] extraNames;

	private Composite fieldsComposite; // container for display scannable fields
	private Map<String, Text> inputTextBoxes; // text box for input names
	private Map<String, Text> extraTextBoxes; // text box for extra names

	private Map<String, String> userUnits = new HashMap<>();

	/**
	 * @param parent
	 *            the parent composite on which to draw this one
	 * @param style
	 *            SWT.HORIZONTAL or SWT.VERTICAL will define a horizontal or vertical (default) layout
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
		// create input boxes for input names
		inputTextBoxes = Arrays.stream(inputNames).collect(Collectors.toMap(String::trim, e -> {

			final var label = new CLabel(fieldsComposite, SWT.CENTER);
			label.setLayoutData(new RowData());
			label.setText(e);
			final var text = new Text(fieldsComposite, SWT.BORDER);
			text.setEditable(true);
			text.addKeyListener(KeyListener.keyPressedAdapter(ev -> {
				if (ev.character == SWT.CR) {
					List<String> collect = Arrays.stream(inputNames)
							.map(n -> inputTextBoxes.get(n).getText().split(" ")[0]).collect(Collectors.toList());
					move(collect);
				}
			}));
			text.addFocusListener(FocusListener.focusLostAdapter(ev -> scheduleUpdateReadbackJob()));
			final var unit = new CLabel(fieldsComposite, SWT.CENTER);
			unit.setLayoutData(new RowData());
			if (!userUnits.isEmpty()) {
				unit.setText(StringUtils.defaultString(userUnits.get(e)));
			}
			return text;
		}));
		// create extra boxes for extra names
		extraTextBoxes = Arrays.stream(extraNames).collect(Collectors.toMap(String::trim, e -> {
			final var label = new CLabel(fieldsComposite, SWT.None);
			label.setText(e);
			final var text = new Text(fieldsComposite, SWT.BORDER);
			text.setEditable(false);
			text.setEnabled(false);
			final var unit = new CLabel(fieldsComposite, SWT.None);
			if (!userUnits.isEmpty()) {
				unit.setText(StringUtils.defaultString(userUnits.get(e)));
			}
			return text;
		}));

		// the following must be called last line in this method due to super.enable()
		super.setScannable(scannable);
	}

	@Override
	protected void createPositionerControl() {
		// create container to display scannable fields
		fieldsComposite = new Composite(this, SWT.None);
		// Setup layout
		final var rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.fill = true;
		rowLayout.center = true;
		rowLayout.marginTop = 1;
		rowLayout.marginBottom = 1;
		rowLayout.spacing = 1;
		fieldsComposite.setLayout(rowLayout);
		// cannot create display for scannable fields here as the scannable still not defined yet!
	}

	@Override
	protected void updatePositionerControl(final Object newPosition, final boolean moving) {
		final Map<String, String> currentPosition = convertPosition(newPosition);
		Display.getDefault().asyncExec(() -> {
			Arrays.stream(inputNames).filter(e -> !inputTextBoxes.get(e).isDisposed()).forEach(e -> {
				inputTextBoxes.get(e).setText(currentPosition.get(e));
				inputTextBoxes.get(e).setEditable(!moving);
			});
			Arrays.stream(extraNames).filter(e -> !extraTextBoxes.get(e).isDisposed()).forEach(e -> {
				extraTextBoxes.get(e).setText(currentPosition.get(e));
			});
		});
	}

	private Map<String, String> convertPosition(Object newPosition) {
		try {
			final String[] names = Stream.concat(Arrays.stream(inputNames), Arrays.stream(extraNames))
					.toArray(String[]::new);
			final String[] values = ScannableUtils.getFormattedCurrentPositionArray(newPosition, names.length,
					getScannable().getOutputFormat());
			return IntStream.range(0, names.length).boxed().collect(Collectors.toMap(i -> names[i], i -> values[i]));
		} catch (DeviceException e) {
			logger.error("Error: ScannableUtils.getFormattedCurrentPositionArray(...)", e);
		}
		return null;
	}

	public void setUserUnits(Map<String, String> userUnits2) {
		this.userUnits = userUnits2;
	}

}
