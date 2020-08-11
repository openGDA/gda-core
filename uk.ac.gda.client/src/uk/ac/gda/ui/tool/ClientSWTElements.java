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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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

	private ClientSWTElements() {
	}

	public static final Point defaultCompositeMargin() {
		return new Point(10, 10);
	}

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
	public static final Composite createComposite(final Composite parent, int style, int columns, int hAlign,
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
	 * @deprecated use {@link #createClientCompositeWithGridLayout(Composite, int, int)}. This has been done to support
	 *             a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final GridDataFactory createGridDataFactory() {
		return GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.END);
	}

	/**
	 * @deprecated use {@link #createGridDataFactory()}. This has been done to support a more consistent approach
	 *             described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final GridDataFactory createGridDataFactory(int hAlign, int vAlign) {
		return GridDataFactory.swtDefaults().grab(true, true).align(hAlign, vAlign);
	}

	/**
	 * @deprecated use {@link #createClientCompositeWithGridLayout(Composite, int, int)}. This has been done to support
	 *             a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Composite createComposite(final Composite parent, int style) {
		return createComposite(parent, style, 1);
	}

	/**
	 * @deprecated use {@link #createClientGroup(Composite, int, int, ClientMessages)}. This has been done to support a
	 *             more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Group createGroup(final Composite parent, int columns, final ClientMessages message) {
		return createGroup(parent, columns, message, true);
	}

	/**
	 * @deprecated use {@link #createClientGroup(Composite, int, int, ClientMessages)}. This has been done to support a
	 *             more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Group createGroup(final Composite parent, int columns, final ClientMessages message,
			boolean equalWidth) {
		GridLayoutFactory glf = getGroupDefaultGridLayoutFactory(columns, equalWidth);
		GridDataFactory gdf = GridDataFactory.fillDefaults();
		if (columns == 1) {
			gdf = GridDataFactory.fillDefaults();
		}
		gdf.grab(true, true).align(SWT.FILL, SWT.TOP);
		return createGroup(parent, columns, message, glf, gdf);
	}

	@Deprecated
	private static final GridLayoutFactory getGroupDefaultGridLayoutFactory(int columns, boolean equalWidth) {
		GridLayoutFactory glf = GridLayoutFactory.swtDefaults();
		if (columns == 1) {
			glf = GridLayoutFactory.fillDefaults();
		}
		glf.equalWidth(equalWidth);
		return glf;
	}

	/**
	 * @deprecated use {@link #createClientGroup(Composite, int, int, ClientMessages)}. This has been done to support a
	 *             more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Group createGroup(final Composite parent, int columns, final ClientMessages message,
			GridLayoutFactory glf, GridDataFactory gdf) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(ClientResourceManager.getInstance().getGroupDefaultFont());
		if (glf == null) {
			glf = getGroupDefaultGridLayoutFactory(columns, true);
		}
		glf.numColumns(columns).applyTo(group);
		gdf.indent(5, 5);
		gdf.applyTo(group);
		if (message != null) {
			group.setText(ClientMessagesUtility.getMessage(message));
		}
		return group;
	}

	/**
	 * @deprecated use {@link #createClientLabel(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createLabel(final Composite parent, int labelStyle) {
		return createLabel(parent, labelStyle, ClientMessages.EMPTY_MESSAGE, null);
	}

	/**
	 * @deprecated use {@link #createClientLabel(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message,
			final Point span) {
		return createLabel(parent, labelStyle, message, span, null);
	}

	/**
	 * @deprecated use {@link #createClientLabel(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message) {
		return createLabel(parent, labelStyle, ClientMessagesUtility.getMessage(message), DEFAULT_SPAN, null);
	}

	/**
	 * @deprecated use {@link #createClientLabel(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createLabel(final Composite parent, int labelStyle, String message) {
		return createLabel(parent, labelStyle, message, DEFAULT_SPAN, null);
	}

	/**
	 * @deprecated use {@link #createClientLabel(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createLabel(final Composite parent, int labelStyle, String message,
			final FontDescriptor fontDescriptor) {
		return createLabel(parent, labelStyle, message, DEFAULT_SPAN, fontDescriptor);
	}

	/**
	 * @deprecated use {@link #createClientLabel(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message,
			final Point span, final FontDescriptor fontDescriptor) {
		return createLabel(parent, labelStyle, ClientMessagesUtility.getMessage(message), span, fontDescriptor);
	}

	/**
	 * @deprecated use {@link #createClientLabel(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createLabel(final Composite parent, int labelStyle, String message, final Point span,
			final FontDescriptor fontDescriptor) {
		Label label = new Label(parent, labelStyle);
		label.setText(message);
		if (Objects.isNull(fontDescriptor)) {
			label.setFont(ClientResourceManager.getInstance().getLabelDefaultFont());
		} else {
			label.setFont(ClientResourceManager.getInstance().getFont(fontDescriptor));
		}
		GridDataFactory gdf = GridDataFactory.swtDefaults();
		gdf.align(SWT.BEGINNING, SWT.CENTER);
		if (Objects.nonNull(span)) {
			gdf.span(span);
		}
		gdf.applyTo(label);
		return label;
	}

	/**
	 * @deprecated use {@link #createEmptyCell(Composite, int)}. This has been done to support a more consistent
	 *             approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Label createEmptyCell(final Composite parent, int labelStyle) {
		return createLabel(parent, labelStyle, "", DEFAULT_SPAN, null);
	}

	/**
	 * @deprecated use {@link #createClientText(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener) {
		return createText(parent, textStyle, listener, null);
	}

	/**
	 * @deprecated use {@link #createClientText(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span) {
		return createText(parent, textStyle, listener, span, null, null);
	}

	/**
	 * @deprecated use {@link #createClientText(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span, final ClientMessages tooltip, GridDataFactory gdf) {
		return createText(parent, textStyle, listener, span, tooltip, null, gdf);
	}

	/**
	 * @deprecated use {@link #createClientText(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span, final ClientMessages tooltip, Point minSize, GridDataFactory gdf) {
		return createText(parent, textStyle, listener, span, ClientMessagesUtility.getMessage(tooltip), minSize, gdf);
	}

	/**
	 * @deprecated use {@link #createClientText(Composite, int, ClientMessages, Optional)}. This has been done to
	 *             support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	private static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span, final String tooltip, final Point minSize, GridDataFactory gdf) {
		textStyle = textStyle == 0 ? SWT.RIGHT | SWT.BORDER : textStyle;
		Text text = new Text(parent, textStyle);
		text.setFont(ClientResourceManager.getInstance().getTextDefaultFont());
		text.setToolTipText(tooltip);
		GridDataFactory internalGdf = applySpan(text, Optional.ofNullable(span), Optional.ofNullable(minSize), gdf);
		internalGdf.applyTo(text);
		if (listener != null) {
			text.addVerifyListener(listener);
		}
		return text;
	}

	/**
	 * @deprecated use {@link #createClientButton(Composite, int, ClientMessages, ClientMessages, Optional)}. This has
	 *             been done to support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip) {
		return createButton(parent, style, message, tooltip, Optional.empty(), Optional.empty());
	}

	/**
	 * @deprecated use {@link #createClientButton(Composite, int, ClientMessages, ClientMessages, Optional)}. This has
	 *             been done to support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip, ClientImages image) {
		return createButton(parent, style, ClientMessagesUtility.getMessage(message),
				ClientMessagesUtility.getMessage(tooltip), Optional.empty(), Optional.ofNullable(image));
	}

	/**
	 * @deprecated use {@link #createClientButton(Composite, int, ClientMessages, ClientMessages, Optional)}. This has
	 *             been done to support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip, final Point span) {
		return createButton(parent, style, message, tooltip, Optional.ofNullable(span), Optional.empty());
	}

	/**
	 * @deprecated use {@link #createClientButton(Composite, int, ClientMessages, ClientMessages, Optional)}. This has
	 *             been done to support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	private static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip, final Optional<Point> span, final Optional<ClientImages> image) {
		return createButton(parent, style, ClientMessagesUtility.getMessage(message),
				ClientMessagesUtility.getMessage(tooltip), span, image);
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

	/**
	 * @deprecated use {@link #createClientButton(Composite, int, ClientMessages, ClientMessages, Optional)}. This has
	 *             been done to support a more consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static final Button createButton(final Composite parent, int style, String message, String tooltip,
			final Optional<Point> span, final Optional<ClientImages> imageCode) {
		Button button = new Button(parent, style);
		button.setFont(ClientResourceManager.getInstance().getButtonDefaultFont());
		button.setText(message);
		button.setToolTipText(tooltip);
		imageCode.ifPresent(i -> {
			Image image = ClientSWTElements.getImage(i);
			button.setImage(image);
			button.setSize(image.getImageData().width, image.getImageData().height);
		});
		if (!imageCode.isPresent()) {
			button.setSize(DEFAULT_BUTTON_SIZE);
		}
		GridData gridData = new GridData();
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 5;
		button.setLayoutData(gridData);
		return button;
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

	public static final List createList(final Composite parent, int style, final String[] items,
			final ClientMessages tooltip, final Point span, final Point minSize) {
		List list = new List(parent, style | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		list.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		list.setItems(items);
		applySpan(list, Optional.ofNullable(span), Optional.ofNullable(minSize));
		return list;
	}

	public static final Slider createSlider(final Composite parent, int style) {
		Composite container = createComposite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(container);
		container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		FillLayout layout = new FillLayout();
		layout.marginHeight = layout.marginWidth = 1;
		container.setLayout(layout);
		Slider slider = new Slider(container, style);
		slider.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		return slider;
	}

	/**
	 * Retrieves an {@link Image} from a specific plug-in
	 *
	 * @param pluginId
	 *            the plug-in ID
	 * @param path
	 *            the image path inside the plug-in
	 * @return the required image
	 *
	 * @deprecated please use instead {@link #getImage(ClientImages)}. This method is available only for compatibility
	 *             purpose in case is not possible to update the client's images/icons package
	 */
	@Deprecated
	public static Image getImage(String pluginId, String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, path).createImage();
	}

	/**
	 * Retrieves an {@link Image} using a specific object's ClassLoader
	 *
	 * @param caller
	 *            the object used to get the ClassLoader
	 * @param path
	 *            path of the desired resource
	 * @return the required image
	 *
	 * @deprecated please use instead {@link #getImage(ClientImages)} This method is available only for compatibility
	 *             purpose in case is not possible to update the client's images/icons package
	 */
	@Deprecated
	public static Image getImage(Class<?> caller, String path) {
		return new Image(Display.getCurrent(), caller.getResourceAsStream(path));
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

	/**
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void changeVAlignement(Control control, int vAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).verticalAlignment = vAlignement;
		}
	}

	/**
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void changeHAlignment(Control control, int hAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).horizontalAlignment = hAlignement;
		}
	}

	/**
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void changeHIndent(Control control, int hIndent) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).horizontalIndent = hIndent;
		}
	}

	/**
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void changeVerticalAlign(Control control, int vAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).verticalAlignment = vAlignement;
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
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void gridDataSpan(final Composite composite, final int hSpan, final int vSpan) {
		if (GridDataFactory.class.isInstance(composite.getLayoutData())) {
			GridDataFactory gdf = GridDataFactory.class.cast(composite.getLayoutData());
			gdf.span(hSpan, vSpan);
			gdf.applyTo(composite);
		}
	}

	/**
	 * Change a {@link Composite} item alignment using its existing {@link GridDataFactory}
	 *
	 * @param composite
	 * @param hAlign
	 * @param vAlign
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void gridDataAlign(final Composite composite, final int hAlign, final int vAlign) {
		if (GridDataFactory.class.isInstance(composite.getLayoutData())) {
			GridDataFactory gdf = GridDataFactory.class.cast(composite.getLayoutData());
			gdf.align(hAlign, vAlign);
			gdf.applyTo(composite);
		}
	}

	/**
	 * Change a {@link Composite} item "grab" using its existing {@link GridDataFactory}
	 *
	 * @param composite
	 * @param horizontal
	 * @param vertical
	 */
	public static void gridDataGrab(final Composite composite, final boolean horizontal, final boolean vertical) {
		if (GridDataFactory.class.isInstance(composite.getLayoutData())) {
			GridDataFactory gdf = GridDataFactory.class.cast(composite.getLayoutData());
			gdf.grab(horizontal, vertical);
			gdf.applyTo(composite);
		}
	}

	/**
	 * Change a {@link Composite} item minSize using its existing {@link GridDataFactory}
	 *
	 * @param composite
	 * @param minX
	 * @param minY
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void gridDataMinSize(final Composite composite, final int minX, final int minY) {
		if (GridDataFactory.class.isInstance(composite.getLayoutData())) {
			GridDataFactory gdf = GridDataFactory.class.cast(composite.getLayoutData());
			gdf.minSize(minX, minY);
			gdf.applyTo(composite);
		}
	}

	/**
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	private static GridDataFactory applySpan(final Control control, final Optional<Point> span,
			final Optional<Point> minSize) {
		return applySpan(control, span, minSize, null);
	}

	/**
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	private static GridDataFactory applySpan(final Control control, final Optional<Point> span,
			final Optional<Point> minSize, GridDataFactory gdf) {
		if (gdf == null) {
			gdf = GridDataFactory.swtDefaults().grab(true, false).align(SWT.BEGINNING, SWT.BEGINNING);
		}
		gdf.span(span.orElse(DEFAULT_SPAN));
		gdf.minSize(minSize.orElse(DEFAULT_BUTTON_SIZE));
		gdf.applyTo(control);
		return gdf;
	}

	/**
	 * Change a {@link Composite} item minSize using its existing {@link GridDataFactory}
	 *
	 * @param composite
	 * @param width
	 * @param height
	 * @deprecated use {@link #createClientGridDataFactory()} to define the layout. This has been done to support a more
	 *             consistent approach described in
	 *             <a href="https://confluence.diamond.ac.uk/display/DIAD/User+Interfaces+for+DIAD">Confluence</a>
	 */
	@Deprecated
	public static void gridMargin(final Composite composite, final int width, final int height) {
		if (GridLayoutFactory.class.isInstance(composite.getLayout())) {
			GridLayoutFactory glf = GridLayoutFactory.class.cast(composite.getLayout());
			glf.margins(width, height);
			glf.applyTo(composite);
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
	public static final Label createClientLabel(final Composite parent, int style, final ClientMessages message,
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
		Label label = new Label(parent, style);
		label.setText(ClientMessagesUtility.getMessage(message));
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
	 *            the button image. May be empty
	 * @return a new Label component
	 * @deprecated use {@link #createClientButton(Composite, int, ClientMessages, ClientMessages, ClientImages)}
	 */
	@Deprecated
	public static final Button createClientButton(final Composite parent, int style, ClientMessages message,
			ClientMessages tooltip, final Optional<ClientImages> imageCode) {
		Button button = new Button(parent, style);
		button.setFont(ClientResourceManager.getInstance().getButtonDefaultFont());
		button.setText(ClientMessagesUtility.getMessage(message));
		button.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		imageCode.ifPresent(i -> {
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
	 */
	public static final Text createClientText(final Composite parent, int style, final ClientMessages tooltip,
			final Optional<VerifyListener> listener) {
		style = style == 0 ? SWT.RIGHT | SWT.BORDER : style;
		Text text = new Text(parent, style);
		text.setFont(ClientResourceManager.getInstance().getTextDefaultFont());
		text.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		listener.ifPresent(text::addVerifyListener);
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
	 */
	public static final void createClientEmptyCell(final Composite parent, Point hint) {
		Label label = createClientLabel(parent, SWT.NONE, ClientMessages.EMPTY_MESSAGE, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().hint(hint).applyTo(label);
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
}
