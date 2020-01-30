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
	public static final Point DEFAULT_SPAN = new Point(1, 1);

	private ClientSWTElements() {
	}

	public static final Point defaultCompositeMargin() {
		return new Point(10, 10);
	}

	public static final Composite createComposite(final Composite parent, int style, int columns) {
		return createComposite(parent, style, columns, SWT.LEFT, SWT.TOP);
	}

	public static final Composite createComposite(final Composite parent, int style, int columns, int hAlign,
			int vAlign) {
		Composite composite = new Composite(parent, style);
		GridLayoutFactory glf = GridLayoutFactory.swtDefaults();
		if (columns == 1) {
			glf = GridLayoutFactory.fillDefaults();
		}
		if (columns > 0) {
			glf.numColumns(columns);
		}
		glf.applyTo(composite);

		GridDataFactory gdf = GridDataFactory.swtDefaults();
		if (columns == 1) {
			gdf = GridDataFactory.fillDefaults();
		}
		gdf.grab(true, true).align(hAlign, vAlign).applyTo(composite);
		return composite;
	}

	public static final Composite createComposite(final Composite parent, int style) {
		return createComposite(parent, style, 1);
	}

	public static final Group createGroup(final Composite parent, int columns, final ClientMessages message) {
		return createGroup(parent, columns, message, true);
	}

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

	private static final GridLayoutFactory getGroupDefaultGridLayoutFactory(int columns, boolean equalWidth) {
		GridLayoutFactory glf = GridLayoutFactory.swtDefaults();
		if (columns == 1) {
			glf = GridLayoutFactory.fillDefaults();
		}
		glf.equalWidth(equalWidth);
		return glf;
	}

	public static final Group createGroup(final Composite parent, int columns, final ClientMessages message,
			GridLayoutFactory glf, GridDataFactory gdf) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(ClientResourceManager.getInstance().getGroupDefaultFont());
		if (glf == null) {
			glf = getGroupDefaultGridLayoutFactory(columns, true);
		}
		glf.numColumns(columns).applyTo(group);
		gdf.applyTo(group);
		if (message != null) {
			group.setText(ClientMessagesUtility.getMessage(message));
		}
		return group;
	}

	public static final Label createLabel(final Composite parent, int labelStyle) {
		return createLabel(parent, labelStyle, ClientMessages.EMPTY_MESSAGE, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message,
			final Point span) {
		return createLabel(parent, labelStyle, message, span, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message) {
		return createLabel(parent, labelStyle, ClientMessagesUtility.getMessage(message), DEFAULT_SPAN, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, String message) {
		return createLabel(parent, labelStyle, message, DEFAULT_SPAN, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, String message,
			final FontDescriptor fontDescriptor) {
		return createLabel(parent, labelStyle, message, DEFAULT_SPAN, fontDescriptor);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message,
			final Point span, final FontDescriptor fontDescriptor) {
		return createLabel(parent, labelStyle, ClientMessagesUtility.getMessage(message), span, fontDescriptor);
	}

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

	public static final Label createEmptyCell(final Composite parent, int labelStyle) {
		return createLabel(parent, labelStyle, "", DEFAULT_SPAN, null);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener) {
		return createText(parent, textStyle, listener, null);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span) {
		return createText(parent, textStyle, listener, span, null, null);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span, final ClientMessages tooltip, GridDataFactory gdf) {
		return createText(parent, textStyle, listener, span, tooltip, null, gdf);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span, final ClientMessages tooltip, Point minSize, GridDataFactory gdf) {
		return createText(parent, textStyle, listener, span, ClientMessagesUtility.getMessage(tooltip), minSize, gdf);
	}

	private static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			final Point span, final String tooltip, final Point minSize, GridDataFactory gdf) {
		textStyle = textStyle == 0 ? SWT.RIGHT | SWT.BORDER : 0;
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

	public static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip) {
		return createButton(parent, style, message, tooltip, Optional.empty(), Optional.empty());
	}

	public static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip, Image image) {
		return createButton(parent, style, ClientMessagesUtility.getMessage(message),
				ClientMessagesUtility.getMessage(tooltip), Optional.empty(), Optional.ofNullable(image));
	}

	public static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip, final Point span) {
		return createButton(parent, style, message, tooltip, Optional.ofNullable(span), Optional.empty());
	}

	private static final Button createButton(final Composite parent, int style, final ClientMessages message,
			final ClientMessages tooltip, final Optional<Point> span, final Optional<Image> image) {
		return createButton(parent, style, ClientMessagesUtility.getMessage(message),
				ClientMessagesUtility.getMessage(tooltip), span, image);
	}

	private static final Button createButton(final Composite parent, int style, String message, String tooltip,
			final Optional<Point> span, final Optional<Image> image) {
		Button button = new Button(parent, style);
		button.setFont(ClientResourceManager.getInstance().getButtonDefaultFont());
		button.setText(message);
		button.setToolTipText(tooltip);
		image.ifPresent(i -> {
			button.setImage(i);
			button.setSize(i.getImageData().width, i.getImageData().height);
			button.setSize(i.getImageData().width, i.getImageData().height);
		});
		if (!image.isPresent()) {
			button.setSize(DEFAULT_BUTTON_SIZE);
		}
		applySpan(button, span, Optional.ofNullable(button.getSize()));
		return button;
	}

	public static final Combo createCombo(final Composite parent, int style, final String[] items,
			final ClientMessages tooltip) {
		Combo combo = new Combo(parent, style);
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
		return GDAClientActivator.getImageDescriptor(image.getImagePath()).createImage();
	}

	public static void changeVAlignement(Control control, int vAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).verticalAlignment = vAlignement;
		}
	}

	public static void changeHAlignment(Control control, int hAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).horizontalAlignment = hAlignement;
		}
	}

	public static void changeHIndent(Control control, int hIndent) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).horizontalIndent = hIndent;
		}
	}

	public static void changeVerticalAlign(Control control, int vAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).verticalAlignment = vAlignement;
		}
	}

	public static void setTooltip(Control control, final ClientMessages tooltip) {
		control.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
	}

	public static UUID findParentUUID(Composite composite) {
		Composite old = composite;
		while (true) {
			if (UUID.class.isInstance(old.getData(CompositeFactory.COMPOSITE_ROOT))) {
				return UUID.class.cast(old.getData(CompositeFactory.COMPOSITE_ROOT));
			}
			old = old.getParent();
			if (old == null) {
				return null;
			}
		}
	}

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
	 */
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
	 */
	public static void gridDataMinSize(final Composite composite, final int minX, final int minY) {
		if (GridDataFactory.class.isInstance(composite.getLayoutData())) {
			GridDataFactory gdf = GridDataFactory.class.cast(composite.getLayoutData());
			gdf.minSize(minX, minY);
			gdf.applyTo(composite);
		}
	}

	private static GridDataFactory applySpan(final Control control, final Optional<Point> span,
			final Optional<Point> minSize) {
		return applySpan(control, span, minSize, null);
	}

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

}
