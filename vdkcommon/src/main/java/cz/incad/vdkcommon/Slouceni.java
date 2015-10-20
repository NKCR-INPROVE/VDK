package cz.incad.vdkcommon;

import cz.incad.utils.RomanNumber;
import cz.incad.vdkcommon.xml.XMLReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.util.ArrayList;

/**
 *
 * @author alberto
 */
public class Slouceni {

    static final Logger logger = Logger.getLogger(Slouceni.class.getName());

    public static String export(String xml) {

        try {
            String retval = "";
            XMLReader xmlReader = new XMLReader();
            xmlReader.loadXml(xml);

            //ISBN
            String pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='020']/marc:subfield[@code='a']/text()");
            retval += pole + "\t";

            //ISSN
            pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='022']/marc:subfield[@code='a']/text()");
            retval += pole + "\t";

            //ccnb
            pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='015']/marc:subfield[@code='a']/text()");
            retval += pole + "\t";

            //Check 245n číslo části 
            String f245nraw = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='n']/text()");

            //Pole 250 údaj o vydání (nechat pouze numerické znaky) (jen prvni cislice)
            String f250a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']/text()");

            //Pole 100 autor – osobní jméno (ind1=1 →  prijmeni, jmeno; ind1=0 → jmeno, prijmeni.  
            //Obratit v pripade ind1=1, jinak nechat)
            String f100a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='100']/marc:subfield[@code='a']/text()");
            String ind1 = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='100']/@ind1");
            if ("1".equals(ind1) && !"".equals(f100a)) {
                String[] split = f100a.split(",", 2);
                if (split.length == 2) {
                    f100a = split[1] + split[0];
                }
            }

            //vyber poli
            retval += xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']/text()")
                    + "\t"
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='b']/text()") + "\t"
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='c']/text()") + "\t"
                    + f245nraw + "\t"
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='p']/text()") + "\t"
                    + f250a + "\t"
                    + f100a + "\t"
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='110']/marc:subfield[@code='a']/text()") + "\t"
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='111']/marc:subfield[@code='a']/text()") + "\t"
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']/text()") + "\t"
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']/text()") + "\t"
                    + onlyLeadNumbers(xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']/text()")) + "\t";

            return retval;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String toCSV(String isbn, String issn, String ccnb,
            String f245, String f245n, String f245p, String f250a,
            String f100a, String f110a, String f111a, String f260) {
        try {
            ArrayList<String> nextLine = new ArrayList<String>();
            nextLine.add(isbn == null ? "" : isbn);
            nextLine.add(issn == null ? "" : issn);
            nextLine.add(ccnb == null ? "" : ccnb);
            nextLine.add(f245 == null ? "" : f245);
            nextLine.add(f245n == null ? "" : f245n);
            nextLine.add(f245p == null ? "" : f245p);
            nextLine.add(f250a == null ? "" : f250a);
            nextLine.add(f100a == null ? "" : f100a);
            nextLine.add(f110a == null ? "" : f110a);
            nextLine.add(f111a == null ? "" : f111a);
            nextLine.add(f260 == null ? "" : f260);

            StringWriter sw = new StringWriter();
            CSVWriter writer = new CSVWriter(sw, '\t', '\"');
            writer.writeNext((String[]) nextLine.toArray(new String[nextLine.size()]));
            return sw.toString();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private static Map csvToMap(String[] parts) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            map.put("isbn", parts[0]);
            map.put("issn", parts[1]);
            map.put("ccnb", parts[2]);
            map.put("245a", parts[3]);
            map.put("245n", parts[4]);
            map.put("245p", parts[5]);
            map.put("250a", parts[6]);
            map.put("100a", parts[7]);
            map.put("110a", parts[8]);
            map.put("111a", parts[9]);
            map.put("260a", parts[10]);
            if (parts.length > 11) {
                map.put("cena", parts[11]);
            }
            if (parts.length > 12) {
                map.put("comment", parts[12]);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error parsing csv data: {0}", parts);
            logger.log(Level.SEVERE, null, ex);
        }
        return map;
    }

    private static Map csvToMap(String csv) throws IOException {
        CSVReader parser = new CSVReader(new StringReader(csv), '\t', '\"', false);
        String[] values = parser.readNext();
        if (values != null) {
            return csvToMap(values);
        } else {
            return null;
        }
    }

    private static String csvToJSONString(String csv) {
        try {

            CSVReader parser = new CSVReader(new StringReader(csv), '\t', '\"', false);
            String[] parts = parser.readNext();
            if (parts != null) {
                Map map = csvToMap(parts);
                String docCode = generateMD5(map).toString();
                return toJSON(map, docCode).toString();
            }

//            CSVStrategy strategy = new CSVStrategy('\t', '\"', '#');
//            CSVParser parser = new CSVParser(new StringReader(csv), strategy);
//            String[] parts = parser.getLine();
//            if (parts != null) {
//                return toJSON(csvToMap(parts)).toString();
//            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error parsing csv data: {0}", csv);
            logger.log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private static JSONObject toJSON(Map<String, String> map, String docCode) {
        try {
            JSONObject j = new JSONObject(map);
            j.put("docCode", docCode);
            return j;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static JSONObject generateMD5(Map<String, String> map) {
        JSONObject j = new JSONObject(map);
        try {

            //ISBN
            String pole = map.get("isbn");
            ISBN val = new ISBN();

            if (pole != null && !pole.equals("")) {
                //pole = pole.toUpperCase().substring(0, Math.min(13, pole.length()));
                if (!"".equals(pole) && val.isValid(pole)) {
                    j.put("docCode", MD5.generate(new String[]{pole}));
                    j.put("codeType", "isbn");
                    return j;
                }
            }

            //ISSN
            pole = map.get("issn");
            if (pole != null && !pole.equals("")) {
                //pole = pole.toUpperCase().substring(0, Math.min(13, pole.length()));
                if (!"".equals(pole) && val.isValid(pole)) {
                    j.put("docCode", MD5.generate(new String[]{pole}));
                    j.put("codeType", "issn");
                    return j;
                }
            }

            //ccnb
            pole = map.get("ccnb");
            //logger.log(Level.INFO, "ccnb: {0}", pole);
            if (pole != null && !"".equals(pole)) {
                j.put("docCode", MD5.generate(new String[]{pole}));
                j.put("codeType", "ccnb");
                return j;
            }

            //Check 245n číslo části 
            String f245nraw = map.get("245n");
            //logger.log(Level.INFO, "245n číslo části: {0}", f245nraw);
            String f245n = "";
            RomanNumber rn = new RomanNumber(f245nraw);
            if (rn.isValid()) {
                f245n = Integer.toString(rn.toInt());
            }

            //Pole 250 údaj o vydání (nechat pouze numerické znaky) (jen prvni cislice)
            String f250a = map.get("250a");
            //logger.log(Level.INFO, "f250a: {0}", f250a);
            f250a = onlyLeadNumbers(f250a);

            //Pole 100 autor – osobní jméno (ind1=1 →  prijmeni, jmeno; ind1=0 → jmeno, prijmeni.  
            //Obratit v pripade ind1=1, jinak nechat)
            String f100a = map.get("100a");
            String ind1 = map.get("100aind1");
            if ("1".equals(ind1) && !"".equals(f100a)) {
                String[] split = f100a.split(",", 2);
                if (split.length == 2) {
                    f100a = split[1] + split[0];
                }
            }

            if ("".equals(f100a)) {
                f100a = map.get("245c");
            }

            //vyber poli
            String uniqueCode = MD5.generate(new String[]{
                map.get("245a"),
                map.get("245b"),
                //map.get("245c"),
                f245n,
                map.get("245p"),
                f250a,
                f100a,
                map.get("110a"),
                map.get("111a"),
                map.get("260a"),
                map.get("260b"),
                onlyLeadNumbers(map.get("260c"))
            });
            j.put("docCode", uniqueCode);
            j.put("codeType", "fields");
            return j;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            j.put("error", ex);
            return j;
        }
        
    }
    
    public static JSONObject fromMap(Map map) {
        return generateMD5(map);
    }

    public static JSONObject fromCSVStringArray(String[] parts) throws IOException {

        return generateMD5(csvToMap(parts));
    }

    public static JSONObject fromCSV(String csv) throws IOException {
        CSVReader parser = new CSVReader(new StringReader(csv), '\t', '\"', false);
        String[] values = parser.readNext();
        if (values != null) {
            return generateMD5(csvToMap(values));
        } else {
            return null;
        }
    }

    public static JSONObject fromXml(String xml) {
        try {
            XMLReader xmlReader = new XMLReader();
            xmlReader.loadXml(xml);
            Map<String, String> map = new HashMap<String, String>();

            //ISBN
            String pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='020']/marc:subfield[@code='a']/text()");
            map.put("isbn", pole);

            //ISSN
            pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='022']/marc:subfield[@code='a']/text()");
            map.put("issn", pole);
            //ccnb
            pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='015']/marc:subfield[@code='a']/text()");
            map.put("ccnb", pole);

            //Check 245n číslo části 
            String f245nraw = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='n']/text()");
            //logger.log(Level.INFO, "245n číslo části: {0}", f245nraw);
            String f245n = "";
            RomanNumber rn = new RomanNumber(f245nraw);
            if (rn.isValid()) {
                f245n = Integer.toString(rn.toInt());
            }
            map.put("245n", f245n);

            //Pole 250 údaj o vydání (nechat pouze numerické znaky) (jen prvni cislice)
            String f250a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']/text()");
            //logger.log(Level.INFO, "f250a: {0}", f250a);
            f250a = onlyLeadNumbers(f250a);
            map.put("250a", f250a);

            //Pole 100 autor – osobní jméno (ind1=1 →  prijmeni, jmeno; ind1=0 → jmeno, prijmeni.  
            //Obratit v pripade ind1=1, jinak nechat)
            String f100a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='100']/marc:subfield[@code='a']/text()");
            String ind1 = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='100']/@ind1");
            if ("1".equals(ind1) && !"".equals(f100a)) {
                String[] split = f100a.split(",", 2);
                if (split.length == 2) {
                    f100a = split[1] + split[0];
                }
            }
            if ("".equals(f100a)) {
                f100a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='c']/text()");
            }

            map.put("245a",
                    xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']/text()")
                    + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='b']/text()"));
            map.put("245p", xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='p']/text()"));
            map.put("100a", f100a);
            map.put("110a", xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='110']/marc:subfield[@code='a']/text()"));
            map.put("111a", xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='111']/marc:subfield[@code='a']/text()"));
            map.put("260a", xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']/text()")
                    + " " + xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']/text()")
                    + " " + onlyLeadNumbers(xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']/text()"))
            );

            return generateMD5(map);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            JSONObject j = new JSONObject();
            j.put("error", ex);
            return j;
        }
    }

    private static String onlyLeadNumbers(String s) {
        if (s == null || "".equals(s)) {
            return s;
        }
        String retVal = "";
        int n = 0;
        while (n < s.length() && Character.isDigit(s.charAt(n))) {
            retVal += s.charAt(n);
            n++;
        }
        return retVal;
    }
    
    private static final char[] SEPARATORS = {'-', ' '};
    private static final String[] PREFIXES = {"ISBN ", "ISBN: ", "ISBN:"};
    
    private static String removePrefix(String original) {
        for (String prefix : PREFIXES) {
            if (original.startsWith(prefix)) {
                return original.substring(prefix.length());
            }
        }
        return original;
    }

    private static String removeSeparators(String original) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            char character = original.charAt(i);
            if (!isSeparator(character)) {
                result.append(character);
            }
        }
        return result.toString();
    }

    private static boolean isSeparator(char character) {
        for (char separator : SEPARATORS) {
            if (character == separator) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        String s = "\"\"	\"\"	\"cnb000245226\"	\"Hlubinami pravěku /\"	\"\"	\"\"	\"1. vyd.\"	\"Josef,, Augusta\"	\"\"	\"\"	\"Praha :Mladá fronta,1956\"";
        JSONObject json = new JSONObject(Slouceni.csvToJSONString(s));
        System.out.println(json);
        Map map = Slouceni.csvToMap(s);
        System.out.println(map);
        System.out.println(Slouceni.generateMD5(map));
        
        ISBN val = new ISBN();
        String isbn = "978-80-86526-78-2 (váz.) : ";
        System.out.println(isbn + ": " + val.isValid(isbn));
        isbn = "lkjklj(brož) :";
        System.out.println(isbn + ": " + val.isValid(isbn));
        isbn = "80-7112-003-0 :";
        System.out.println(isbn + ": " + val.isValid(isbn));
        isbn = "80-7179-001-X (brož) :";
        System.out.println(isbn + ": " + val.isValid(isbn));
        
    }

    
/*
    private static String generateMD5(String[] parts) {

        try {

            //ISBN
            String pole = parts[0];
            //logger.log(Level.INFO, "isbn: {0}", pole);
            ISBNValidator val = new ISBNValidator();
            if (!"".equals(pole) && val.isValid(pole)) {
                pole = pole.toUpperCase();
                return MD5.generate(new String[]{pole});
            }

            //ISSN
            pole = parts[1];
            //logger.log(Level.INFO, "issn: {0}", pole);
            if (!"".equals(pole) && val.isValid(pole)) {
                pole = pole.toUpperCase();
                return MD5.generate(new String[]{pole});
            }

            //ccnb
            pole = parts[3];
            //logger.log(Level.INFO, "ccnb: {0}", pole);
            if (!"".equals(pole)) {
                return MD5.generate(new String[]{pole});
            }

            //vyber poli
            return MD5.generate(Arrays.copyOfRange(parts, 4, 10));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String generateMD5Old(String xml) {
        try {
            XMLReader xmlReader = new XMLReader();
            xmlReader.loadXml(xml);

            //ISBN
            String pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='020']/marc:subfield[@code='a']/text()");
            pole = pole.toUpperCase();
            //logger.log(Level.INFO, "isbn: {0}", pole);
            ISBNValidator val = new ISBNValidator();
            if (!"".equals(pole) && val.isValid(pole)) {
                return MD5.generate(new String[]{pole});
            }

            //ISSN
            pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='022']/marc:subfield[@code='a']/text()");
            pole = pole.toUpperCase();
            //logger.log(Level.INFO, "issn: {0}", pole);
            if (!"".equals(pole) && val.isValid(pole)) {
                return MD5.generate(new String[]{pole});
            }

            //ccnb
            pole = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='015']/marc:subfield[@code='a']/text()");
            //logger.log(Level.INFO, "ccnb: {0}", pole);
            if (!"".equals(pole)) {
                return MD5.generate(new String[]{pole});
            }

            //Check 245n číslo části 
            String f245nraw = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='n']/text()");
            //logger.log(Level.INFO, "245n číslo části: {0}", f245nraw);
            String f245n = "";
            RomanNumber rn = new RomanNumber(f245nraw);
            if (rn.isValid()) {
                f245n = Integer.toString(rn.toInt());
            }

            //Pole 250 údaj o vydání (nechat pouze numerické znaky) (jen prvni cislice)
            String f250a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']/text()");
            //logger.log(Level.INFO, "f250a: {0}", f250a);
            f250a = onlyLeadNumbers(f250a);

            //Pole 100 autor – osobní jméno (ind1=1 →  prijmeni, jmeno; ind1=0 → jmeno, prijmeni.  
            //Obratit v pripade ind1=1, jinak nechat)
            String f100a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='100']/marc:subfield[@code='a']/text()");
            String ind1 = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='100']/@ind1");
            if ("1".equals(ind1) && !"".equals(f100a)) {
                String[] split = f100a.split(",", 2);
                if (split.length == 2) {
                    f100a = split[1] + split[0];
                }
            }
            if ("".equals(f100a)) {
                f100a = xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='c']/text()");
            }

            //vyber poli
            String uniqueCode = MD5.generate(new String[]{
                xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']/text()"),
                xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='b']/text()"),
                //xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='c']/text()"),
                f245n,
                xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='p']/text()"),
                f250a,
                f100a,
                xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='110']/marc:subfield[@code='a']/text()"),
                xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='111']/marc:subfield[@code='a']/text()"),
                xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']/text()"),
                xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']/text()"),
                onlyLeadNumbers(xmlReader.getNodeValue("/oai:record/oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']/text()"))
            });

            return uniqueCode;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }
*/
    
}
