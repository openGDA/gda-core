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

package uk.ac.gda.tomography.scan.editor.view;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.service.message.TomographyMessages;
import uk.ac.gda.tomography.service.message.TomographyMessagesUtility;
import uk.ac.gda.tomography.ui.tool.TomographySWTElements;

/**
 * Helps to build an ExpandBar. Once instantiated, {@link #getInternalArea()} provides the {@link Composite} where build the content. Calling
 * {@link #buildExpBar()} assemble all together.
 *
 * @author Maurizio Nagni
 */
public class ExpandBarBuilder {

	private final ExpandBar bar;
	private Composite internalArea;
	private final TomographyMessages title;
	private boolean built = false;

	private static final Logger logger = LoggerFactory.getLogger(ExpandBarBuilder.class);

	public ExpandBarBuilder(Composite composite, TomographyMessages title) {
		this.bar = new ExpandBar(composite, SWT.NONE);
		this.title = title;
		GridLayoutFactory.swtDefaults().applyTo(this.bar);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(this.bar);
		this.internalArea = TomographySWTElements.createComposite(this.bar, SWT.NONE, 1);
		GridLayoutFactory.swtDefaults().applyTo(this.internalArea);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(this.internalArea);
	}

	public Composite getInternalArea() {
		return internalArea;
	}

	public ExpandBar buildExpBar() {
		if (!built) {
			addExpandableArea(title);
		}
		built = true;
		return getBar();
	}

	private ExpandBar getBar() {
		return bar;
	}

	private void addExpandableArea(TomographyMessages title) {
		ExpandItem item0 = new ExpandItem(this.bar, SWT.NONE, 0);
		item0.setText(TomographyMessagesUtility.getMessage(title));
		item0.setHeight(getInternalArea().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(getInternalArea());
		item0.setExpanded(true);
	}
}
