package spm;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * DataTable class for managing XML data entries.
 * Extends ArrayList<Entry>.
 */
public class DataTable extends ArrayList<Entry> {
    private static final String ROOT_TAG = "smpdb";
    private static final String ENTRY_TAG = "entry";
    private static final String PASS_TEST_TAG = "passtest";
    
    private Element rootElement;
    private String filterString = "";
    private List<Entry> filteredEntries = null;
    private Document document = null;
    private String fileName = null;
    private String encryptedPassTest = null;
    private NodeList entryNodes;

    public DataTable(final String fileName) throws ParserConfigurationException, SAXException, IOException, InvalidKeyException {
        this.fileName = fileName;
        loadDocument();
    }

    /**
     * Creates a new database file with the given name and encryption key.
     * @param fileName The name of the file to create.
     * @param key The encryption key.
     * @return True if the database was created successfully, false otherwise.
     */
    public static boolean createDatabase(final String fileName, byte[] key) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement(ROOT_TAG);
            doc.appendChild(root);

            Element passTest = doc.createElement(PASS_TEST_TAG);
            passTest.setTextContent(Crypto.encryptPassword(key));
            root.appendChild(passTest);

            new File(fileName).createNewFile();
            saveDocument(doc, fileName);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the provided key matches the master password.
     * @param key The encryption key to check.
     * @return True if the key matches, false otherwise.
     */
    public boolean verifyMasterPassword(byte[] key) {
        return Crypto.checkPassword(key, encryptedPassTest);
    }

    /**
     * Changes the master password from the old key to the new key.
     * @param oldKey The current encryption key.
     * @param newKey The new encryption key.
     * @return True if the password was changed successfully, false otherwise.
     */
    public boolean updateMasterPassword(byte[] oldKey, byte[] newKey) {
        if (!verifyMasterPassword(oldKey)) return false;

        encryptedPassTest = Crypto.encryptPassword(newKey);
        ((Element) rootElement.getElementsByTagName(PASS_TEST_TAG).item(0)).setTextContent(encryptedPassTest);

        NodeList entryList = rootElement.getElementsByTagName(ENTRY_TAG);
        for (int i = 0; i < entryList.getLength(); i++) {
            Node oldNode = entryList.item(i);
            oldNode.getParentNode().replaceChild(new Entry(oldNode).toXmlElement(document, ENTRY_TAG, newKey), oldNode);
        }
        Entry.encryptionKey = newKey;

        saveChanges();
        reloadEntries();
        return true;
    }

    /**
     * Replaces an entry at the specified index with a new entry.
     * @param index The index of the entry to replace.
     * @param entry The new entry.
     */
    public void updateEntry(final int index, final Entry entry) {
        NodeList entryList = rootElement.getElementsByTagName(ENTRY_TAG);
        rootElement.replaceChild(entry.toXmlElement(document, ENTRY_TAG), entryList.item(index));
        saveChanges();
        reloadEntries();
    }

    /**
     * Removes an entry at the specified index.
     * @param index The index of the entry to remove.
     */
    public void deleteEntry(final int index) {
        NodeList entryList = rootElement.getElementsByTagName(ENTRY_TAG);
        rootElement.removeChild(entryList.item(index));
        saveChanges();
        reloadEntries();
    }

    /**
     * Adds a new entry to the DataTable.
     * @param entry The entry to add.
     */
    public void insertEntry(final Entry entry) {
        rootElement.appendChild(entry.toXmlElement(document, ENTRY_TAG));
        saveChanges();
        reloadEntries();
    }

    /**
     * Sets the filter string for filtering entries.
     * @param filter The filter string.
     */
    public void setFilterString(final String filter) {
        filterString = filter;
    }

    /**
     * Gets the absolute ID of the filtered entry at the specified index.
     * @param index The index of the filtered entry.
     * @return The absolute ID of the entry.
     */
    public int getAbsoluteId(final int index) {
        return filteredEntries.get(index).id;
    }

    /**
     * Converts the filtered entries to a 2D array for table representation.
     * @return A 2D array representing the filtered entries.
     */
    public Object[][] toTableArray() {
        filteredEntries = filterEntries(filterString);
        Object[][] result = new Object[filteredEntries.size()][];
        for (int i = 0; i < filteredEntries.size(); ++i)
            result[i] = filteredEntries.get(i).toArray();
        return result;
    }

    // Private methods

    /**
     * Saves the current state of the document to the file.
     */
    private void saveChanges() {
        saveDocument(document, fileName);
        entryNodes = rootElement.getElementsByTagName(ENTRY_TAG);
    }

    /**
     * Saves the given document to the specified file.
     * @param doc The document to save.
     * @param fileName The name of the file.
     */
    private static void saveDocument(Document doc, String fileName) {
        String xsltContent =
            "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
            "  <xsl:output indent=\"yes\"/>\n" +
            "  <xsl:strip-space elements=\"*\"/>\n" +
            "  <xsl:template match=\"/\">\n" +
            "    <xsl:text>\n</xsl:text>\n" +
            "    <xsl:apply-templates/>\n" +
            "  </xsl:template>\n" +
            "  <xsl:template match=\"@*|node()\">\n" +
            "    <xsl:copy>\n" +
            "      <xsl:apply-templates select=\"@*|node()\"/>\n" +
            "    </xsl:copy>\n" +
            "  </xsl:template>\n" +
            "</xsl:stylesheet>\n";
        try {
            doc.getDocumentElement().normalize();
            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xsltContent)));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(new File(fileName)));
        } catch (TransformerException ex) {
            throw new RuntimeException("Cannot write to file", ex);
        }
    }

    /**
     * Loads the document from the file and initializes the DataTable.
     * @throws InvalidKeyException If the provided key is incorrect.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     * @throws SAXException If any parse errors occur.
     * @throws IOException If any IO errors occur.
     */
    private void loadDocument() throws InvalidKeyException, ParserConfigurationException, SAXException, IOException {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileName);
        rootElement = (Element) document.getElementsByTagName(ROOT_TAG).item(0);
        encryptedPassTest = ((Element) rootElement.getElementsByTagName(PASS_TEST_TAG).item(0)).getTextContent();
        if (!Crypto.checkPassword(Entry.encryptionKey, encryptedPassTest))
            throw new InvalidKeyException("Incorrect password");
        reloadEntries();
    }

    /**
     * Reloads the entries from the document.
     */
    private void reloadEntries() {
        clear();
        NodeList entryList = rootElement.getElementsByTagName(ENTRY_TAG);
        for (int i = 0; i < entryList.getLength(); ++i)
            add(new Entry((Element) entryList.item(i), i));
    }

    /**
     * Filters the entries based on the filter string.
     * @param filter The filter string.
     * @return A list of filtered entries.
     */
    private List<Entry> filterEntries(final String filter) {
        return filter.isEmpty() ? new ArrayList<>(this) : stream().filter(p -> p.contains(filter)).collect(Collectors.toList());
    }
}
