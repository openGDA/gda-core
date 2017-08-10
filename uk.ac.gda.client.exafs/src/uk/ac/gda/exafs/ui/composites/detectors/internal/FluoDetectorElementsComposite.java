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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.selector.GridListEditor;
import org.eclipse.richbeans.widgets.selector.GridListEditor.GRID_ORDER;
import org.eclipse.richbeans.widgets.selector.ListEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper.BOOLEAN_MODE;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.beans.vortex.DetectorElement;

public class FluoDetectorElementsComposite extends Composite {

	private static Logger logger = LoggerFactory.getLogger(FluoDetectorElementsComposite.class);

	private Group elementsGroup;
	private Label elementTablePlaceholder;
	private GridListEditor detectorElementTable;
	private LabelWrapper elementName;
	private BooleanWrapper excluded;
	private GRID_ORDER gridOrder =  GRID_ORDER.LEFT_TO_RIGHT_TOP_TO_BOTTOM;

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
	}

	/*
	 * Call this only once, after the number of elements is known and the regions composite has been created
	 */
	public void configureDetectorElementTable(int numberOfElements, Composite regionsComposite) {

		createDetectorElementTable(numberOfElements);

		detectorElementTable.setEditorClass(DetectorElement.class);
		detectorElementTable.setEditorUI(regionsComposite);
		detectorElementTable.setEnabled(true);
		detectorElementTable.setAdditionalLabelProvider(new ColumnLabelProvider() {
			private final Color lightGray = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);

			@Override
			public Color getForeground(Object element) {
				if (element instanceof IDetectorElement) {
					IDetectorElement detectorElement = (IDetectorElement) element;
					if (detectorElement.isExcluded()) {
						return lightGray;
					}
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});
	}

	private void createDetectorElementTable(int numberOfElements) {
		if (detectorElementTable != null) {
			throw new IllegalStateException("Detector element table already exists - cannot create it more than once");
		}

		elementTablePlaceholder.setVisible(false);
		elementTablePlaceholder.dispose();

		// Squared table of Detector Elements
		int columns = numberOfElements / 2;
		int rows = 2;

		double elementListSizeSquareRoot = Math.sqrt(numberOfElements);

		if (Double.compare(elementListSizeSquareRoot, (int) elementListSizeSquareRoot) == 0) {
			columns = (int) elementListSizeSquareRoot;
			rows = (int) elementListSizeSquareRoot;
		} else if ((numberOfElements % 2) != 0) {
			logger.warn("Non-even, non-square number of detector elements: not sure how to layout the grid!");
		}

		detectorElementTable = new GridListEditor(elementsGroup, SWT.NONE, columns, rows);
		if (gridOrder==GRID_ORDER.CUSTOM_MAP && numberOfElements==9) {
			setupDetectorMap9Element();
		} else {
			detectorElementTable.setGridOrder(gridOrder);
		}
		detectorElementTable.setGridWidth(Math.max(160, columns * 25));
		detectorElementTable.setGridHeight(rows * 23);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(detectorElementTable);
		detectorElementTable.moveAbove(elementName);
		this.getParent().layout(true, true);
	}

	/**
	 *  Setup grid map for 9 element Ge detector which has slightly unusual arrangement of elements.
	 */
	void setupDetectorMap9Element() {
		detectorElementTable.setGridOrder(GRID_ORDER.CUSTOM_MAP);
		Map<Integer, Integer> gridMap = new HashMap<Integer, Integer>();
		int[] elementIndices = new int[] { 5, 4, 3,
										   6, 8, 2,
										   7, 0, 1 };
		for (int i = 0; i < elementIndices.length; i++) {
			gridMap.put(i, elementIndices[i]);
		}
		detectorElementTable.setGridMap(gridMap);
	}

	public ListEditor getDetectorList() {
		return detectorElementTable;
	}

	public int getSelectedElementIndex() {
		return detectorElementTable.getSelectedIndex();
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
}
