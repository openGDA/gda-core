package uk.ac.diamond.daq.guigenerator;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.iface.DomInspector;
import org.metawidget.inspector.impl.propertystyle.javabean.JavaBeanPropertyStyle;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import org.metawidget.inspector.xml.XmlInspector;
import org.metawidget.inspector.xml.XmlInspectorConfig;
import org.metawidget.swt.SwtMetawidget;
import org.metawidget.swt.widgetbuilder.OverriddenWidgetBuilder;
import org.metawidget.swt.widgetbuilder.ReadOnlyWidgetBuilder;
import org.metawidget.swt.widgetbuilder.SwtWidgetBuilder;
import org.metawidget.widgetbuilder.composite.CompositeWidgetBuilder;
import org.metawidget.widgetbuilder.composite.CompositeWidgetBuilderConfig;

public class GuiGeneratorService implements IGuiGeneratorService {

	// Initialise metawidget objects. All of these should be immutable so only one (static) instance is required
	private static final OverriddenWidgetBuilder OVERRIDDEN_WIDGET_BUILDER = new OverriddenWidgetBuilder();
	private static final ReadOnlyWidgetBuilder READ_ONLY_WIDGET_BUILDER = new ReadOnlyWidgetBuilder();
	private static final TableWidgetBuilder TABLE_WIDGET_BUILDER = new TableWidgetBuilder();
	private static final SwtWidgetBuilder SWT_WIDGET_BUILDER = new SwtWidgetBuilder();
	private static final FileChooserWidgetBuilder FILE_CHOOSER_WIDGET_BUILDER = new FileChooserWidgetBuilder();
	@SuppressWarnings("unchecked") // we know the generic array created in setWidgetBuilders() is type-safe
	private static final CompositeWidgetBuilderConfig<Control, SwtMetawidget> COMPOSITE_WIDGET_BUILDER_CONFIG =
			new CompositeWidgetBuilderConfig<Control, SwtMetawidget>().setWidgetBuilders(
					OVERRIDDEN_WIDGET_BUILDER, READ_ONLY_WIDGET_BUILDER, TABLE_WIDGET_BUILDER,
					FILE_CHOOSER_WIDGET_BUILDER, SWT_WIDGET_BUILDER);
	private static final CompositeWidgetBuilder<Control, SwtMetawidget> COMPOSITE_WIDGET_BUILDER = new CompositeWidgetBuilder<>(COMPOSITE_WIDGET_BUILDER_CONFIG);

	private static final ComboLabelWidgetProcessor COMBO_LABEL_PROCESSOR = new ComboLabelWidgetProcessor();
	private static final TooltipWidgetProcessor TOOLTIP_PROCESSOR = new TooltipWidgetProcessor();
	private static final RichbeansDecoratorWidgetProcessor DECORATOR_PROCESSOR = new RichbeansDecoratorWidgetProcessor();
	private static final TwoWayDataBindingProcessor BINDING_PROCESSOR = new TwoWayDataBindingProcessor();

	// Initialise a set for the inspectors. The reference to the set is final but the contents will change.
	private static final Set<DomInspector<?>> DOM_INSPECTORS = new LinkedHashSet<>();
	private static final DomInspector<?>[] EMPTY_INSPECTOR_ARRAY = new DomInspector<?>[0];

	private static CompositeInspector inspector;

	static {
		XmlInspectorConfig xmlInspectorConfig = new XmlInspectorConfig();
		xmlInspectorConfig.setInputStream(GuiGeneratorService.class.getResourceAsStream("metawidget-metadata.xml"));
		xmlInspectorConfig.setRestrictAgainstObject(new JavaBeanPropertyStyle());
		xmlInspectorConfig.setValidateAgainstClasses(new JavaBeanPropertyStyle());

		DOM_INSPECTORS.add(new XmlInspector(xmlInspectorConfig));
		DOM_INSPECTORS.add(new PropertyTypeInspector());
		updateCompositeInspector();
	}

	// Use OGSi to register inspectors to provide GUI metadata
	// This is a workaround to get metadata from annotations and beans in org.eclipse.scanning.api, when we are not
	// allowed a dependency in either direction between the richbeans and scanning projects. Somewhere else in the GDA
	// codebase we will need a metawidget inspector to register as an OSGi service and inspect the scanning beans.
	public static synchronized void addDomInspector(DomInspector<?> inspector) {
		DOM_INSPECTORS.add(inspector);
		updateCompositeInspector();
	}

	public static synchronized void removeDomInspector(DomInspector<?> inspector) {
		DOM_INSPECTORS.remove(inspector);
		updateCompositeInspector();
	}

	private static void updateCompositeInspector() {
		inspector = new CompositeInspector(new CompositeInspectorConfig().setInspectors(
				DOM_INSPECTORS.toArray(EMPTY_INSPECTOR_ARRAY)));
	}

	@Override
	public <T> T generateGui(Object bean, T parent) {

		if (!(parent instanceof Composite)) throw new RuntimeException(getClass().getSimpleName()+" cannot deal with UI components other than Composite!");

		// Create a Metawidget
		SwtMetawidget metawidget = new SwtMetawidget((Composite)parent, SWT.NONE);

		// Metawidget builds GUIs in five stages
		// 1. Inspector
		metawidget.setInspector(inspector);

		// 2. InspectionResultProcessors
		// none (would probably like the JEXL processor here in the long run but can't build with it included at the
		// moment)

		// 3. WidgetBuilder
		metawidget.setWidgetBuilder(COMPOSITE_WIDGET_BUILDER);

		// 4. WidgetProcessors
		metawidget.addWidgetProcessor(COMBO_LABEL_PROCESSOR);
		metawidget.addWidgetProcessor(TOOLTIP_PROCESSOR);
		metawidget.addWidgetProcessor(DECORATOR_PROCESSOR);
		metawidget.addWidgetProcessor(BINDING_PROCESSOR);

		// Reflection binding processor (for actions) is present by default

		// 5. Layout
		// (default)

		// Finish
		metawidget.setToInspect(bean);
		metawidget.pack();

		return (T)metawidget;
	}

	@Override
	public <T> void openDialog(Object bean, T parent, String title) {
		if (parent!=null && !(parent instanceof Shell)) throw new RuntimeException(getClass().getSimpleName()+" cannot deal with UI components other than Shell!");
		new ObjectEditorDialog(bean, (Shell)parent, title, this).open();
	}
}
