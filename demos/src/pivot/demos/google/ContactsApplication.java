package pivot.demos.google;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.PostalAddress;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.adapter.ListAdapter;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Form;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.ListViewSelectionListener;
import pivot.wtk.Orientation;
import pivot.wtk.Sheet;
import pivot.wtk.SheetCloseListener;
import pivot.wtk.Window;
import pivot.wtk.effects.ShadeDecorator;
import pivot.wtk.media.Image;
import pivot.wtkx.WTKXSerializer;

public class ContactsApplication implements Application {
	private static class PresenceStatusMonitor implements RosterListener {
		private ImageView imAccountStatusImageView;

		public PresenceStatusMonitor(ImageView imAccountStatusImageView) {
			this.imAccountStatusImageView = imAccountStatusImageView;
		}

	    public void entriesAdded(java.util.Collection<String> addresses) {
	    	// No-op
	    }

	    public void entriesDeleted(java.util.Collection<String> addresses) {
	    	// No-op
	    }

	    public void entriesUpdated(java.util.Collection<String> addresses) {
	    	// No-op
	    }

	    public void presenceChanged(Presence presence) {
	        updateAccountStatus(presence, imAccountStatusImageView);
	    }
	};

	private ContactsService contactsService = null;
	private XMPPConnection xmppConnection = null;

	private Window window = null;
	private ListView contactListView = null;
	private Label nameLabel = null;
	Form.Section addressSection = null;
	Form.Section phoneNumberSection = null;
	Form.Section emailAddressSection = null;
	Form.Section imAccountSection = null;

	private ShadeDecorator windowDecorator = new ShadeDecorator(0.1f, Color.BLACK);

	private LoginSheet loginSheet = null;

	private ArrayList<PresenceStatusMonitor> presenceStatusMonitors =
		new ArrayList<PresenceStatusMonitor>();

	public static final String APPLICATION_NAME = "Pivot-ContactsExample-1";
	public static final String GOOGLE_TALK_PROTOCOL = "GOOGLE_TALK";

	private static final URL baseFeedURL;
	private static final HashMap<String, String> protocolLabels;
	private static final Image greenBullet;
	private static final Image redBullet;

	static {
		try {
			baseFeedURL = new URL("http://www.google.com/m8/feeds/contacts/");
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		}

		protocolLabels = new HashMap<String, String>();
		protocolLabels.put("AIM", "AIM");
		protocolLabels.put("GOOGLE_TALK", "Google");
		protocolLabels.put("ICQ", "ICQ");
		protocolLabels.put("JABBER", "Jabber");
		protocolLabels.put("MSN", "MSN");
		protocolLabels.put("YAHOO", "Yahoo");

		greenBullet = Image.load(ContactsApplication.class.getResource("bullet_green.png"));
		redBullet = Image.load(ContactsApplication.class.getResource("bullet_red.png"));
	}

	public void startup(Display display, Dictionary<String, String> properties)
		throws Exception {
		// Create the contacts service
		contactsService = new ContactsService(APPLICATION_NAME);

		// Create the XMPP connection
		ConnectionConfiguration connectionConfiguration =
			new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
		xmppConnection = new XMPPConnection(connectionConfiguration);

		// Load the main contacts UI
		WTKXSerializer wtkxSerializer = new WTKXSerializer();
		window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("contacts.wtkx")));
		contactListView = (ListView)wtkxSerializer.getObjectByName("contactListView");
		contactListView.getListViewSelectionListeners().add(new ListViewSelectionListener() {
			public void selectionChanged(ListView listView) {
				refreshContactDetail();
			}
		});

		nameLabel = (Label)wtkxSerializer.getObjectByName("nameLabel");
		addressSection = (Form.Section)wtkxSerializer.getObjectByName("addressSection");
		phoneNumberSection = (Form.Section)wtkxSerializer.getObjectByName("phoneNumberSection");
		emailAddressSection = (Form.Section)wtkxSerializer.getObjectByName("emailAddressSection");
		imAccountSection = (Form.Section)wtkxSerializer.getObjectByName("imAccountSection");

		// Open the window
		window.setTitle("Google Contacts");
		window.setMaximized(true);
		window.getDecorators().add(windowDecorator);
		window.open(display);

		// Open the login prompt
		loginSheet = new LoginSheet(contactsService, xmppConnection);
		loginSheet.open(window, new SheetCloseListener() {
			public void sheetClosed(Sheet sheet) {
				if (sheet.getResult()) {
					window.getDecorators().remove(windowDecorator);

					try {
						loadContacts();
					} catch (Exception exception) {
						Alert.alert(exception.getMessage(), window);
					}
				} else {
					DesktopApplicationContext.exit();
				}
			}
		});
	}

	public boolean shutdown(boolean optional) {
		xmppConnection.disconnect();
		window.close();
		return true;
	}

	public void suspend() {
		// No-op
	}

	public void resume() {
		// No-op
	}

	private void loadContacts() throws Exception {
		String username = loginSheet.getUsername();

		URL feedUrl = new URL(baseFeedURL, username + "@gmail.com/full");
		ContactFeed contactFeed = contactsService.getFeed(feedUrl, ContactFeed.class);

		java.util.List<ContactEntry> entries = contactFeed.getEntries();
		Collections.sort(entries, new Comparator<ContactEntry>() {
			public int compare(ContactEntry ce1, ContactEntry ce2) {
				String title1 = ce1.getTitle().getPlainText();
				String title2 = ce2.getTitle().getPlainText();
				return title1.compareTo(title2);
			}
		});

		ListAdapter<ContactEntry> contacts = new ListAdapter<ContactEntry>(entries);
		contactListView.setListData(contacts);

		if (contacts.getLength() > 0) {
			contactListView.setSelectedIndex(0);
			contactListView.requestFocus();
		}
	}

	private void refreshContactDetail() {
		// Clear out existing data
		nameLabel.setText(null);
		addressSection.remove(0, addressSection.getLength());
		phoneNumberSection.remove(0, phoneNumberSection.getLength());
		emailAddressSection.remove(0, emailAddressSection.getLength());
		imAccountSection.remove(0, imAccountSection.getLength());

		// Stop listening for presence changes
		Roster roster = xmppConnection.getRoster();
		for (PresenceStatusMonitor monitor : presenceStatusMonitors) {
			roster.removeRosterListener(monitor);
		}

		presenceStatusMonitors.clear();

		ContactEntry contactEntry = (ContactEntry)contactListView.getSelectedItem();
		if (contactEntry != null) {
			nameLabel.setText(contactEntry.getTitle().getPlainText());

			for (PostalAddress postalAddress : contactEntry.getPostalAddresses()) {
				String value = postalAddress.getValue();

				FlowPane addressFlowPane = new FlowPane(Orientation.VERTICAL);
				addressSection.add(addressFlowPane);

				String[] lines = value.split("\n");
				for (int i = 0, n = lines.length; i < n; i++) {
					addressFlowPane.add(new Label(lines[i]));
				}

				String rel = postalAddress.getRel();
				if (rel != null) {
					Form.setName(addressFlowPane, getFormName(rel));
				}
			}

			for (PhoneNumber phoneNumber : contactEntry.getPhoneNumbers()) {
				String value = phoneNumber.getPhoneNumber();
				Label phoneNumberLabel = new Label(value);

				phoneNumberSection.add(phoneNumberLabel);

				String rel = phoneNumber.getRel();
				if (rel != null) {
					Form.setName(phoneNumberLabel, getFormName(rel));
				}
			}

			for (Email email : contactEntry.getEmailAddresses()) {
				String value = email.getAddress();
				Label emailAddressLabel = new Label(value);

				emailAddressSection.add(emailAddressLabel);

				String rel = email.getRel();
				if (rel != null) {
					Form.setName(emailAddressLabel, getFormName(rel));
				}
			}

			for (Im im : contactEntry.getImAddresses()) {
				String value = im.getAddress();

				FlowPane imAccountFlowPane = new FlowPane();
				imAccountFlowPane.getStyles().put("spacing", 0);

				Label imAccountLabel = new Label(value);
				imAccountFlowPane.add(imAccountLabel);

				ImageView imAccountStatusImageView = new ImageView();
				imAccountFlowPane.add(imAccountStatusImageView);

				imAccountSection.add(imAccountFlowPane);

				String protocol = im.getProtocol();
				if (protocol != null) {
					protocol = getFormName(protocol);
					Form.setName(imAccountFlowPane, protocolLabels.get(protocol));

					if (protocol.equals(GOOGLE_TALK_PROTOCOL)) {
						Presence presence = roster.getPresence(value);
						updateAccountStatus(presence, imAccountStatusImageView);

						PresenceStatusMonitor monitor = new PresenceStatusMonitor(imAccountStatusImageView);
						roster.addRosterListener(monitor);
						presenceStatusMonitors.add(monitor);
					}
				}
			}
		}
	}

	private static String getFormName(String rel) {
		String formName = rel.substring(rel.indexOf("#") + 1);
		formName = Character.toUpperCase(formName.charAt(0)) + formName.substring(1);

		return formName;
	}

	private static void updateAccountStatus(Presence presence, ImageView imAccountStatusImageView) {
		if (presence == null) {
			imAccountStatusImageView.setImage((Image)null);
		} else {
			boolean available = presence.isAvailable();
			imAccountStatusImageView.setImage(available ? greenBullet : redBullet);
			imAccountStatusImageView.setTooltipText(presence.getStatus());
		}
	}
}
