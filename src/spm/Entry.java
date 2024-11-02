package spm;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Entry {
    public final int id;
    public static byte[] encryptionKey = null;
    private String website;
    private String username;
    private String password;
    private String notes;
    private String timestamp;

    private static final String WEBSITE_TAG = "site";
    private static final String USERNAME_TAG = "login";
    private static final String PASSWORD_TAG = "pass";
    private static final String NOTES_TAG = "comment";
    private static final String TIMESTAMP_TAG = "date";

    /**
     * Constructor to create a new Entry.
     * @param website The website associated with the entry.
     * @param username The username for the entry.
     * @param password The password for the entry.
     * @param notes Additional comments or notes.
     */
    public Entry(final String website, final String username, final String password, final String notes) {
        this.id = 0;
        this.website = website;
        this.username = username;
        this.password = Crypto.encryptString(encryptionKey, password);
        this.notes = notes;
        updateTimestamp();
    }

    public Entry(Node node) {
        this((Element) node);
    }

    public Entry(Element node) {
        this(node, 0);
    }

    public Entry(Element node, int id) {
        this.id = id;
        this.website = Crypto.decryptString(encryptionKey, node.getElementsByTagName(WEBSITE_TAG).item(0).getTextContent());
        this.username = Crypto.decryptString(encryptionKey, node.getElementsByTagName(USERNAME_TAG).item(0).getTextContent());
        this.password = node.getElementsByTagName(PASSWORD_TAG).item(0).getTextContent();
        this.notes = Crypto.decryptString(encryptionKey, node.getElementsByTagName(NOTES_TAG).item(0).getTextContent());
        this.timestamp = node.getElementsByTagName(TIMESTAMP_TAG).item(0).getTextContent();
    }

    public String getWebsite() {
        return website;
    }

    public String getUsername() {
        return username;
    }

    public String getNotes() {
        return notes;
    }

    public String getTimestamp() {
        return timestamp;
    }

    private static void addXmlElement(Element parentElement, final String tagName, final String textContent) {
        Element newElement = parentElement.getOwnerDocument().createElement(tagName);
        newElement.setTextContent(textContent);
        parentElement.appendChild(newElement);
    }

    /**
     * Converts the Entry to an XML Element.
     * @param doc The XML Document.
     * @param elementName The name of the XML Element.
     * @return The XML Element representing the Entry.
     */
    public Element toXmlElement(Document doc, String elementName) {
        Element element = doc.createElement(elementName);
        addXmlElement(element, WEBSITE_TAG, Crypto.encryptString(encryptionKey, website));
        addXmlElement(element, USERNAME_TAG, Crypto.encryptString(encryptionKey, username));
        addXmlElement(element, PASSWORD_TAG, password);
        addXmlElement(element, NOTES_TAG, Crypto.encryptString(encryptionKey, notes));
        addXmlElement(element, TIMESTAMP_TAG, timestamp);
        return element;
    }

    /**
     * Converts the Entry to an XML Element with a new encryption key.
     * @param doc The XML Document.
     * @param elementName The name of the XML Element.
     * @param newKey The new encryption key.
     * @return The XML Element representing the Entry.
     */
    public Element toXmlElement(Document doc, String elementName, byte[] newKey) {
        Element element = doc.createElement(elementName);
        addXmlElement(element, WEBSITE_TAG, Crypto.encryptString(newKey, website));
        addXmlElement(element, USERNAME_TAG, Crypto.encryptString(newKey, username));
        addXmlElement(element, PASSWORD_TAG, Crypto.encryptString(newKey, Crypto.decryptString(encryptionKey, password)));
        addXmlElement(element, NOTES_TAG, Crypto.encryptString(newKey, notes));
        addXmlElement(element, TIMESTAMP_TAG, timestamp);
        return element;
    }

    /**
     * Checks if the entry contains the specified string in its website, username, or notes.
     * @param searchString The string to search for.
     * @return True if the string is found, otherwise false.
     */
    public boolean contains(final String searchString) {
        return website.contains(searchString) || username.contains(searchString) || notes.contains(searchString);
    }

    /**
     * Converts the Entry to an array of strings.
     * @return An array containing the website, username, notes, and timestamp.
     */
    public String[] toArray() {
        return new String[] { website, username, notes, timestamp };
    }

    /**
     * Returns a formatted name for the entry.
     * @return A string in the format "website (username)".
     */
    public String getFormattedName() {
        return website + " (" + username + ")";
    }

    /**
     * Gets the decrypted password.
     * @return The decrypted password.
     */
    public String getPassword() {
        return Crypto.decryptString(encryptionKey, password);
    }

    /**
     * Updates the timestamp to the current date and time.
     */
    private void updateTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        timestamp = dateFormat.format(new Date());
    }
}
