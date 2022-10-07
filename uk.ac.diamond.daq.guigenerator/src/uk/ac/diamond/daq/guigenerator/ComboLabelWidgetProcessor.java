package uk.ac.diamond.daq.guigenerator;

import static org.metawidget.inspector.InspectionResultConstants.LOOKUP;
import static org.metawidget.inspector.InspectionResultConstants.LOOKUP_LABELS;

import java.util.Map;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.metawidget.swt.SwtMetawidget;
import org.metawidget.util.ArrayUtils;
import org.metawidget.widgetprocessor.iface.WidgetProcessor;

/**
 * This processor switches the items in any SWT Combo boxes to the contents of the LOOKUP_LABELS attribute, if defined.
 * TODO With the current databinding implementation, this works well for enums but will break the databinding for String
 * fields which have had lookup labels added in their metadata.
 *
 * @author Colin Palmer
 */
public class ComboLabelWidgetProcessor implements WidgetProcessor<Control, SwtMetawidget> {

	@Override
	public Control processWidget(Control widget, String elementName, Map<String, String> attributes,
			SwtMetawidget metawidget) {

		// Check if the widget is an SWT Combo
		if (widget instanceof Combo) {
			Combo combo = (Combo) widget;
			String lookup = attributes.get(LOOKUP);
			String lookupLabels = attributes.get(LOOKUP_LABELS);

			// If lookup labels are defined, use them instead of the existing values
			if (lookup != null && !"".equals(lookup) && lookupLabels != null && !"".equals(lookupLabels)) {

				final String[] items = ArrayUtils.fromString(lookup);
				final String[] labels = ArrayUtils.fromString(lookupLabels);

				for (int comboIndex = 0; comboIndex < combo.getItemCount(); comboIndex++) {
					for (int labelIndex = 0; labelIndex < items.length && labelIndex < labels.length; labelIndex++) {
						if (combo.getItem(comboIndex).equals(items[labelIndex])) {
							combo.setItem(comboIndex, labels[labelIndex]);
							break;
						}
					}
				}
			}
		}
		return widget;
	}
}
