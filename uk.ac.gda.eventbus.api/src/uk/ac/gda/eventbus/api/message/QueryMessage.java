package uk.ac.gda.eventbus.api.message;

import java.io.Serializable;

public class QueryMessage extends GDAMessage {

	private static final long serialVersionUID = 7892308524972085507L;

	public QueryMessage() {
		super(GDAMessageCategory.QUERY);
	}

	public QueryMessage(Serializable sourceToken, String message) {
		super(sourceToken, GDAMessageCategory.QUERY, message);
	}

	public QueryMessage(Serializable sourceToken) {
		super(sourceToken, GDAMessageCategory.QUERY);
	}

	public QueryMessage(String message) {
		super(null, GDAMessageCategory.QUERY, message);
	}

}
