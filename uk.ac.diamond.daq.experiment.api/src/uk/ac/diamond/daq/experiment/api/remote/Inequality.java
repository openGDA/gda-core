package uk.ac.diamond.daq.experiment.api.remote;

/**
 * This type specifies the comparison that should be applied
 * between a signal provider and a reference target
 */
public enum Inequality {

	LESS_THAN("<"),
	LESS_THAN_OR_EQUAL_TO("<="),
	EQUAL_TO("="),
	GREATER_THAN_OR_EQUAL_TO(">="),
	GREATER_THAN(">");

	private String symbol;

	private Inequality(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}
}