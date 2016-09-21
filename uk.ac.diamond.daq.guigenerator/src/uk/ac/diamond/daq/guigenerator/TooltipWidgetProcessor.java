package uk.ac.diamond.daq.guigenerator;

import static uk.ac.diamond.daq.guigenerator.RichbeansAnnotationsInspector.TOOLTIP;

import java.util.Map;

import org.eclipse.swt.widgets.Control;
import org.metawidget.swt.SwtMetawidget;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;

/**
 * This processor adds tooltips to widgets
 *
 * @author Colin Palmer
 */
public class TooltipWidgetProcessor implements WidgetProcessor<Control, SwtMetawidget> {

	@Override
	public Control processWidget(Control widget, String elementName, Map<String, String> attributes,
			SwtMetawidget metawidget) {

			String tooltip = attributes.get(TOOLTIP);

			// If a tooltip is defined, add it to the widget
			if (tooltip != null && tooltip.length() > 0) {
				widget.setToolTipText(tooltip);
			}

		return widget;
	}
}
