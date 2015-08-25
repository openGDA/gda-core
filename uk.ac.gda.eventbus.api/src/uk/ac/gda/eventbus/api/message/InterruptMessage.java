package uk.ac.gda.eventbus.api.message;

public class InterruptMessage extends GDAMessage {

	private static final long serialVersionUID = 2837209124922874108L;

	private int priority = Integer.MAX_VALUE;

	public InterruptMessage(Object sourceToken, String message) {
		super(sourceToken,GDAMessageCategory.INTERRUPT,message);
	}

	public int getPriority() {
		return priority;
	}

}
