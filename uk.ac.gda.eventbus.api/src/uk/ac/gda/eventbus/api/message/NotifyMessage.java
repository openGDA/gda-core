package uk.ac.gda.eventbus.api.message;

public class NotifyMessage extends GDAMessage {

	private static final long serialVersionUID = 7582388296446175980L;

	public NotifyMessage() {
		super(GDAMessageCategory.NOTIFY);
	}

	public NotifyMessage(Object sourceToken, String message) {
		super(sourceToken, GDAMessageCategory.NOTIFY, message);
	}

	public NotifyMessage(String message) {
		super(null, GDAMessageCategory.NOTIFY, message);
	}

}
