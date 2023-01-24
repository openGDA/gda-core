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

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.exafs.xes.IXesEnergyScannable;
import gda.exafs.xes.XesUtils;

public class XesDiagramComposite extends XesControlsBuilder {

	private Group xesDataComp;

	private String[] headerLabels = {"", "L", "dx", "dy" };

	private GridDataFactory gdFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

	private Map<Scannable, RowWidgets> allRowWidgets = new HashMap<>();

	private String pathToImage = "icons/XESDiagram.png";

	@Override
	public void createControls(Composite parent) {
		final Composite right = new Composite(parent, SWT.NONE);
		right.setLayout(new GridLayout(1, false));

		// Load the diagram image and set the composite size based on the dimensions
		URL imageURL = this.getClass().getResource("/"+pathToImage);
		Image diagramImage = ImageDescriptor.createFromURL(imageURL).createImage();

		var bounds = diagramImage.getBounds();
		gdFactory.align(SWT.CENTER, SWT.CENTER).hint(bounds.width+50, SWT.DEFAULT).applyTo(right);

		ExpandableComposite xesDiagramComposite = new ExpandableComposite(right, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE);
		xesDiagramComposite.marginWidth = 5;
		xesDiagramComposite.marginHeight = 5;
		xesDiagramComposite.setText("XES Diagram");
		xesDiagramComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		Composite xesComp = new Composite(xesDiagramComposite, SWT.NONE);
		xesComp.setLayout(new GridLayout(1, false));

		Label xesLabel = new Label(xesComp, SWT.NONE);
		xesLabel.setImage(diagramImage);
		xesDiagramComposite.setClient(xesComp);
		xesDiagramComposite.setExpanded(true);
		xesDiagramComposite.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				xesDiagramComposite.layout();
				right.layout();
				final ScrolledComposite sc = (ScrolledComposite) xesDiagramComposite.getParent();
				sc.setMinSize(xesDiagramComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});

		xesDataComp = new Group(xesComp, SWT.NONE);
		xesDataComp.setText("Properties");
		xesDataComp.setLayout(new GridLayout(4, false));
		gdFactory.applyTo(xesDataComp);

		createLabels(xesDataComp);

		parent.addDisposeListener(l -> {
			diagramImage.dispose();
			xesDiagramComposite.dispose();
			xesLabel.dispose();
			xesDataComp.dispose();
		});
	}

	public void setRowScannables(List<IXesEnergyScannable> xesEnergyScannables) {
		xesEnergyScannables.forEach(this::addRowValues);
	}

	public void addRowValues(IXesEnergyScannable scn) {
		Label rowLabel = new Label(xesDataComp, SWT.CENTER);
		rowLabel.setText(scn.getName());

		RowWidgets widgets = new RowWidgets(xesDataComp, scn);
		allRowWidgets.put(scn, widgets);
	}

	private void createLabels(Composite parent) {
		for(String label : headerLabels) {
			Label l = new Label(parent, SWT.CENTER);
			l.setText(label);
			gdFactory.applyTo(l);
		}
	}

	/**
	 * Update the L, dx, and dy values in the diagram table for given energy,
	 * using crystal parameters from the spectrometer scannable.
	 *
	 * @param scn IXesEnergyScannable
	 * @param energy [eV]
	 * @throws DeviceException
	 */
	public void updateValues(IXesEnergyScannable scn, double energy) throws DeviceException {
		if (allRowWidgets.containsKey(scn)) {
			allRowWidgets.get(scn).update(energy);
			xesDataComp.getParent().layout();
		}
	}

	/**
	 * Widgets for a row in the table, showing L, dx, dy for a given energy
	 * for an XesEnergyScannable object.
	 */
	private class RowWidgets {
		private final LabelWrapper L;
		private final LabelWrapper dx;
		private final LabelWrapper dy;
		private final IXesEnergyScannable xesEnergyScannable;

		public RowWidgets(Composite parent, IXesEnergyScannable scn) {
			L = new LabelWrapper(parent, SWT.CENTER);
			dx = new LabelWrapper(parent, SWT.CENTER);
			dy = new LabelWrapper(parent, SWT.CENTER);
			xesEnergyScannable = scn;
			Stream.of(L, dx, dy).forEach(w -> {
				w.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
				w.setUnit("mm");
				gdFactory.applyTo(w);
			});
		}

		/**
		 * Update L, dx, and dy for given energy value
		 * @param energy
		 * @throws DeviceException
		 */
		public void update(double energy) throws DeviceException {
			double theta = calculateBraggFromEnergy(energy, xesEnergyScannable);
			double radius = xesEnergyScannable.getRadius();
			L.setValue(XesUtils.getL(radius, theta));
			dx.setValue(XesUtils.getDx(radius, theta));
			dy.setValue(XesUtils.getDy(radius, theta));
		}

		private double calculateBraggFromEnergy(double energy, IXesEnergyScannable scn) throws DeviceException {
			return XesUtils.getBragg(energy, scn.getMaterialType(), scn.getCrystalCut());
		}
	}

}
