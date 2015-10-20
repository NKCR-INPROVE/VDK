/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.xsl;

import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author Administrator
 */
public class XmlUnescapeService {
    public static String unescape(String s) {
        String res = StringEscapeUtils.unescapeXml(s)
                .replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "")
                .replace("<?xml version=\"1.0\"?>", "")
                .replace("&", "&amp;").trim();
        //System.out.println(res);
        return res;
    }

}
