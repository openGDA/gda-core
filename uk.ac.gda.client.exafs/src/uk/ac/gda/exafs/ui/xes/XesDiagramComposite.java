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

import java.util.stream.Stream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import com.swtdesigner.SWTResourceManager;

import gda.exafs.xes.XesUtils;

public class XesDiagramComposite extends XesControlsBuilder {

	private Group xesDataComp;
	private LabelWrapper L;
	private LabelWrapper dx;
	private LabelWrapper dy;

	@Override
	public void createControls(Composite parent) {
		final Composite right = new Composite(parent, SWT.NONE);

		GridDataFactory gdFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(150,  SWT.DEFAULT);

		gdFactory.hint(200, SWT.DEFAULT).applyTo(right);
		right.setLayout(new GridLayout(1, false));

		ExpandableComposite xesDiagramComposite = new ExpandableComposite(right, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE);
		xesDiagramComposite.marginWidth = 5;
		xesDiagramComposite.marginHeight = 5;
		xesDiagramComposite.setText("XES Diagram");
		xesDiagramComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		Composite xesComp = new Composite(xesDiagramComposite, SWT.NONE);
		xesComp.setLayout(new GridLayout(1, false));

		Label xesLabel = new Label(xesComp, SWT.NONE);
		xesLabel.setImage(SWTResourceManager.getImage(getClass(), "/icons/XESDiagram.png"));
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
		xesDataComp.setLayout(new GridLayout(2, false));
		gdFactory.applyTo(xesDataComp);

		Label lblL = new Label(xesDataComp, SWT.NONE);
		lblL.setText("L        ");

		L = new LabelWrapper(xesDataComp, SWT.NONE);
		L.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
		L.setUnit("mm");
		L.setText("790");
		gdFactory.applyTo(L);

		Label lblDx = new Label(xesDataComp, SWT.NONE);
		lblDx.setText("dx");

		dx = new LabelWrapper(xesDataComp, SWT.NONE);
		dx.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
		dx.setUnit("mm");
		dx.setText("30");
		gdFactory.applyTo(dx);

		Label lblDy = new Label(xesDataComp, SWT.NONE);
		lblDy.setText("dy");

		dy = new LabelWrapper(xesDataComp, SWT.NONE);
		dy.setUnit("mm");
		dy.setTextType(TEXT_TYPE.NUMBER_WITH_UNIT);
		dy.setText("600");
		gdFactory.applyTo(dy);

		parent.addDisposeListener(l -> dispose());
	}

	public void dispose() {
		Stream.of(xesDataComp, L, dx, dy).forEach(Composite::dispose);
	}

	public void updateValues(double radius, double theta) {
		L.setValue(XesUtils.getL(radius, theta));
		dx.setValue(XesUtils.getDx(radius, theta));
		dy.setValue(XesUtils.getDy(radius, theta));
		xesDataComp.getParent().layout();
	}

}
