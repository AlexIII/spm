package spm;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Entry {
    public final int id;
    public static byte[] key = null;
    private String site;
    private String login;
    private String pass;
    private String comment;
    private String date;

    public Entry(final String site, final String login, final String pass, final String comment) {
        this.id = 0;
        this.site = site;
        this.login = login;
        this.pass = Crypto.encryptString(key, pass);
        this.comment = comment;
        updateDate();
    }

    public Entry(Node node) {
        this((Element) node);
    }

    public Entry(Element node) {
        this(node, 0);
    }

    public Entry(Element node, int id) {
        this.id = id;
        this.site = Crypto.decryptString(key, node.getElementsByTagName("site").item(0).getTextContent());
        this.login = Crypto.decryptString(key, node.getElementsByTagName("login").item(0).getTextContent());
        this.pass = node.getElementsByTagName("pass").item(0).getTextContent();
        this.comment = Crypto.decryptString(key, node.getElementsByTagName("comment").item(0).getTextContent());
        this.date = node.getElementsByTagName("date").item(0).getTextContent();
    }

    public String getSite() {
        return site;
    }

    public String getLogin() {
        return login;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    private static void addElement(Element el, final String tag, final String text) {
        Element tmp = el.getOwnerDocument().createElement(tag);
        tmp.setTextContent(text);
        el.appendChild(tmp);
    }

    public Element toElement(Document doc, String name) {
        Element el = doc.createElement(name);
        addElement(el, "site", Crypto.encryptString(key, site));
        addElement(el, "login", Crypto.encryptString(key, login));
        addElement(el, "pass", pass);
        addElement(el, "comment", Crypto.encryptString(key, comment));
        addElement(el, "date", date);
        return el;
    }

    public Element toElement(Document doc, String name, byte[] newKey) {
        Element el = doc.createElement(name);
        addElement(el, "site", Crypto.encryptString(newKey, site));
        addElement(el, "login", Crypto.encryptString(newKey, login));
        addElement(el, "pass", Crypto.encryptString(newKey, Crypto.decryptString(key, pass)));
        addElement(el, "comment", Crypto.encryptString(newKey, comment));
        addElement(el, "date", date);
        return el;
    }

    public boolean like(final String str) {
        return site.contains(str) || login.contains(str) || comment.contains(str);
    }

    public String[] toArray() {
        return new String[] { site, login, comment, date };
    }

    public String name() {
        return site + " (" + login + ")";
    }

    public String getPassword() {
        return Crypto.decryptString(key, pass);
    }

    private void updateDate() {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        date = fmt.format(new Date());
    }
}
