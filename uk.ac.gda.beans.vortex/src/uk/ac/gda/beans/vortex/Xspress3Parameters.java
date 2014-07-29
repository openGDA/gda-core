/**
 * 
 */
package uk.ac.gda.beans.vortex;

import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * @author dfq16044
 *
 */
public class Xspress3Parameters extends VortexParameters {
	/**
	 * 
	 */
	private static final long serialVersionUID = -962382978263704423L;

	/**
	 * 
	 */
	
	
	static public final java.net.URL mappingURL = Xspress3Parameters.class.getResource("Xspress3Mapping.xml");

	static public final java.net.URL schemaURL = Xspress3Parameters.class.getResource("Xspress3Mapping.xsd");
	
	
	
	public static Xspress3Parameters createFromXML(String filename) throws Exception {
		return (Xspress3Parameters) XMLHelpers.createFromXML(mappingURL, Xspress3Parameters.class, schemaURL, filename);
	}

	public static void writeToXML(Xspress3Parameters xspressParameters, String filename) throws Exception {
		XMLHelpers.writeToXML(mappingURL, xspressParameters, filename);
	}
	
	public Xspress3Parameters() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Xspress3Parameters objectCast (Object obj){ 
		return (Xspress3Parameters) obj;
	}

}
