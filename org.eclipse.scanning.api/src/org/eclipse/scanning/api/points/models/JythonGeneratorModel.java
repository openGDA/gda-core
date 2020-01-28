package org.eclipse.scanning.api.points.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;

/**
 * This class allows loading of an almost arbitrary Python function generator as a PointsModel.
 * See /org.eclipse.scanning.test/src/org/eclipse/scanning/test/points/JythonGeneratorExamples.py for example generators
 * In short, the jython Class to be loaded should extend GeneratorWrapper, wrapping a CompoundGenerator that contains
 * any number of function generators that extends Generator from ScanPointGenerator and provides prepare_arrays methods
 *  and a constructor which calls super with the arguments (axes, units, size, alternate)
 *
 * Models must provide the following jythonArguments:
 *  "axes" (also set through the helper methods setName(String singleAxis), setScannableNames(List<String> multipleAxes))
 *   - the name(s) of the scannable ax[is/es] to be scanned. Default is "stage_x"
 *  "units" (setUnits(List<String> units))
 *   - the units that the generator will act in in the scannable axis. Default is "mm"
 *   - Can be length 1 or length getScannableNames().size()
 *  "size" (setSize(int size))
 *   - the number of points in this model. Default is 0, which will generate no points.
 *  "continuous" (setContinuous(boolean continuous))
 *   - Whether the generator should generate midpoints between each point. Default is false.
 *   - If true, the function generator must be capable of producing points from an index array xrange(-0.5, size-0.5, 1)
 *   "alternate" (setAlternating(boolean alternating)) Default is false.
 *   - Whether the generator should, if wrapped in another generator by a CompoundGenerator, switch directions on each step
 *   of the wrapping generator.
 *   i.e. a grid
 *   	from top left -> top right -> bottom left -> bottom right
 *   should start its next cycle
 *   	bottom right -> bottom left -> ... isAlternating() == true
 *   or
 *   	top left -> top right -> ... isAlternating() == false
 *
 * As the functions must also be jython compatible, the use of xrange over range is not to be deprecated
 *
 * @author Joseph Ware
 *
 */
public class JythonGeneratorModel extends AbstractPointsModel {

	// Mandatory fields
	private static final String CONTINUOUS = "continuous";
	private static final String ALTERNATE = "alternate";
	private static final String AXES = "axes";
	private static final String UNITS = "units";
	private static final String SIZE = "size";

	@FieldDescriptor(label = "Module Name", hint = "The name of the module to load.\nUsually this is the same as the python file without an ending '.py'", fieldPosition = 0)
	private String moduleName;

	@FieldDescriptor(label = "Module Path", hint = "The file path to the module folder.", file = FileType.EXISTING_FOLDER, fieldPosition = 1)
	private String path;

	@FieldDescriptor(label = "Class Name", hint = "The name of the class implementing PPointGenerator.\nIt must wrap a class extending Generator.", fieldPosition = 2)
	private String className;

	private Map<String, Object> jythonArguments = new HashMap<>();

	public JythonGeneratorModel() {
		// Mandatory fields
		setName("stage_x");
		setUnits(super.getUnits());
		setSize(0);
		setAlternating(false);
		setContinuous(false);
	}

	public Map<String, Object> getJythonArguments() {
		return jythonArguments;
	}

	public void setJythonArguments(Map<String, Object> args) {
		Map<String, Object> oldMap = new HashMap<>(jythonArguments);
		pcs.firePropertyChange("jythonArguments", oldMap, args);
		this.jythonArguments = args;
	}

	public void addJythonArgument(String key, Object value) {
		if (jythonArguments == null) jythonArguments = new HashMap<>();
		Map<String, Object> oldMap = new HashMap<>(jythonArguments);
		jythonArguments.put(key, value);
		pcs.firePropertyChange("jythonArguments", oldMap, jythonArguments);
	}

	@Override
	public boolean isContinuous() {
		if (jythonArguments.containsKey(CONTINUOUS)) return (boolean) jythonArguments.get(CONTINUOUS);
		return false;
	}

	@Override
	public void setContinuous(boolean newValue) {
		addJythonArgument(CONTINUOUS, newValue);
	}

	@Override
	public List<String> getUnits(){
		if (jythonArguments.containsKey(UNITS)) return (List<String>) jythonArguments.get(UNITS);
		return super.getUnits();
	}

	@Override
	public void setUnits(List<String> units) {
		addJythonArgument(UNITS, units);
	}

	@Override
	public boolean isAlternating() {
		if (jythonArguments.containsKey(ALTERNATE)) return (boolean) jythonArguments.get(ALTERNATE);
		return false;
	}

	@Override
	public void setAlternating(boolean alternating) {
		addJythonArgument(ALTERNATE, alternating);
	}

	@Override
	public List<String> getScannableNames() {
		if (jythonArguments.containsKey(AXES)) return (List<String>) jythonArguments.get(AXES);
		return Collections.emptyList();
	}

	public void setScannableNames(List<String> names) {
		jythonArguments.put(AXES, names);
	}

	@Override
	public String getName() {
		if (getScannableNames().isEmpty()) return null;
		return getScannableNames().get(0);
	}

	@Override
	// Always use List<String> as could be on one axis or multiple axes. Only place we need to convert is getName()
	public void setName(String name) {
		addJythonArgument(AXES, Arrays.asList(name));
	}

	public int getSize() {
		if (jythonArguments.containsKey(SIZE)) return (int) jythonArguments.get(SIZE);
		return 0;
	}

	public void setSize(int size) {
		addJythonArgument(SIZE, size);
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((jythonArguments == null) ? 0 : jythonArguments.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		JythonGeneratorModel other = (JythonGeneratorModel) obj;
		if (!equals(className, other.className))
			return false;
		if (!equals(moduleName, other.moduleName))
			return false;
		if (!equals(path, other.path))
			return false;
		return equals(jythonArguments, other.jythonArguments);
	}

	private boolean equals(Object a, Object b) {
		if (a == null)
			return (b == null);
		return a.equals(b);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [path=" + path + ", moduleName=" + moduleName + ", className=" + className +
				", jythonArguments=" + jythonArguments+"]";
	}

}