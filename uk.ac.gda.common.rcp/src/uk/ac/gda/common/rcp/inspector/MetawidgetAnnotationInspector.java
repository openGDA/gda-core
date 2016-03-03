// Metawidget
//
// This file is dual licensed under both the LGPL
// (http://www.gnu.org/licenses/lgpl-2.1.html) and the EPL
// (http://www.eclipse.org/org/documents/epl-v10.php). As a
// recipient of Metawidget, you may choose to receive it under either
// the LGPL or the EPL.
//
// Commercial licenses are also available. See http://metawidget.org
// for details.

package uk.ac.gda.common.rcp.inspector;

import static org.metawidget.inspector.InspectionResultConstants.COMES_AFTER;
import static org.metawidget.inspector.InspectionResultConstants.DONT_EXPAND;
import static org.metawidget.inspector.InspectionResultConstants.HIDDEN;
import static org.metawidget.inspector.InspectionResultConstants.LABEL;
import static org.metawidget.inspector.InspectionResultConstants.LARGE;
import static org.metawidget.inspector.InspectionResultConstants.LOOKUP;
import static org.metawidget.inspector.InspectionResultConstants.LOOKUP_LABELS;
import static org.metawidget.inspector.InspectionResultConstants.MASKED;
import static org.metawidget.inspector.InspectionResultConstants.NAME;
import static org.metawidget.inspector.InspectionResultConstants.READ_ONLY;
import static org.metawidget.inspector.InspectionResultConstants.REQUIRED;
import static org.metawidget.inspector.InspectionResultConstants.SECTION;
import static org.metawidget.inspector.InspectionResultConstants.TRUE;
import static org.metawidget.inspector.InspectionResultConstants.WIDE;

import java.util.Map;

import org.eclipse.scanning.api.annotation.UiAction;
import org.eclipse.scanning.api.annotation.UiAttribute;
import org.eclipse.scanning.api.annotation.UiAttributes;
import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiDontExpand;
import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.annotation.UiLabel;
import org.eclipse.scanning.api.annotation.UiLarge;
import org.eclipse.scanning.api.annotation.UiLookup;
import org.eclipse.scanning.api.annotation.UiMasked;
import org.eclipse.scanning.api.annotation.UiReadOnly;
import org.eclipse.scanning.api.annotation.UiRequired;
import org.eclipse.scanning.api.annotation.UiSection;
import org.eclipse.scanning.api.annotation.UiWide;
import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.BaseObjectInspectorConfig;
import org.metawidget.inspector.impl.Trait;
import org.metawidget.inspector.impl.actionstyle.Action;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.util.ArrayUtils;
import org.metawidget.util.CollectionUtils;

/**
 * Copied from org.metawidget.inspector.annotation with imports adjusted to inspect the copied Metawidget annotations
 * in org.eclipse.scanning.api.annotation rather than those in org.metawidget.inspector.annotation
 * <p>
 * Original metawidget java doc:
 * <p>
 * Inspects annotations defined by Metawidget (declared in this same package).
 * <p>
 * Note: the name of this class is longwinded for extra clarity. It is not just a
 * 'MetawidgetInspector', because of course there are lots of different Metawidget Inspectors.
 * Equally, it is not just an 'AnnotationInspector', because it doesn't generically scan all
 * possible annotations.
 *
 * @author <a href="http://kennardconsulting.com">Richard Kennard</a>
 */

public class MetawidgetAnnotationInspector
	extends BaseObjectInspector {

	//
	// Constructor
	//

	public MetawidgetAnnotationInspector() {

		this( new BaseObjectInspectorConfig() );
	}

	public MetawidgetAnnotationInspector( BaseObjectInspectorConfig config ) {

		super( config );
	}

	//
	// Protected methods
	//

	@Override
	protected Map<String, String> inspectTrait( Trait trait )
		throws Exception {

		Map<String, String> attributes = CollectionUtils.newHashMap();

		// UiHidden

		if ( trait.isAnnotationPresent( UiHidden.class ) ) {
			attributes.put( HIDDEN, TRUE );
		}

		// UiComesAfter

		UiComesAfter comesAfter = trait.getAnnotation( UiComesAfter.class );

		if ( comesAfter != null ) {
			attributes.put( COMES_AFTER, ArrayUtils.toString( comesAfter.value() ) );
		}

		// UiReadOnly

		UiReadOnly readOnly = trait.getAnnotation( UiReadOnly.class );

		if ( readOnly != null ) {
			attributes.put( READ_ONLY, TRUE );
		}

		// UiSection

		UiSection uiSection = trait.getAnnotation( UiSection.class );

		if ( uiSection != null ) {
			attributes.put( SECTION, ArrayUtils.toString( uiSection.value() ) );
		}

		// UiLabel

		UiLabel label = trait.getAnnotation( UiLabel.class );

		if ( label != null ) {
			attributes.put( LABEL, label.value() );
		}

		// UiAttribute

		UiAttribute uiAttribute = trait.getAnnotation( UiAttribute.class );

		if ( uiAttribute != null ) {
			putUiAttribute( uiAttribute, attributes );
		}

		// UiAttributes

		UiAttributes uiAttributes = trait.getAnnotation( UiAttributes.class );

		if ( uiAttributes != null ) {
			for ( UiAttribute nestedUiAttribute : uiAttributes.value() ) {
				putUiAttribute( nestedUiAttribute, attributes );
			}
		}

		return attributes;
	}

	@Override
	protected Map<String, String> inspectProperty( Property property )
		throws Exception {

		Map<String, String> attributes = CollectionUtils.newHashMap();

		// UiRequired

		if ( property.isAnnotationPresent( UiRequired.class ) ) {
			attributes.put( REQUIRED, TRUE );
		}

		// UiLookup

		UiLookup lookup = property.getAnnotation( UiLookup.class );

		if ( lookup != null ) {
			attributes.put( LOOKUP, ArrayUtils.toString( lookup.value() ) );

			// (note: values().length == labels().length() is not validated
			// here, as XmlInspector could bypass it anyway)

			if ( lookup.labels().length > 0 ) {
				attributes.put( LOOKUP_LABELS, ArrayUtils.toString( lookup.labels() ) );
			}
		}

		// UiMasked

		if ( property.isAnnotationPresent( UiMasked.class ) ) {
			attributes.put( MASKED, TRUE );
		}

		// UiLarge

		if ( property.isAnnotationPresent( UiLarge.class ) ) {
			attributes.put( LARGE, TRUE );
		}

		// UiWide

		if ( property.isAnnotationPresent( UiWide.class ) ) {
			attributes.put( WIDE, TRUE );
		}

		// UiDontExpand

		UiDontExpand dontExpand = property.getAnnotation( UiDontExpand.class );

		if ( dontExpand != null ) {
			attributes.put( DONT_EXPAND, TRUE );
		}

		return attributes;
	}

	@Override
	protected Map<String, String> inspectAction( Action action )
		throws Exception {

		Map<String, String> attributes = CollectionUtils.newHashMap();

		// UiAction (this is kind of a dummy match)

		if ( action.isAnnotationPresent( UiAction.class ) ) {
			attributes.put( NAME, action.getName() );
		}

		return attributes;
	}

	//
	// Private methods
	//

	private void putUiAttribute( UiAttribute uiAttribute, Map<String, String> attributes ) {

		for ( String name : uiAttribute.name() ) {
			attributes.put( name, uiAttribute.value() );
		}
	}
}
