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

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;
import static uk.ac.gda.ui.tool.WidgetUtilities.getDataObject;
import static uk.ac.gda.ui.tool.WidgetUtilities.selectAndNotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;
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

	private static final Logger logger = LoggerFactory.getLogger(SelectableContainedCompositeFactory.class);

	private static final String CONFIGURATION_FACTORY = "configurationFactory";

	private final List<Button> options = new ArrayList<>();
	private final List<NamedCompositeFactory> composites = new ArrayList<>();
	private final ClientMessages groupName;

	private ClientScrollableContainer clientScrollableContainer;
	/**
	 * The actual selected widget
	 */
	private Widget selectedWidget;

	/**
	 * Creates an factory instance.
	 * @param composites the namedCompsite to select from
	 * @param groupName the label to use for the radio group
	 */
	public SelectableContainedCompositeFactory(List<NamedCompositeFactory> composites, ClientMessages groupName) {
		this.composites.addAll(composites);
		this.groupName = groupName;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.trace("Creating {}", this);
		// The main container
		var mainContainer = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainContainer);
		standardMarginHeight(mainContainer.getLayout());
		standardMarginWidth(mainContainer.getLayout());

		// The radio group container. Grab/Vertical is false as the vertical size should be constant
		var acquisitionModes = createClientGroup(mainContainer, style, 1, groupName);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(acquisitionModes);

		var radioContainer = createClientCompositeWithGridLayout(acquisitionModes, SWT.NONE, composites.size());
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(radioContainer);
		standardMarginHeight(radioContainer.getLayout());

		clientScrollableContainer = new ClientScrollableContainer(this);
		clientScrollableContainer.createComposite(mainContainer, SWT.NONE);

		// Creates one radio button for each NamedComposite and set it as data object so can be easily retrieved when selected
		composites.stream().forEach(c -> {
			var option = createClientButton(radioContainer, SWT.RADIO, c.getName(), c.getTooltip());
			option.setData(CONFIGURATION_FACTORY, c);
			createClientGridDataFactory().applyTo(option);
			options.add(option);
			// the selection lister which will build on-demand this NamedComposite
			option.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::configurationRadioListener));
		});
		Optional.ofNullable(radioContainer.getChildren())
			.filter(ArrayUtils::isNotEmpty)
			.ifPresent(radios -> selectAndNotify((Button)radios[0], true));
		logger.trace("Created {}", this);
		return mainContainer;
	}

	private void configurationRadioListener(SelectionEvent event) {
		// Reject messages from not selected buttons
		if (!((Button)event.widget).getSelection())
			return;

		//Reject messages from the already selected widget
		if (event.widget.equals(selectedWidget))
			return;

		// Deletes, if any the existing composites inside the internal scrollable area
		clientScrollableContainer.cleanInnerContainer();
		// Extracts the selected NamedComposite
		CompositeFactory contentFactory = getDataObject(event.widget, CompositeFactory.class, CONFIGURATION_FACTORY);
		clientScrollableContainer.populateInnerContainer(contentFactory);

		selectedWidget = event.widget;
	}

	/**
	 * Enable, or disable, the selectable widget
	 * @param lock {@code true} to lock, {@code false} to unlock
	 */
	@Override
	public void lock(boolean lock) {
		if (lock) {
			options.forEach(b -> b.setEnabled(false));
		} else {
			options.forEach(b -> b.setEnabled(true));
		}
	}
}
