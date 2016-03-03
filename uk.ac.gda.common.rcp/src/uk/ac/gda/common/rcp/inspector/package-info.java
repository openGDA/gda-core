/**
 * This package exists to provide a workaround for GUI auto-generation based on metadata from annotations defined and
 * used in org.eclipse.scanning.api. A workaround is needed because the scanning and richbeans projects are not allowed
 * to depend on each other, yet somehow the GUI generator (in the richbeans project) needs to obtain metadata about
 * objects defined in the scanning project. Therefore we need a Metawidget inspector somewhere else in the codebase
 * where we can have dependencies on both richbeans and scanning and so link the two together.
 * <p>
 * The actual linking is done by registering the copy of the MetawidgetAnnotationInspector in this package as an OSGi
 * service, which will then be used automatically for object inspection by the GUI generator. Unfortunately this
 * requires several internal Metawidget packages to be exported from the GUI generator bundle but it seems very hard to
 * avoid some kind of ugliness like that.
 *
 * @author Colin Palmer
 *
 */
package uk.ac.gda.common.rcp.inspector;