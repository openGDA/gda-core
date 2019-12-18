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

package uk.ac.diamond.daq.mapping.xanes.ui;

import static com.github.tschoonj.xraylib.Xraylib.K_SHELL;
import static com.github.tschoonj.xraylib.Xraylib.L3_SHELL;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_ELEMENT_AND_EDGE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_ELEMENT_AND_EDGE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;
import com.google.common.collect.ImmutableMap;
import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

public class XanesEdgeCombo implements IObservable {
	private static final Logger logger = LoggerFactory.getLogger(XanesEdgeCombo.class);

	private final ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Maps shell as string (as set in {@link ElementAndEdges} to the corresponding {@link Xraylib} constant
	 */
	private static final Map<String, Integer> edgeMap = ImmutableMap.of("K", K_SHELL, "L", L3_SHELL);


	public XanesEdgeCombo(Composite parent) {
		final Composite content = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(content);

		final Label label = new Label(content, SWT.WRAP);
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		label.setText(getMessage(XANES_ELEMENT_AND_EDGE));

		final ComboViewer elementsAndEdgeCombo = new ComboViewer(content);
		elementsAndEdgeCombo.setContentProvider(ArrayContentProvider.getInstance());
		elementsAndEdgeCombo.setInput(createEdgeToEnergyList());
		elementsAndEdgeCombo.getCombo().setToolTipText(getMessage(XANES_ELEMENT_AND_EDGE_TOOLTIP));
		elementsAndEdgeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EdgeToEnergy) element).getEdge();
			}
		});
		elementsAndEdgeCombo.addSelectionChangedListener(e -> handleEdgeSelectionChanged(
				(EdgeToEnergy) elementsAndEdgeCombo.getStructuredSelection().getFirstElement()));
	}

	private void handleEdgeSelectionChanged(EdgeToEnergy selection) {
		logger.debug("Element/edge selection changed to {}", selection);
		observableComponent.notifyIObservers(this, selection.getEnergy());
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
		final Map<String, ElementAndEdgesList> elementsAndEdgesMap = Finder.getInstance().getLocalFindablesOfType(ElementAndEdgesList.class);
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
				final String entryFormat = elementEntry.isRadioactive() ? "*%s-%s*" : "%s-%s";
				final String comboEntry = String.format(entryFormat, element, edge);
				result.add(new EdgeToEnergy(comboEntry, Xraylib.EdgeEnergy(atomicNumber, edgeNumber)));
			}
		}
		return result;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Maps element/edge in user-readable format (e.g. "Fe-K") to the corresponding edge energy<br>
	 * Used as input for the combo box for the user to choose the edge to scan
	 */
	private static class EdgeToEnergy {
		private final String edge;
		private final double energy;

		public EdgeToEnergy(String edge, double energy) {
			this.edge = edge;
			this.energy = energy;
		}

		public String getEdge() {
			return edge;
		}

		public double getEnergy() {
			return energy;
		}

		@Override
		public String toString() {
			return "EdgeToEnergy [edge=" + edge + ", energy=" + energy + "]";
		}
	}
}
