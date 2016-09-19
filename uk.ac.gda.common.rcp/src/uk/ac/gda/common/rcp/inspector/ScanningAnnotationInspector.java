package uk.ac.gda.common.rcp.inspector;

import static org.metawidget.inspector.InspectionResultConstants.TRUE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.annotation.MaximumValue;
import org.eclipse.scanning.api.annotation.MinimumValue;
import org.eclipse.scanning.api.annotation.UiFilename;
import org.eclipse.scanning.api.annotation.UiTooltip;
import org.eclipse.scanning.api.annotation.Units;
import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.propertystyle.Property;

/**
 * This inspector is a copy of RichbeansAnnotationsInspector (in uk.ac.diamond.daq.guigenerator) which inspects some
 * annotations defined in org.eclipse.scanning.api.annotation.
 * <p>
 * This means the @MinimumValue, @MaximumValue and @Units annotations can be used in any code which depends on
 * org.eclipse.scanning.api without depending on uk.ac.diamond.daq.guigenerator.
 *
 * @author James Mudd
 * @author Colin Palmer
 */
public class ScanningAnnotationInspector extends BaseObjectInspector {

	// We'd like to be able to use "maximum-value" and "minimum-value" here, since they're already defined as standard
	// attribute names in Metawidget. Unfortunately, though, if both of them are applied to a numeric field,
	// SwtMetawidget uses a Scale slider widget which is inappropriate since it doesn't actually show the number!
	public static final String MINIMUM_VALUE = "minimumValue";
	public static final String MAXIMUM_VALUE = "maximumValue";
	public static final String UNITS = "units";
	public static final String TOOLTIP = "tooltip";
	public static final String FILENAME = "filename";

	@Override
	protected Map<String, String> inspectProperty(Property property) throws Exception {
		Map<String, String> attributes = new HashMap<String, String>();

		// Check the minimum value annotation
		MinimumValue minmiumValue = property.getAnnotation(MinimumValue.class);
		if (minmiumValue != null) {
			attributes.put(MINIMUM_VALUE, minmiumValue.value());
		}

		// Check the maximum value annotation
		MaximumValue maximumValue = property.getAnnotation(MaximumValue.class);
		if (maximumValue != null) {
			attributes.put(MAXIMUM_VALUE, maximumValue.value());
		}

		// Check the maximum value annotation
		Units units = property.getAnnotation(Units.class);
		if (units != null) {
			attributes.put(UNITS, units.value());
		}

		// Check the tooltip annotation
		UiTooltip tooltip = property.getAnnotation(UiTooltip.class);
		if (tooltip != null) {
			attributes.put(TOOLTIP, tooltip.value());
		}

		// Check the filename annotation
		UiFilename filename = property.getAnnotation(UiFilename.class);
		if (filename != null) {
			attributes.put(FILENAME, TRUE);
		}

		return attributes;
	}
}
