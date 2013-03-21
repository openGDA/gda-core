package gda.epics.predicate;

import gda.observable.Predicate;

public class GreaterThanOrEqualTo implements Predicate<Integer> {

	private final int value;

	public GreaterThanOrEqualTo(int value) {
		this.value = value;
	}

	@Override
	public boolean apply(Integer object) {
		return (object >= value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
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
		GreaterThanOrEqualTo other = (GreaterThanOrEqualTo) obj;
		if (value != other.value)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "GreaterThanOrEqualTo(" + value + ")";
	}

}