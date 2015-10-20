package cz.incad.vdkcommon;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.NodeList;

/**
 *
 * @author alberto
 */
public class NKF {

    static final Logger logger = Logger.getLogger(NKF.class.getName());

    static String[] signs = new String[]{"I", "II", "III", "IV", "V", "VIII", "IX",
        "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII",
        "P I", "P II", "P III", "P IV", "A", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10", "A11", "A12", "A13", "A14", "A15 a-f",
        "A 16", "B", "C", "D", "Diss D", "Diss T", "E", "F", "G", "GDA", "H", "HH", "CH", "MGA", "60 A - F", "71 C - F", "72", "73 A - K"
    };

    public static boolean isNKF(String signatura) {
        try {
            logger.log(Level.FINE, "signatura: {0}", signatura);
            String[] parts = signatura.split(" ");
            String first = parts[0];
            logger.log(Level.FINE, "first: {0}", first);
            List<String> s2 = Arrays.asList(signs);
            if (s2.contains(first)) {
                return true;
            }
            logger.log(Level.FINE, "parts.length: {0}", parts.length);
            if (parts.length > 1) {
                String second = first + " " + parts[1];
                logger.log(Level.FINE, "second: {0}", second);
                if (s2.contains(second)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean hasNKF(NodeList nodes) {
        try {
            for (int i = 0; i < nodes.getLength(); i++) {
                String signatura = nodes.item(i).getNodeValue();
                logger.log(Level.FINE, "signatura: {0}", signatura);
                if (isNKF(signatura)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean hasUKF(NodeList nodes) {
        if(nodes.getLength() == 0){
            return true;
        }
        try {
            for (int i = 0; i < nodes.getLength(); i++) {
                String signatura = nodes.item(i).getNodeValue();
                logger.log(Level.FINE, "signatura: {0}", signatura);
                if (!isNKF(signatura)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
