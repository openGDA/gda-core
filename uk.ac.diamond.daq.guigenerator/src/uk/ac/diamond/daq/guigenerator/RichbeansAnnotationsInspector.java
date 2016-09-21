package uk.ac.diamond.daq.guigenerator;

import static org.metawidget.inspector.InspectionResultConstants.NAME;
import static org.metawidget.inspector.InspectionResultConstants.READ_ONLY;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.richbeans.api.generator.RichbeansAnnotations;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MaximumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MinimumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.RowDeleteAction;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiAction;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiHidden;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiReadOnly;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiTooltip;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.Units;
import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.BaseObjectInspectorConfig;
import org.metawidget.inspector.impl.Trait;
import org.metawidget.inspector.impl.actionstyle.Action;
import org.metawidget.inspector.impl.propertystyle.Property;

/**
 * This inspector checks for the annotations in {@link RichbeansAnnotations} and adds them to the Metawidget pipeline so
 * they can be applied by the {@link RichbeansDecoratorWidgetProcessor}.
 *
 * @author James Mudd
 */
public class RichbeansAnnotationsInspector extends BaseObjectInspector {

	// We'd like to be able to use "maximum-value" and "minimum-value" here, since they're already defined as standard
	// attribute names in Metawidget. Unfortunately, though, if both of them are applied to a numeric field,
	// SwtMetawidget uses a Scale slider widget which is inappropriate since it doesn't actually show the number!
	public static final String MINIMUM_VALUE = "minimumValue";
	public static final String MAXIMUM_VALUE = "maximumValue";
	public static final String UNITS = "units";
	public static final String TOOLTIP = "tooltip";
	public static final String DELETE_METHOD = "deleteMethod";
	public static final String HIDDEN = "hidden";
	public static final String ACTION = "action";

	public RichbeansAnnotationsInspector() {
		super(config());
	}

	private static BaseObjectInspectorConfig config() {
		BaseObjectInspectorConfig baseObjectInspectorConfig = new BaseObjectInspectorConfig();
		baseObjectInspectorConfig.setActionStyle(new RichbeansActionStyle());
		return baseObjectInspectorConfig;
	}

	@Override
	public Map<String, String> inspectProperty(Property property) throws Exception {
		Map<String, String> attributes = new HashMap<String, String>();

		attributes.putAll(setValueAttribute(MinimumValue.class, MinimumValue::value, MINIMUM_VALUE, property));
		attributes.putAll(setValueAttribute(MaximumValue.class, MaximumValue::value, MAXIMUM_VALUE, property));
		attributes.putAll(setValueAttribute(Units.class, Units::value, UNITS, property));
		attributes.putAll(setValueAttribute(UiTooltip.class, UiTooltip::value, TOOLTIP, property));
		attributes.putAll(setValueAttribute(RowDeleteAction.class, RowDeleteAction::value, DELETE_METHOD, property));
		attributes.putAll(setBooleanValue(UiHidden.class, HIDDEN, property));
		attributes.putAll(setBooleanValue(UiReadOnly.class, READ_ONLY, property));

		return attributes;
	}

	@Override
	public Map<String, String> inspectAction(Action action) throws Exception {
		Map<String, String> attributes = new HashMap<>();
		if ( action.isAnnotationPresent( UiAction.class ) ) {
			attributes.put( NAME, action.getName() );
		}
		return attributes;
	}

	private <T extends Annotation> Map<String, String> setBooleanValue(Class<T> annotationClazz, String attributeName, Trait property) {
		return setValueAttribute(annotationClazz, (ann) -> "true", attributeName, property);
	}

	private <T extends Annotation> Map<String,String> setValueAttribute(Class<T> annotationClazz, Function<T, String> valueExtractor, String attributeName, Trait property) {
		T annotation = property.getAnnotation(annotationClazz);
		if (annotation != null) {
			Map<String, String> attributes = new HashMap<>();
			attributes.put(attributeName, valueExtractor.apply(annotation));
			return attributes;
		}
		return Collections.emptyMap();
	}
}
