package uk.ac.gda.beans.exafs;

import java.util.Arrays;
import java.util.Objects;

public class IonchamberOptimisationParams {

	public final String classType = IonchamberOptimisationParams.class.getCanonicalName();

	/** ID of the class to be used for Editor view */
	public final String editorClass = "uk.ac.gda.exafs.ui.IonchamberOptimisationParamsEditor";

	private boolean autoControl;
	private double[] energies;

	public boolean isAutoControl() {
		return autoControl;
	}
	public void setAutoControl(boolean autoControl) {
		this.autoControl = autoControl;
	}
	public double[] getEnergies() {
		return energies;
	}
	public void setEnergies(double[] energies) {
		this.energies = energies;
	}

	public String getClassType() {
		return classType;
	}

	public String getEditorClass() {
		return editorClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(energies);
		result = prime * result + Objects.hash(autoControl, classType, editorClass);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IonchamberOptimisationParams other = (IonchamberOptimisationParams) obj;
		return autoControl == other.autoControl && Objects.equals(classType, other.classType)
				&& Objects.equals(editorClass, other.editorClass) && Arrays.equals(energies, other.energies);
	}
}