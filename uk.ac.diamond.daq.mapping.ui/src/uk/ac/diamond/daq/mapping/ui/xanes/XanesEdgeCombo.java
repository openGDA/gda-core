/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.xanes;

import static com.github.tschoonj.xraylib.Xraylib.K_SHELL;
import static com.github.tschoonj.xraylib.Xraylib.L3_SHELL;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_ELEMENT_AND_EDGE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_ELEMENT_AND_EDGE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;
import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;

public class XanesEdgeCombo implements ISelectionProvider {
	private static final Logger logger = LoggerFactory.getLogger(XanesEdgeCombo.class);

	/**
	 * Maps shell as string (as set in {@link ElementAndEdges} to the corresponding {@link Xraylib} constant
	 */
	private static final Map<String, Integer> edgeMap = Map.of("K", K_SHELL, "L", L3_SHELL);

	private Label label;
	private ComboViewer elementsAndEdgeCombo;

	public XanesEdgeCombo(Composite parent) {
		var content = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(content);

		label = new Label(content, SWT.WRAP);
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		label.setText(getMessage(XANES_ELEMENT_AND_EDGE));

		elementsAndEdgeCombo = new ComboViewer(content);
		elementsAndEdgeCombo.setContentProvider(ArrayContentProvider.getInstance());
		elementsAndEdgeCombo.setInput(createEdgeToEnergyList());
		elementsAndEdgeCombo.getCombo().setToolTipText(getMessage(XANES_ELEMENT_AND_EDGE_TOOLTIP));
		elementsAndEdgeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EdgeToEnergy) element).getEdge();
			}
		});
	}

	/**
	 * Create a list of {@link EdgeToEnergy} objects from {@link #elementsAndEdges} set when the bean is created.<br>
	 * Used as input to the combo viewer
	 * <p>
	 * Radioactive elements are indicated with asterisks around the entry.
	 *
	 * @return list of EdgeToEnergy to set in the combo viewer
	 */
	private List<EdgeToEnergy> createEdgeToEnergyList() {
		final Map<String, ElementAndEdgesList> elementsAndEdgesMap = Finder.getLocalFindablesOfType(ElementAndEdgesList.class);
		if (elementsAndEdgesMap == null || elementsAndEdgesMap.isEmpty()) {
			logger.error("No element/edge combinations have been set");
			return Collections.emptyList();
		}

		final List<ElementAndEdges> elementsAndEdges = elementsAndEdgesMap.values().iterator().next().getElementsAndEdges();
		final List<EdgeToEnergy> result = new ArrayList<>(elementsAndEdges.size());

		// Iterate over elements
		for (ElementAndEdges elementEntry : elementsAndEdges) {
			final String element = elementEntry.getElementName();
			final int atomicNumber = Xraylib.SymbolToAtomicNumber(element);

			// Iterate over the edges of this element
			for (String edge : elementEntry.getEdges()) {
				final Integer edgeNumber = edgeMap.get(edge);
				if (edgeNumber == null) {
					logger.error("Unknown edge {}", edge);
					continue;
				}
				var entryFormat = elementEntry.isRadioactive() ? "*%s-%s*" : "%s-%s";
				var comboEntry = String.format(entryFormat, element, edge);
				result.add(new EdgeToEnergy(comboEntry, Xraylib.EdgeEnergy(atomicNumber, edgeNumber)));
			}
		}
		return result;
	}

	public IViewerObservableValue<EdgeToEnergy> getObservableValue() {
		return ViewerProperties.singleSelection(EdgeToEnergy.class).observe(elementsAndEdgeCombo);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		elementsAndEdgeCombo.addSelectionChangedListener(listener);
	}

	@Override
	public IStructuredSelection getSelection() {
		return elementsAndEdgeCombo.getStructuredSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		elementsAndEdgeCombo.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		elementsAndEdgeCombo.setSelection(selection, true);
	}

	public double getSelectedEnergy() {
		final EdgeToEnergy selection = (EdgeToEnergy) elementsAndEdgeCombo.getStructuredSelection().getFirstElement();
		return selection == null ? 0.0 : selection.getEnergy();
	}

	/**
	 * Enable/disable the wrapped controls
	 */
	public void setEnabled(boolean enabled) {
		elementsAndEdgeCombo.getCombo().setEnabled(enabled);
		label.setEnabled(enabled);
	}
}
