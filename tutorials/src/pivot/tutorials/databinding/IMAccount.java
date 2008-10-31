package pivot.tutorials.databinding;

public class IMAccount {
	private String id;
	private String type;

	public IMAccount(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public String getID() {
		return id;
	}

	public String getId() {
		return getID();
	}

	public String getType() {
		return type;
	}
}
