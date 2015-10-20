
package cz.incad.vdkcommon;

import cz.incad.vdkcommon.xml.XMLReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author alberto
 */
public class Bohemika {
    
    static final Logger log = Logger.getLogger(Bohemika.class.getName());

    
    public static boolean isBohemika(String xml){
        try {
            XMLReader xmlReader = new XMLReader();
            xmlReader.loadXml(xml);
            String df008 = xmlReader.getNodeText("/oai:record/oai:metadata/marc:record/marc:controlfield[@tag='008']/text()");
            
            if (df008.length()>17 && df008.substring(15, 17).trim().equalsIgnoreCase("xr")) {
                return true;
            } else if (df008.length()>37 && df008.substring(35, 38).trim().equalsIgnoreCase("cze")) {
                return true;
            } else {
                String df044a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='044']/marc:subfield[@code='a']/text()");
                if (df044a.trim().equalsIgnoreCase("xr")) {
                    return true;
                } else {
                    String df041a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='041']/marc:subfield[@code='a']/text()");
                    if (df041a.trim().equalsIgnoreCase("cze")) {
                        return true;
                    } else {
                        String df041h = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='041']/marc:subfield[@code='h']/text()");
                        if (df041h.trim().equalsIgnoreCase("cze")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Bohemika.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Bohemika.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Bohemika.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(Bohemika.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
}
