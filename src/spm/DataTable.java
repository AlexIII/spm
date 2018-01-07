/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.util.stream.Collectors;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.security.InvalidKeyException;
import org.w3c.dom.Node;

/**
 *
 * @author Alex
 */
public class DataTable extends ArrayList<Entry> {
    static private final String rootTag = "smpdb";
    static private final String entryTag = "entry";
    static private final String passTestTag = "passtest";
    private Element root;
    private String filterStr = "";
    private List<Entry> filtred = null;
    private Document doc = null;
    private String fname = null;
    private String passTest = null;
    private NodeList entries;
    
    public DataTable(final String fname) throws ParserConfigurationException, SAXException, IOException, InvalidKeyException {
        this.fname = fname;
        load();
    }
    
    //public methods
    static public boolean createDB(final String fname, byte[] key) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            //add root
            Element root = doc.createElement(rootTag);
            doc.appendChild(root);

            //add <passtest> element in xml
            Element passTest = doc.createElement(passTestTag);
            passTest.setTextContent(Crypto.encryptPassword(key));
            root.appendChild(passTest);

            //save
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
        //check old password
        if(!checkMasterPass(oldKey)) return false;
        
        //change <passtest> element in xml
        passTest = Crypto.encryptPassword(newKey);
        ((Element)root.getElementsByTagName(passTestTag).item(0))
            .setTextContent(passTest);

        //reencode entries
        NodeList eList = root.getElementsByTagName(entryTag);
        for (int i = 0; i < eList.getLength(); i++) {
            Node old = eList.item(i);
            old.getParentNode().replaceChild(new Entry(old).toElement(doc, entryTag, newKey), old);
        }
        Entry.key = newKey;
        
        //save xml
        save();
        reload();
        return true;
    }
    public void replaceEntry(final int i, final Entry e) {
        NodeList eList = root.getElementsByTagName(entryTag);
        root.replaceChild(e.toElement(doc, entryTag), eList.item(i));
        save();
        reload();
    }
    public void removeEntry(final int i) {
        NodeList eList = root.getElementsByTagName(entryTag);
        root.removeChild(eList.item(i));
        save();
        reload();
    }
    public void addEntry(final Entry e) {
        root.appendChild(e.toElement(doc, entryTag));
        save();
        reload();
    }
    
    //gui-specific
    public void setFilter(final String s) {
        filterStr = s;
    }
    public int getAbsId(final int i) {
        return filtred.get(i).id;
    }
    public Object[][] toTableArray() {
        filtred = filter(filterStr);
        Object[][] res = new Object[filtred.size()][];
        for(int i = 0; i < filtred.size(); ++i)
          res[i] = filtred.get(i).toArray();
        return res;
    }    
    
    //private methods
    private void save() {
        save(doc, fname);
        entries = root.getElementsByTagName(entryTag);
    }
    
    private static void save(Document doc, String fname) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(new File(fname)));
        } catch (TransformerException ex) {
            throw new RuntimeException("Cant write to file");
        }
    }
    
    private void load() throws InvalidKeyException, ParserConfigurationException, SAXException, IOException {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fname);
        org.w3c.dom.EntityReference ref = doc.createEntityReference("name");
        root = (Element)doc.getElementsByTagName(rootTag).item(0);
        passTest = ((Element)root.getElementsByTagName(passTestTag).item(0)).getTextContent();
        if(!Crypto.checkPassword(Entry.key, passTest))
            throw new InvalidKeyException("Incorrect password");
        reload();      
    }
    
    private void reload() {
        clear();
        NodeList eList = root.getElementsByTagName(entryTag);
        for(int i = 0; i < eList.getLength(); ++i)
            add(new Entry((Element)eList.item(i), i)); 
    }
    
    private List<Entry> filter(final String s) {
        return s.length() == 0?
            new ArrayList<>(this) :
            stream().filter(p -> p.like(s)).collect(Collectors.toList());
    }
}
