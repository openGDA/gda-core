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
import static uk.ac.gda.ui.tool.ClientSWTElements.createScrolledComposite;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;
import static uk.ac.gda.ui.tool.WidgetUtilities.getDataObject;
import static uk.ac.gda.ui.tool.WidgetUtilities.selectAndNotify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * This widget can dynamically change its content based on the user selection.
 * <p>
 * An instance of this class contains a collection of {@link NamedComposite}. Each element of the collection is exposed as radio button at the top of the widget.
 * The selection of an element causes the associated {@code NamedComposite} to be build inside a {@link ScrolledComposite}
 * </p>
 * @author Maurizio Nagni
 */
public class SelectableContainedCompositeFactory implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(SelectableContainedCompositeFactory.class);

	private static final String CONFIGURATION_FACTORY = "configurationFactory";

	private List<Button> options = new ArrayList<>();
	private final List<NamedComposite> composites = new ArrayList<>();
	private final ClientMessages groupName;

	private ScrolledComposite scrolledInnerContainer;
	private Composite innerContainer;

	/**
	 * Creates an factory instance.
	 * @param composites the namedCompsite to select from
	 * @param groupName the label to use for the radio group
	 */
	public SelectableContainedCompositeFactory(List<NamedComposite> composites, ClientMessages groupName) {
		this.composites.addAll(composites);
		this.groupName = groupName;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.trace("Creating {}", this);
		// The main container
		Composite mainContainer = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainContainer);
		standardMarginHeight(mainContainer.getLayout());
		standardMarginWidth(mainContainer.getLayout());

		// The radio group container. Grab/Vertical is false as the vertical size should be constant
		Group acquisitionModes = createClientGroup(mainContainer, style, 1, groupName);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(acquisitionModes);

		Composite radioContainer = createClientCompositeWithGridLayout(acquisitionModes, SWT.NONE, composites.size());
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(radioContainer);
		standardMarginHeight(radioContainer.getLayout());

		// The scrollable container.
		scrolledInnerContainer = createScrolledComposite(mainContainer);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(scrolledInnerContainer);

		// The scrollable inner area.
		innerContainer = createClientCompositeWithGridLayout(scrolledInnerContainer, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(innerContainer);
		scrolledInnerContainer.setContent(innerContainer);

		// Creates one radio button for each NamedComposite and set it as data object so can be easily retrieved when selected
		composites.stream().forEach(c -> {
			Button option = createClientButton(radioContainer, SWT.RADIO, c.getName(), c.getTooltip());
			option.setData(CONFIGURATION_FACTORY, c);
			createClientGridDataFactory().applyTo(option);
			options.add(option);
			// the selection lister which will build on-demand this NamedComposite
			option.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::configurationRadioListener));
		});
		Optional.ofNullable(radioContainer.getChildren())
			.ifPresent(radios -> selectAndNotify((Button)radios[0], true));
		logger.trace("Created {}", this);
		return mainContainer;
	}

	private void configurationRadioListener(SelectionEvent event) {
		// Deletes, if any the existing composites inside the internal scrollable area
		Arrays.stream(innerContainer.getChildren())
			.forEach(Control::dispose);
		// Extracts the selected NamedComposite
		CompositeFactory cf = getDataObject(event.widget, CompositeFactory.class, CONFIGURATION_FACTORY);
		// build the selected NamedComposite
		Composite newComposite = cf.createComposite(innerContainer, SWT.NONE);
		// layout the children
		newComposite.layout();
		// Even after the layout, the newCompsite.getSize() is {0,0}.
		// Calling newComposite.computeSize calculate the default size, that is the widget size
		// In this way is possible to correctly set the size below which the scroll bars appear.
		scrolledInnerContainer.setMinSize(newComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT ));
	}
}
