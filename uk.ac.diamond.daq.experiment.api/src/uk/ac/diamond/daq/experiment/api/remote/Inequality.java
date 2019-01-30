package uk.ac.diamond.daq.experiment.api.remote;

import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;

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

	public LimitCondition getLimitCondition(double argument) {
		switch (this) {
		case EQUAL_TO:
			return signal -> signal == argument;
		case GREATER_THAN:
			return signal -> signal > argument;
		case GREATER_THAN_OR_EQUAL_TO:
			return signal -> signal >= argument;
		case LESS_THAN:
			return signal -> signal < argument;
		case LESS_THAN_OR_EQUAL_TO:
			return signal -> signal <= argument;
		default:
			throw new IllegalStateException("Unknown inequality '" + this + '"');
		}
	}

	@Override
	public String toString() {
		return symbol;
	}
}