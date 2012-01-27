/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.nexus.nxclassio;

import gda.data.nexus.extractor.NexusExtractorException;

import org.nexusformat.NexusException;


/**
 * Class to represent an NXinstrument entry in a NexusFile
 *
 */
public class NXInstrument {
	final String name;
	final NXSource source; 
	final NXAperture aperture;
	/**
	 * class name for an NXinstrument in a Nexus file
	 */
	public static String NXClassName="NXinstrument";
	
	static NXInstrument getFromNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.setDataSetName("");
		String name = nfh.getNameForClass(myPath, NXClassName);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("name");
		name = nfh.getString(myPath);
		NXSource source = NXSource.getFromNexus( nfh,  myPath);
//		NXAperture aperture = NXAperture.getFromNexus( nfh, nexusPath);
		return new NXInstrument( name,source, null );
	}
	void addToNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("");
		nfh.openData(myPath, true);
		source.addToNexus(nfh, myPath);
		aperture.addToNexus(nfh, myPath);
	}
	/**
	 * @return @see NXClassName
	 */
	public String getNXClassName() {
		return NXClassName;
	}
	NXInstrument(String name,NXSource source, NXAperture aperture){
		this.name = name;
		this.source = source;
		this.aperture = aperture;
	}
	/**
	 * @return Name of the NXinstrument
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "NXinstrument : '" + name + "'\n" + source.toString() + "\n" + ( aperture == null ? "no aperture " : aperture.toString());
	}

}

class NXSource{
	public static String NXClassName="NXsource";
	final String name;
	final boolean top_up;
	static NXSource getFromNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.setDataSetName("");
		String name = nfh.getNameForClass(nexusPath, NXClassName);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("name");
		name = nfh.getString(myPath);
		return new NXSource(name,false);
	}	
	void addToNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("");
		nfh.openData(myPath, true);
	}

	NXSource(String name, boolean top_up){
		this.name = name;
		this.top_up = top_up;
	}

	@Override
	public String toString() {
		return "NXsource : '" + name + "'\n" + (top_up ? "top_up is ON" : "top_up is OFF ");
	}
	
}
class NXAperture{
	public static String NXClassName="NXaperture";
	String name;
	NXGeometry geometry;
	public NXAperture( String name, NXGeometry geometry){
		this.name = name;
		this.geometry = geometry;
	}
	@Override
	public String toString() {
		return "NXaperture : " + name + "\n" + geometry.toString();
	}

	void addToNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("");
		nfh.openData(myPath, true);
		geometry.addToNexus(nfh, myPath);
	}

}
class NXGeometry{
	public static String NXClassName="NXgeometry";
	String name;
	NXShape shape;
	NXOrientation orientation;
	public NXGeometry(	String name, NXShape shape, NXOrientation orientation){
		this.name = name;
		this.shape = shape;
		this.orientation = orientation;
	}
	@Override
	public String toString() {
		return "NXGeometry : " + name + "\n" + shape.toString() +  "\n" + orientation.toString();
	}
	void addToNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("");
		nfh.openData(myPath, true);
		shape.addToNexus(nfh, myPath);
		orientation.addToNexus(nfh, myPath);
	}
}

class NXShape{
	public static String NXClassName="NXshape";
	String name;
	String shape;
	Double size;
	public NXShape(	String name, String shape, Double size){
		this.name = name;
		this.shape = shape;
		this.size = size;
	}
	@Override
	public String toString() {
		return "NXShape : " + name + " shape: " + shape.toString() +  " size : " + size.toString();
	}
	void addToNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("");
		nfh.openData(myPath, true);
		nfh.setString(myPath, "shape", shape);
		nfh.setDoubleData(myPath, "size", new int []{1}, new double [] { size});
	}
}

class NXOrientation{
	public static String NXClassName="NXorientation";
	String name;
	double [] value= new double [] { 1.0, 2.0};
	public NXOrientation(	String name, double [] value){
		this.name = name;
		this.value = value;
	}
	@Override
	public String toString() {
		return "NXOrientation : " + name + " value: " + value.toString();
	}
	void addToNexus(NexusFileHandle nfh, final NexusPath nexusPath) throws NexusException, NexusExtractorException{
		NexusPath myPath = NexusPath.getInstance(nexusPath);
		myPath.addGroupPath(new NexusGroup(name,NXClassName));
		myPath.setDataSetName("");
		nfh.openData(myPath, true);
		nfh.setDoubleData(myPath, "size", new int []{value.length}, value);
	}
}
	