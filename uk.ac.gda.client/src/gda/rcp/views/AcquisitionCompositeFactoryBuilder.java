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

package gda.rcp.views;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Generic layout for an acquisition configuration.
 * It splits horizontally the composite in three areas:
 * <ol>
 * <li>
 *  <i>top</i>: contains the acquisition configuration elements
 * </li>
 * <li>
 *  <i>Control buttons</i>: typically allows the user to Run, Save the acquisition actually edited or create a New one
 * </li>
 * <li>
 *  <i>bottom</i>: allows the user to browser other previously saved acquisition configurations
 * </li>
 * </ol>
 *
 * <p>Documented further on <a href="https://confluence.diamond.ac.uk/x/pyKeBg">Confluence</a>
 *
 * @author Maurizio Nagni
 */
public class AcquisitionCompositeFactoryBuilder {

	private Optional<CompositeFactory> top = Optional.empty();
	private Optional<CompositeFactory> bottom = Optional.empty();

	private Composite topComposite;
	private Composite controlButtonsComposite;
	private Composite bottomComposite;

	public CompositeFactory build() {

		return (parent, style) -> {
			var composite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
			standardMarginHeight(composite.getLayout());
			standardMarginWidth(composite.getLayout());

			if (top.isPresent()) {
				topComposite = createClientCompositeWithGridLayout(composite, SWT.NONE, 1);
				createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(topComposite);
			}

			controlButtonsComposite = createClientCompositeWithGridLayout(composite, SWT.NONE, 1);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(controlButtonsComposite);

			if (bottom.isPresent()) {
				bottomComposite = createClientCompositeWithGridLayout(composite, SWT.NONE, 1);
				createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(bottomComposite);
			}

			/* for some reason, combining these conditionals with those above
			 * produces unexpected (and unwanted) differences in style */
			top.ifPresent(factory -> factory.createComposite(topComposite, style));
			bottom.ifPresent(factory ->	factory.createComposite(bottomComposite, style));

			return composite;
		};
	}

	public AcquisitionCompositeFactoryBuilder addTopArea(CompositeFactory compositeFactory) {
		this.top = Optional.ofNullable(compositeFactory);
		return this;
	}

	public Supplier<Composite> getControlButtonsCompositeSupplier() {
		return this::getControlButtonsComposite;
	}

	public AcquisitionCompositeFactoryBuilder addBottomArea(CompositeFactory compositeFactory) {
		this.bottom = Optional.of(compositeFactory);
		return this;
	}

	private Composite getControlButtonsComposite() {
		return controlButtonsComposite;
	}

}
