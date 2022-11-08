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

package uk.ac.gda.ui.tool.selectable;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.ClientScrollableContainer;

/**
 * This widget can dynamically change its content based on the user selection.
 * <p>
 * An instance of this class contains a collection of {@link NamedCompositeFactory}. Each element of the collection is exposed as radio button at the top of the widget.
 * The selection of an element causes the associated {@code NamedComposite} to be build inside a {@link ScrolledComposite}
 * </p>
 * <p>
 * Any of the {@code NamedComposite} may inhibit the transition to another {code NamedComposite} locking this {@code Composite}.
 * To activate the lock the {@code NamedComposite} executes
 * <pre>
 * 	LockableSelectable lockableClass = WidgetUtilities.getDataObject(this.parent, LockableSelectable.class, LockableSelectable.LOCKABLE_SELECTABLE);
 *	Optional.of(lockableClass).ifPresent(l -> l.lock(true));
 * </pre>
 * After which the selectable widget will be disabled.
 * In a similar way the lock can be disabled setting the lock to {@code false}
 * <pre>
 * </p>
 *
 * @author Maurizio Nagni
 */
public class SelectableContainedCompositeFactory implements CompositeFactory, Lockable {

	private final List<NamedCompositeFactory> composites;
	private final ClientMessages groupName;

	private ClientScrollableContainer scrollableComposite;

	private ComboViewer compositeSelector;

	/**
	 * Cached for continuity when recreating the composite
	 * e.g. switching perspectives
	 */
	private Optional<NamedCompositeFactory> selectedComposite = Optional.empty();

	private IPerspectiveDescriptor instancePerspective;

	/**
	 * Creates an factory instance.
	 * @param composites the namedCompsite to select from
	 * @param groupName the label to use for the radio group
	 */
	public SelectableContainedCompositeFactory(List<NamedCompositeFactory> composites, ClientMessages groupName) {
		this.composites = composites;
		this.groupName = groupName;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		instancePerspective = getActiveWindow().getActivePage().getPerspective();

		var mainContainer = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainContainer);
		standardMarginHeight(mainContainer.getLayout());
		standardMarginWidth(mainContainer.getLayout());

		var selectionsComposite = ClientSWTElements.innerComposite(mainContainer, 2, false);

		ClientSWTElements.label(selectionsComposite, ClientMessagesUtility.getMessage(groupName));

		compositeSelector = new ComboViewer(selectionsComposite);
		compositeSelector.setContentProvider(ArrayContentProvider.getInstance());
		compositeSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof NamedCompositeFactory namedComposite) {
					return ClientMessagesUtility.getMessage(namedComposite.getName());
				} else {
					return super.getText(element);
				}
			}
		});

		compositeSelector.getCombo().addListener(SWT.MouseWheel, event -> event.doit = false);

		compositeSelector.setInput(composites);

		ClientSWTElements.STRETCH.applyTo(compositeSelector.getControl());

		compositeSelector.addSelectionChangedListener(this::handleSelectionChanged);


		scrollableComposite = new ClientScrollableContainer(this);
		scrollableComposite.createComposite(mainContainer, SWT.NONE);

		refreshSelection();

		getActiveWindow().addPerspectiveListener(new PerspectiveAdapter() {

			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				if (instancePerspective.equals(perspective)) {
					/* if we're switching to the perspective where this instance lives,
					 * select the previously selected composite, or the first one */
					refreshSelection();
				} else {
					/* this perspective listener does not get removed,
					 * so we must manually clear our selection
					 * when another perspective is activated */
					scrollableComposite.cleanInnerContainer();
				}
			}
		});

		return mainContainer;
	}

	private void handleSelectionChanged(SelectionChangedEvent event) {
		var composite = (NamedCompositeFactory) event.getStructuredSelection().getFirstElement();
		createSelectedComposite(composite);
	}

	private void refreshSelection() {
		var selection = selectedComposite.orElse((NamedCompositeFactory) compositeSelector.getElementAt(0));
		compositeSelector.setSelection(new StructuredSelection(selection));
	}

	private void createSelectedComposite(NamedCompositeFactory selection) {
		scrollableComposite.cleanInnerContainer();
		scrollableComposite.populateInnerContainer(selection);
		selectedComposite = Optional.of(selection);
	}

	/**
	 * Enable, or disable, the selectable widget
	 * @param lock {@code true} to lock, {@code false} to unlock
	 */
	@Override
	public void lock(boolean lock) {
		compositeSelector.getControl().setEnabled(!lock);
	}

	private IWorkbenchWindow getActiveWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
}
