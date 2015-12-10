package uk.ac.gda.eventbus.api.message;

public class ErrorMessage extends GDAMessage {

	private static final long serialVersionUID = 5543479297191424861L;

	public ErrorMessage() {
		super(GDAMessageCategory.ERROR);
	}

	public ErrorMessage(Object sourceToken, String message) {
		super(sourceToken, GDAMessageCategory.ERROR, message);
	}

	public ErrorMessage(Object sourceToken) {
		super(sourceToken, GDAMessageCategory.ERROR);
	}

	public ErrorMessage(String message) {
		super(null, GDAMessageCategory.ERROR, message);
	}

}
