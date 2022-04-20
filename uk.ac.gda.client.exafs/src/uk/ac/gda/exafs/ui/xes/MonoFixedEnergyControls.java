/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.xes;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import gda.util.exafs.Element;

public class MonoFixedEnergyControls extends XesControlsBuilder {

	private Composite mainComposite;
	private ScaleBox monoEnergy;
	private ComboWrapper element;
	private ComboWrapper edge;

	@Override
	public void createControls(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		mainComposite.setLayoutData(gridData);

		mainComposite.setLayout(new GridLayout(6, false));

		Link lblMonoEnergy = new Link(mainComposite, SWT.NONE);
		lblMonoEnergy.setText("<a>Mono Energy</a>");
		lblMonoEnergy.setToolTipText("Click to toggle element and edge controls for looking up edge energy.");

		monoEnergy = new ScaleBox(mainComposite, SWT.NONE);

		GridDataFactory gdFactory = GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER);

		monoEnergy.setMinimum(MonoScanRangeControls.getMinMonoEnergy());
		monoEnergy.setMaximum(MonoScanRangeControls.getMaxMonoEnergy());
		monoEnergy.setUnit("eV");
		gdFactory.hint(150, SWT.DEFAULT).applyTo(monoEnergy);

		Label lblElement = new Label(mainComposite, SWT.NONE);
		lblElement.setText("  Element");

		element = new ComboWrapper(mainComposite, SWT.DROP_DOWN);
		element.setItems(Element.getSortedEdgeSymbols("Sc", "U"));
		element.setValue("Fe");
		gdFactory.hint(80, SWT.DEFAULT).applyTo(element);

		gridData.widthHint = 69;

		Label lblEdge = new Label(mainComposite, SWT.NONE);
		lblEdge.setText("Edge");

		edge = new ComboWrapper(mainComposite, SWT.READ_ONLY);
		edge.setItems(Element.getElement("Fe").getAllowedEdges().toArray(new String[1]));
		edge.select(0);
		gdFactory.hint(70, SWT.DEFAULT).applyTo(edge);

		element.addValueListener(e -> updateElement());
		element.addValueListener(e -> updateElement());
		edge.addValueListener(e -> updateEdge((String) e.getValue()));

		lblElement.setVisible(false);
		element.setVisible(false);
		lblEdge.setVisible(false);
		edge.setVisible(false);

		lblMonoEnergy.addListener(SWT.Selection, e -> {
			lblElement.setVisible(!lblElement.isVisible());
			element.setVisible(!element.isVisible());
			lblEdge.setVisible(!lblEdge.isVisible());
			edge.setVisible(!edge.isVisible());
		});

		parent.addDisposeListener(l -> {
			lblMonoEnergy.dispose();
			dispose();
		});
	}

	public void dispose() {
		getWidgets().forEach(Composite::dispose);
		deleteIObservers();
	}

	private List<Composite> getWidgets() {
		return List.of(mainComposite, monoEnergy, edge, element);
	}

	private void updateElement() {
		Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		Color black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

		Element ele = getSelectedElement();
		if (ele == null) {
			element.setForeground(red);
			return;
		}

		element.setForeground(black);
		String currentEdge = (String) edge.getValue();
		List<String> edges = ele.getAllowedEdges();
		edge.setItems(edges.toArray(new String[edges.size()]));
		if (currentEdge == null || !edges.contains(currentEdge)) {
			currentEdge = edges.get(0);
		}
		edge.select(edges.indexOf(currentEdge));
		double edgeEn = ele.getEdgeEnergy(currentEdge);
		getMonoEnergy().setValue(edgeEn); // Its in eV in Element.
	}

	private Element getSelectedElement() {
		final String symbol = (String) element.getValue();
		return Element.getElement(symbol);
	}

	private void updateEdge(final String edge) {
		Element ele = getSelectedElement();
		if (ele == null)
			return;
		final double edgeEn = ele.getEdgeEnergy(edge);
		getMonoEnergy().setValue(edgeEn); // Its in eV in Element.
	}

	public Composite getMainComposite() {
		return mainComposite;
	}

	public ScaleBox getMonoEnergy() {
		return monoEnergy;
	}

	public ComboWrapper getElement() {
		return element;
	}

	public ComboWrapper getEdge() {
		return edge;
	}
}
