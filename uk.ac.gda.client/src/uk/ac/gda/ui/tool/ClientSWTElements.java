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

package uk.ac.gda.ui.tool;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

import gda.rcp.GDAClientActivator;
import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Utility class for create SWT Client standard SWT elements. GridLayout is the default layout for all the elements
 *
 * @author Maurizio Nagni
 */
public final class ClientSWTElements {

	public static final Point DEFAULT_COMPOSITE_SIZE = new Point(500, SWT.DEFAULT);
	public static final Point DEFAULT_BUTTON_SIZE = new Point(100, 50);
	public static final Point SMALL_BUTTON_SIZE = new Point(60, 30);
	public static final Point DEFAULT_TEXT_SIZE = new Point(80, SWT.DEFAULT);
	public static final Point DEFAULT_SPAN = new Point(1, 1);
	public static final int DEFAULT_MARGIN_HEIGHT = 5;
	public static final int DEFAULT_MARGIN_WIDTH = 5;

	private ClientSWTElements() {
	}

	public static final Point defaultCompositeMargin() {
		return new Point(10, 10);
	}

	/**
	 * @param parent
	 * @param style
	 * @param columns
	 * @return a new {@code composite}
	 * @deprecated use {@link #createClientCompositeWithGridLayout(Composite, int, int)}. This has been done to support
	 *             a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>. To be removed on GDA 9.25
	 */
	@Deprecated
	public static final Composite createComposite(final Composite parent, int style, int columns) {
		return createComposite(parent, style, columns, SWT.LEFT, SWT.TOP);
	}

	/**
	 * @param parent
	 * @param style
	 * @param columns
	 * @param hAlign
	 * @param vAlign
	 * @return a new {@code composite}
	 * @deprecated use {@link #createClientCompositeWithGridLayout(Composite, int, int)}. This has been done to support
	 *             a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	private static final Composite createComposite(final Composite parent, int style, int columns, int hAlign,
			int vAlign) {
		Composite composite = createClientCompositeWithGridLayout(parent, style, columns);
		GridDataFactory gdf = createGridDataFactory(hAlign, vAlign).grab(true, false);
		if (columns == 1) {
			gdf = GridDataFactory.fillDefaults();
		}
		gdf.applyTo(composite);
		return composite;
	}

	/**
	 * @deprecated use {@link #createGridDataFactory()}. This has been done to support a more consistent approach
	 *             described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	private static final GridDataFactory createGridDataFactory(int hAlign, int vAlign) {
		return GridDataFactory.swtDefaults().grab(true, true).align(hAlign, vAlign);
	}

	public static final void updateButton(final Button button, ClientMessages message, ClientMessages tooltip,
			final ClientImages imageCode) {
		updateButton(button, ClientMessagesUtility.getMessage(message), ClientMessagesUtility.getMessage(tooltip),
				imageCode);
	}

	private static final void updateButton(final Button button, String message, String tooltip,
			final ClientImages imageCode) {
		Optional.ofNullable(message).ifPresent(button::setText);
		Optional.ofNullable(tooltip).ifPresent(button::setToolTipText);

		Optional.ofNullable(imageCode).ifPresent(i -> {
			Image image = ClientSWTElements.getImage(i);
			button.setImage(image);
			button.setSize(image.getImageData().width, image.getImageData().height);
			button.setSize(image.getImageData().width, image.getImageData().height);
		});

		if (imageCode == null) {
			button.setSize(DEFAULT_BUTTON_SIZE);
		}
		button.getParent().layout(true, true);
	}

	public static final Combo createCombo(final Composite parent, int style, final String[] items,
			final ClientMessages tooltip) {
		Combo combo = new Combo(parent, style);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 5;
		combo.setLayoutData(gridData);
		combo.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		combo.setItems(items);
		return combo;
	}

	/**
	 * Retrieves an {@link Image} from the client's standard folder, which is located into uk.ac.gda.client package.
	 * This method uses {@link ClientImages} in order to both have a standard reference to a specific icon and force the
	 * developer to harmonise the icons around the various GDA client views/perspectives
	 *
	 * @param image
	 *            the image enum
	 * @return the required image
	 */
	public static Image getImage(ClientImages image) {
		try {
			return Optional.ofNullable(GDAClientActivator.getImageDescriptor(image.getImagePath()))
					.orElseThrow(Exception::new).createImage();
		} catch (Exception e) {
			return getImage(ClientImages.NO_IMAGE);
		}
	}

	public static void setTooltip(Control control, final ClientMessages tooltip) {
		control.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
	}

	/**
	 * Queries either in {@code composite} or its parents then upward, {@link Composite#getData(String)} for
	 * CompositeFactory.COMPOSITE_ROOT
	 *
	 * @param composite
	 * @return the {@link UUID} of the parent, eventually {@code Optional.empty()}
	 */
	public static Optional<UUID> findParentUUID(Composite composite) {
		if (composite.isDisposed())
			return Optional.empty();
		Composite old = composite;
		while (true) {
			if (UUID.class.isInstance(old.getData(CompositeFactory.COMPOSITE_ROOT))) {
				return Optional.ofNullable(UUID.class.cast(old.getData(CompositeFactory.COMPOSITE_ROOT)));
			}
			old = old.getParent();
			if (old == null) {
				return Optional.empty();
			}
		}
	}

	/**
	 * Creates basic a {@link GridDataFactory} filling horizontal space and bottom aligned. This supports the approach
	 * described in <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @return a grid data factory
	 */
	public static final GridDataFactory createClientGridDataFactory() {
		return GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END);
	}

	/**
	 * Creates a basic @{@code Composite} component, applying a simple {@link GridLayoutFactory}. This supports the
	 * approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the component
	 * @param columns
	 *            the number of columns in the grid layout
	 * @return a new Component
	 */
	public static final Composite createClientCompositeWithGridLayout(final Composite parent, int style, int columns) {
		Composite composite = new Composite(parent, style);
		GridLayoutFactory glf = GridLayoutFactory.fillDefaults();
		if (columns > 0) {
			glf.numColumns(columns);
		}
		glf.applyTo(composite);
		return composite;
	}

	/**
	 * Creates a basic {@link Label} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the label
	 * @param message
	 *            the text to display
	 * @return a new label
	 */
	public static final Label createClientLabel(final Composite parent, int style, final ClientMessages message) {
		return createClientLabel(parent, style, message, Optional.empty());
	}

	/**
	 * Creates a basic {@link Label} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the label
	 * @param message
	 *            the text to display
	 * @param fontDescriptor
	 *            the label font. If empty uses {@link ClientResourceManager#getLabelDefaultFont()}
	 * @return a new label
	 * @deprecated Use instead {@link #createClientLabel(Composite, int, ClientMessages, FontDescriptor)}
	 */
	@Deprecated
	private static final Label createClientLabel(final Composite parent, int style, final ClientMessages message,
			Optional<FontDescriptor> fontDescriptor) {
		Label label = new Label(parent, style);
		label.setText(ClientMessagesUtility.getMessage(message));
		if (!fontDescriptor.isPresent()) {
			label.setFont(ClientResourceManager.getInstance().getLabelDefaultFont());
		} else {
			label.setFont(ClientResourceManager.getInstance().getFont(fontDescriptor.get()));
		}
		return label;
	}

	/**
	 * Creates a basic {@link Label} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the label
	 * @param message
	 *            the text to display
	 * @param fontDescriptor
	 *            the label font. If empty uses {@link ClientResourceManager#getLabelDefaultFont()}
	 * @return a new label
	 */
	public static final Label createClientLabel(final Composite parent, int style, final ClientMessages message,
			FontDescriptor fontDescriptor) {
		Label label = createClientLabel(parent, style, ClientMessagesUtility.getMessage(message));
		label.setFont(ClientResourceManager.getInstance().getFont(fontDescriptor));
		return label;
	}

	/**
	 * Creates a basic {@link Label} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the label
	 * @param message
	 *            the text to display
	 * @return a new label
	 */
	public static final Label createClientLabel(final Composite parent, int style, final String message) {
		Label label = new Label(parent, style);
		label.setFont(ClientResourceManager.getInstance().getLabelDefaultFont());
		label.setText(message);
		return label;
	}

	/**
	 * Creates a basic {@link Button} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the button (SWP.PUSH, SWT.CHECK, SWT.RADIO, ...)
	 * @param message
	 *            the text to display in the button
	 * @param tooltip
	 *            the tooltip to display
	 * @param imageCode
	 *            the button image
	 * @return a new Button component
	 */
	public static final Button createClientButton(final Composite parent, int style, ClientMessages message,
			ClientMessages tooltip, final ClientImages imageCode) {
		Button button = createClientButton(parent, style, message, tooltip);
		Optional.ofNullable(imageCode).ifPresent(i -> {
			Image image = ClientSWTElements.getImage(i);
			button.setImage(image);
			button.setSize(image.getImageData().width, image.getImageData().height);
		});
		return button;
	}

	/**
	 * Creates a basic {@link Button} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the button (SWP.PUSH, SWT.CHECK, SWT.RADIO, ...)
	 * @param message
	 *            the text to display in the button
	 * @param tooltip
	 *            the tooltip to display
	 * @return a new Label component
	 */
	public static final Button createClientButton(final Composite parent, int style, ClientMessages message,
			ClientMessages tooltip) {
		Button button = new Button(parent, style);
		button.setFont(ClientResourceManager.getInstance().getButtonDefaultFont());
		button.setText(ClientMessagesUtility.getMessage(message));
		button.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		return button;
	}

	/**
	 * Creates a basic {@link Text} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the button. If SWT.NONE applies SWT.RIGHT | SWT.BORDER.
	 * @param tooltip
	 *            the tooltip to display
	 * @param listener
	 *            a listener to validate the text
	 * @return a new Text component
	 * @deprecated Please use either {@link #createClientText(Composite, int, ClientMessages, VerifyListener)} or {@link #createClientText(Composite, int, ClientMessages)}
	 */
	@Deprecated
	private static final Text createClientText(final Composite parent, int style, final ClientMessages tooltip,
			final Optional<VerifyListener> listener) {
		VerifyListener vl = listener.isPresent() ? listener.get() : null;
		return createClientText(parent, style, tooltip, vl);
	}

	/**
	 * Creates a basic {@link Text} component. No validation is done on he text.
	 * This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the button. If SWT.NONE applies SWT.RIGHT | SWT.BORDER.
	 * @param tooltip
	 *            the tooltip to display. May be {@code null}
	 * @return a new Text component
	 */
	public static final Text createClientText(final Composite parent, int style, final ClientMessages tooltip) {
		return createClientText(parent, style, tooltip, Optional.empty());
	}

	/**
	 * Creates a basic {@link Text} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the the button. If SWT.NONE applies SWT.RIGHT | SWT.BORDER.
	 * @param tooltip
	 *            the tooltip to display. May be {@code null}
	 * @param listener
	 *            a listener to validate the text. May be {@code null}
	 * @return a new Text component
	 */
	public static final Text createClientText(final Composite parent, int style, final ClientMessages tooltip,
			final VerifyListener listener) {
		style = style == 0 ? SWT.RIGHT | SWT.BORDER : style;
		Text text = new Text(parent, style);
		text.setFont(ClientResourceManager.getInstance().getTextDefaultFont());

		Optional.ofNullable(tooltip)
			.map(ClientMessagesUtility::getMessage)
			.ifPresent(text::setToolTipText);

		Optional.ofNullable(listener)
			.ifPresent(text::addVerifyListener);
		return text;
	}

	/**
	 * Creates an empty {@link Label} component to use a separator. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param hint
	 *            the horizontal/vertical space
	 * @deprecated Use {@link #createClientGridDataFactory()}
	 */
	@Deprecated
	public static final void createClientEmptyCell(final Composite parent, Point hint) {
		Label label = createClientLabel(parent, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().hint(hint).applyTo(label);
	}

	/**
	 * Creates an empty {@link Label} component to use a separator. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param hspan
	 *            number of columns spanned by the control
	 * @param vspan
	 *            number of rows spanned by the control
	 */
	public static final void createClientEmptyCell(final Composite parent, int hspan, int vspan) {
		Label label = createClientLabel(parent, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().span(hspan, vspan).applyTo(label);
	}

	/**
	 * Creates a basic {@link Group} component. This supports the approach described in
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 *
	 * @param parent
	 *            where materialise the component
	 * @param style
	 *            the style to apply to the component
	 * @param columns
	 *            the number of columns in the group
	 * @param message
	 *            the group header text
	 * @return a new group component
	 */
	public static final Group createClientGroup(final Composite parent, int style, int columns,
			final ClientMessages message) {
		Group group = new Group(parent, style);
		GridLayoutFactory glf = GridLayoutFactory.fillDefaults();
		if (message != null) {
			group.setText(ClientMessagesUtility.getMessage(message));
			group.setFont(ClientResourceManager.getInstance().getGroupDefaultFont());
		}
		if (columns > 0) {
			glf.numColumns(columns);
		}
		glf.applyTo(group);
		return group;
	}

	/**
	 * Creates a standard  {@link ScrolledComposite} with a border and horizontal/vertical bars
	 * @param parent where the composite will belong
	 * @return a scrolled composite
	 */
	public static final ScrolledComposite createScrolledComposite(Composite parent) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		GridLayoutFactory.fillDefaults().applyTo(scrolledComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);
		return scrolledComposite;
	}

	/**
	 * Adds a {@link #DEFAULT_MARGIN_HEIGHT} to a {@link GridLayout}
	 * @param layout the layout to amend
	 */
	public static final void standardMarginHeight(Layout layout) {
		((GridLayout)layout).marginHeight = DEFAULT_MARGIN_HEIGHT;
	}

	/**
	 * Adds a {@link #DEFAULT_MARGIN_WIDTH} to a {@link GridLayout}
	 * @param layout the layout to amend
	 */
	public static final void standardMarginWidth(Layout layout) {
		((GridLayout)layout).marginWidth = DEFAULT_MARGIN_WIDTH;
	}
}
