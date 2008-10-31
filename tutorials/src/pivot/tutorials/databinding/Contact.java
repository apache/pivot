package pivot.tutorials.databinding;

public class Contact {
	private String id;
	private String name;
	private Address address;
	private String phoneNumber;
	private String emailAddress;
	private IMAccount imAccount;

	public Contact(String id, String name, Address address, String phoneNumber,
		String emailAddress, IMAccount imAccount) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
		this.imAccount = imAccount;
	}

	public String getID() {
		return id;
	}

	public String getId() {
		return getID();
	}

	public String getName() {
		return name;
	}

	public Address getAddress() {
		return address;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public IMAccount getIMAccount() {
		return imAccount;
	}

	public IMAccount getImAccount() {
		return getIMAccount();
	}
}
