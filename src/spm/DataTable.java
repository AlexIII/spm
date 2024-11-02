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
    
    private Element root;
    private String filterStr = "";
    private List<Entry> filtered = null;
    private Document doc = null;
    private String fname = null;
    private String passTest = null;
    private NodeList entries;

    public DataTable(final String fname) throws ParserConfigurationException, SAXException, IOException, InvalidKeyException {
        this.fname = fname;
        load();
    }

    // Public methods
    public static boolean createDB(final String fname, byte[] key) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement(ROOT_TAG);
            doc.appendChild(root);

            Element passTest = doc.createElement(PASS_TEST_TAG);
            passTest.setTextContent(Crypto.encryptPassword(key));
            root.appendChild(passTest);

            new File(fname).createNewFile();
            save(doc, fname);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean checkMasterPass(byte[] key) {
        return Crypto.checkPassword(key, passTest);
    }

    public boolean changeMasterPass(byte[] oldKey, byte[] newKey) {
        if (!checkMasterPass(oldKey)) return false;

        passTest = Crypto.encryptPassword(newKey);
        ((Element) root.getElementsByTagName(PASS_TEST_TAG).item(0)).setTextContent(passTest);

        NodeList eList = root.getElementsByTagName(ENTRY_TAG);
        for (int i = 0; i < eList.getLength(); i++) {
            Node old = eList.item(i);
            old.getParentNode().replaceChild(new Entry(old).toElement(doc, ENTRY_TAG, newKey), old);
        }
        Entry.key = newKey;

        save();
        reload();
        return true;
    }

    public void replaceEntry(final int i, final Entry e) {
        NodeList eList = root.getElementsByTagName(ENTRY_TAG);
        root.replaceChild(e.toElement(doc, ENTRY_TAG), eList.item(i));
        save();
        reload();
    }

    public void removeEntry(final int i) {
        NodeList eList = root.getElementsByTagName(ENTRY_TAG);
        root.removeChild(eList.item(i));
        save();
        reload();
    }

    public void addEntry(final Entry e) {
        root.appendChild(e.toElement(doc, ENTRY_TAG));
        save();
        reload();
    }

    // GUI-specific methods
    public void setFilter(final String s) {
        filterStr = s;
    }

    public int getAbsId(final int i) {
        return filtered.get(i).id;
    }

    public Object[][] toTableArray() {
        filtered = filter(filterStr);
        Object[][] res = new Object[filtered.size()][];
        for (int i = 0; i < filtered.size(); ++i)
            res[i] = filtered.get(i).toArray();
        return res;
    }

    // Private methods
    private void save() {
        save(doc, fname);
        entries = root.getElementsByTagName(ENTRY_TAG);
    }

    private static void save(Document doc, String fname) {
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
            transformer.transform(new DOMSource(doc), new StreamResult(new File(fname)));
        } catch (TransformerException ex) {
            throw new RuntimeException("Cannot write to file", ex);
        }
    }

    private void load() throws InvalidKeyException, ParserConfigurationException, SAXException, IOException {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fname);
        root = (Element) doc.getElementsByTagName(ROOT_TAG).item(0);
        passTest = ((Element) root.getElementsByTagName(PASS_TEST_TAG).item(0)).getTextContent();
        if (!Crypto.checkPassword(Entry.key, passTest))
            throw new InvalidKeyException("Incorrect password");
        reload();
    }

    private void reload() {
        clear();
        NodeList eList = root.getElementsByTagName(ENTRY_TAG);
        for (int i = 0; i < eList.getLength(); ++i)
            add(new Entry((Element) eList.item(i), i));
    }

    private List<Entry> filter(final String s) {
        return s.isEmpty() ? new ArrayList<>(this) : stream().filter(p -> p.like(s)).collect(Collectors.toList());
    }
}
