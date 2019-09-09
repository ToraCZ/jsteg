package jsteg;

public class Help {
	final String id;
	final String topic;
	final String text;
	
	public Help(String id, String topic, String text) {
		super();
		this.id = id;
		this.topic = topic;
		this.text = text;
	}

	@Override
	public String toString() {
		return this.topic;
	}
}
