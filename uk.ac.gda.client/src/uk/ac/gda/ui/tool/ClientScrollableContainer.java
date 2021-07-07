/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createScrolledComposite;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.selectable.Lockable;
import uk.ac.gda.ui.tool.selectable.SelectableContainedCompositeFactory;

/**
 * A {@link CompositeFactory} to be used as wrapper around components that have to be scrollable
 *
 * @author Maurizio Nagni
 *
 * @see SelectableContainedCompositeFactory
 */
public class ClientScrollableContainer implements CompositeFactory {

	private final Lockable lockable;
	private ScrolledComposite scrolledInnerContainer;
	private Composite innerContainer;

	public ClientScrollableContainer(Lockable lockable) {
		this.lockable = lockable;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		// The scrollable container.
		scrolledInnerContainer = createScrolledComposite(parent);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(scrolledInnerContainer);

		// The scrollable inner area.
		innerContainer = createClientCompositeWithGridLayout(scrolledInnerContainer, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(innerContainer);
		scrolledInnerContainer.setContent(innerContainer);
		// Exposes the LockableSelectable interface so to allow the selectable factories to lock this widget
		Optional.ofNullable(lockable)
			.ifPresent(l -> innerContainer.setData(Lockable.LOCKABLE_SELECTABLE, lockable) );

		return parent;
	}

	public void cleanInnerContainer() {
		// Deletes, if any the existing composites inside the internal scrollable area
		Arrays.stream(getInnerContainer().getChildren())
			.forEach(Control::dispose);
	}

	public void populateInnerContainer(CompositeFactory cf) {
		var content = cf.createComposite(getInnerContainer(), SWT.NONE);
		content.addPaintListener(e -> resizeScrolledComposite(content));
		resizeScrolledComposite(content);
	}

	private void resizeScrolledComposite(Composite newComposite) {
		// layout the children
		newComposite.layout();
		// Even after the layout, the newCompsite.getSize() is {0,0}.
		// Calling newComposite.computeSize calculate the default size, that is the widget size
		// In this way is possible to correctly set the size below which the scroll bars appear.
		getScrolledInnerContainer().setMinSize(newComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT ));
		getScrolledInnerContainer().getShell().layout(true, true);
	}

	private ScrolledComposite getScrolledInnerContainer() {
		return scrolledInnerContainer;
	}

	private Composite getInnerContainer() {
		return innerContainer;
	}
}
