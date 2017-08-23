package trzcina.maplas6.lokalizacja;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import trzcina.maplas6.pomoc.Stale;

public class PlikGPX {

    public String sciezka;
    public String nazwa;
    public int stan;
    public List<PunktNaMapie> punkty;
    public List<PunktWTrasie> trasa;
    public Boolean zaznaczony;

    public PlikGPX(String sciezka) {
        this.sciezka = sciezka;
        nazwa = new File(sciezka).getName().replace(".gpx", "");
        stan = Stale.PLIKNOWY;
        punkty = new ArrayList<>(100);
        trasa = new ArrayList<>(100);
        zaznaczony = false;
    }

    private String pobierzWartoscParametru(String tag, Element element) {
        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            Node node = nodeList.item(0);
            if (node != null) {
                return node.getNodeValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Document otworzPlik() throws Exception {
        InputStream strumien = new FileInputStream(sciezka);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document dokument = dBuilder.parse(strumien);
        Element element = dokument.getDocumentElement();
        element.normalize();
        return dokument;
    }

    private void szukajWPT(Document dokument) {
        NodeList lista = dokument.getElementsByTagName("wpt");
        for (int i = 0; i < lista.getLength(); i++) {
            Node node = lista.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                PunktNaMapie punktnamapie = new PunktNaMapie(Float.parseFloat(element.getAttribute("lon")), Float.parseFloat(element.getAttribute("lat")), pobierzWartoscParametru("nama", element), pobierzWartoscParametru("cmt", element));
                punkty.add(punktnamapie);
            }
        }
    }

    private void szukajTRK(Document dokument) {
        NodeList lista = dokument.getElementsByTagName("trkpt");
        for(int i = 0; i < lista.getLength(); i++) {
            Node node = lista.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                PunktWTrasie punktwtrasie = new PunktWTrasie(Float.parseFloat(element.getAttribute("lon")), Float.parseFloat(element.getAttribute("lat")));
                trasa.add(punktwtrasie);
            }
        }
    }

    public void parsuj() {
        stan = Stale.PLIKROBIE;
        try {
            Document dokument = otworzPlik();
            szukajWPT(dokument);
            szukajTRK(dokument);
            if((trasa.size() > 0) || (punkty.size() > 0)) {
                stan = Stale.PLIKGOTOWY;
            }
        } catch (Exception e) {
            e.printStackTrace();
            stan = Stale.PLIKBLAD;
        }
    }

}
