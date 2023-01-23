/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.selector.BeanSelectionEvent;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.richbeans.widgets.selector.GridListEditor.GRID_ORDER;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper.BOOLEAN_MODE;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.vortex.DetectorElement;

public class FluoDetectorElementsComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(FluoDetectorElementsComposite.class);

	private Group elementsGroup;
	private Label elementTablePlaceholder;
	private LabelWrapper elementName;
	private BooleanWrapper excluded;
	private GRID_ORDER gridOrder =  GRID_ORDER.LEFT_TO_RIGHT_TOP_TO_BOTTOM;
	private FluoDetectorElementConfig elementConfiguration = null;
	private double maxDetectorElementCounts = 250000;
	private List<Double> elementCounts = Collections.emptyList();
	private Map<Integer, Label> elementLabelMap = new HashMap<>();
	private volatile int selectedElementIndex = 0;
	private BeanSelectionListener listener;
	private List<DetectorElement> detectorElements;

	public FluoDetectorElementsComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		elementsGroup = new Group(this, SWT.NONE);
		elementsGroup.setText("Detector Elements");
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(elementsGroup);

		elementTablePlaceholder = new Label(elementsGroup, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(elementTablePlaceholder);
		elementTablePlaceholder.setText("Not connected to detector, no elements to display");

		elementName = new LabelWrapper(elementsGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(elementName);
		elementName.setTextType(TEXT_TYPE.PLAIN_TEXT);

		excluded = new BooleanWrapper(elementsGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(excluded);
		excluded.setBooleanMode(BOOLEAN_MODE.REVERSE);
		excluded.setText("Enabled");

		excluded.addValueListener(l -> notifyEnabledStatus());
		excluded.on();
	}

	/**
	 * Call this only once, after the number of elements is known and the regions composite has been created
	 */
	public void configureDetectorElementTable(List<DetectorElement> detectorElements) {
		this.detectorElements = detectorElements;
		createDetectorElementTable(detectorElements.size());
	}

	private void createDetectorElementTable(int numberOfElements) {

		elementTablePlaceholder.setVisible(false);
		elementTablePlaceholder.dispose();

		// Set number of columns to get square table of Detector Elements
		int columns = numberOfElements / 2;
		double elementListSizeSquareRoot = Math.sqrt(numberOfElements);
		if (Double.compare(elementListSizeSquareRoot, (int) elementListSizeSquareRoot) == 0) {
			columns = (int) elementListSizeSquareRoot;
		} else if ((numberOfElements % 2) != 0) {
			logger.info("Non-even, non-square number of detector elements - the last square in the detector element table will be empty");
			// increment by 1 - there will be one empty element in the table
			columns++;
		}

		int styleBits = SWT.NONE;

		// Set style bits for right-to-left fill orientation
		GRID_ORDER order = getGridOrderFromConfig(numberOfElements);
		if (order == GRID_ORDER.TOP_TO_BOTTOM_RIGHT_TO_LEFT) {
			styleBits |= SWT.RIGHT_TO_LEFT;
		}
		Group grp = new Group(elementsGroup, styleBits);
		GridLayoutFactory.swtDefaults().numColumns(columns).equalWidth(true).spacing(2,2).applyTo(grp);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(grp);

		elementLabelMap = new HashMap<>();

		for(int i=0; i<numberOfElements; i++) {
			Label labelWidget = new Label(grp, SWT.CENTER);
			labelWidget.setText(Integer.toString(i));
			labelWidget.addMouseListener(createMouseListener(i));
			GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(25, 25).applyTo(labelWidget);
			String labelText = String.valueOf(i);
			if (order == GRID_ORDER.CUSTOM_MAP) {
				labelText = String.valueOf(elementConfiguration.getElementMap().get(i));
			}
			logger.debug("Element {} : elementForLabel = {}", i, labelText);
			labelWidget.setText(labelText);
			elementLabelMap.put(i, labelWidget);
		}

		// Make sure first element is selected
		selectDetectorElementLabel(0);

		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(grp);
		grp.moveAbove(elementName);
		this.getParent().layout(true, true);
	}

	/**
	 * Listener that responds to mouse click on detector element labels
	 * and updates the label colours.
	 * @param index
	 * @return
	 */
	private MouseListener createMouseListener(int index) {
		return new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				logger.trace("MouseDown - detector element {}", index);
				selectDetectorElementLabel(index);
			}
		};
	}

	private void notifyEnabledStatus() {
		if (listener != null) {
			listener.selectionChanged(new BeanSelectionEvent(this, selectedElementIndex, getExcluded().getValue()));
		}
	}

	private void selectDetectorElementLabel(int index) {
		boolean selectionChanged = index != selectedElementIndex;
		selectedElementIndex = index;
		setElementLabelColours();
		// Fire event to update the MCA plot view
		if (listener != null && selectionChanged) {
			listener.selectionChanged(new BeanSelectionEvent(this, index, detectorElements.get(index)));
		}
	}

	/**
	 * Set the colours of the detector element label widgets
	 * using colours provided by {@link #getForegroundColour(int)} and {@link #getBackgroundColour(int)}.
	 */
	private void setElementLabelColours() {
		for(var mapEntry : elementLabelMap.entrySet()) {
			Label label = mapEntry.getValue();
			int index = mapEntry.getKey();
			label.setForeground(getForegroundColour(index).color);
			label.setBackground(getBackgroundColour(index).color);
		}
	}

	/**
	 * Enum of colours used for rendering detector element labels
	 */
	private enum ColourEnum {
		BLUE(SWT.COLOR_BLUE),
		RED(SWT.COLOR_RED),
		WHITE(SWT.COLOR_WHITE),
		BLACK(SWT.COLOR_BLACK);

		protected final Color color;
		ColourEnum(int col) {
			color = Display.getDefault().getSystemColor(col);
		}
	}

	/**
	 * Generate background colour for detector element label. :
	 * Blue if element is selected, white otherwise
	 * @param index of the detector element
	 * @return
	 */
	private ColourEnum getBackgroundColour(int index) {
		if (index == selectedElementIndex) {
			return ColourEnum.BLUE;
		}
		return ColourEnum.WHITE;
	}

	/**
	 * Generate foreground (text) colour for detector element label :
	 * Red if count value is too high; else white if element is selected, black otherwise
	 * @param index of the detector element
	 * @return
	 */
	private ColourEnum getForegroundColour(int index) {
		if (!elementCountsWithinRange(index)) {
			return ColourEnum.RED;
		}
		if (index == selectedElementIndex) {
			return ColourEnum.WHITE;
		}
		return ColourEnum.BLACK;
	}

	/**
	 *
	 * @param index
	 * @return Return true is specified detector element has counts that exceed max allowed value
	 */
	private boolean elementCountsWithinRange(int index) {
		if (index >= 0 && index < elementCounts.size()) {
			return elementCounts.get(index) < maxDetectorElementCounts;
		}
		return true;
	}

	private GRID_ORDER getGridOrderFromConfig(int numElements) {
		if (elementConfiguration == null) {
			return gridOrder;
		}
		if (elementConfiguration.getElementMap() != null && elementConfiguration.getElementMap().size() == numElements) {
			return GRID_ORDER.CUSTOM_MAP;
		} else {
			int gridFromConfig = elementConfiguration.getGridOrder();
			if (gridFromConfig<0 || gridFromConfig>1) {
				gridFromConfig=0;
			}
			return GRID_ORDER.values()[gridFromConfig];
		}
	}

	public int getSelectedElementIndex() {
		return selectedElementIndex;
	}

	public IFieldWidget getElementNameLabel() {
		return elementName;
	}

	public IFieldWidget getExcluded() {
		return excluded;
	}

	public void setDetectorElementOrder(GRID_ORDER order) {
		this.gridOrder = order;
	}

	public FluoDetectorElementConfig getElementConfiguration() {
		return elementConfiguration;
	}

	public void setElementConfiguration(FluoDetectorElementConfig elementConfiguration) {
		this.elementConfiguration = elementConfiguration;
	}

	/**
	 * Set total counts for each detector element and refresh the
	 * element labels.
	 * @param counts
	 */
	public void setElementCounts(List<Double> counts) {
		elementCounts = counts;
		selectDetectorElementLabel(selectedElementIndex);
	}


	/**
	 * Maximum number of counts for a detector element. Values exceeding this
	 * will have the element number show in red.
	 * @param maxDetectorCounts
	 */
	public void setMaxDetectorElementCounts(double maxDetectorCounts) {
		maxDetectorElementCounts = maxDetectorCounts;
	}


	public void addBeanSelectionListener(BeanSelectionListener listener) {
		this.listener = listener;
	}
}
