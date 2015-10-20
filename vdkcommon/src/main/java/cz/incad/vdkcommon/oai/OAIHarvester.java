package cz.incad.vdkcommon.oai;

import cz.incad.vdkcommon.Bohemika;
import cz.incad.vdkcommon.xml.XMLReader;
import cz.incad.vdkcommon.Options;
import cz.incad.vdkcommon.Slouceni;
import cz.incad.vdkcommon.VDKJobData;
import cz.incad.vdkcommon.solr.Indexer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class OAIHarvester {

    private Logger logger;
    private JSONObject opts;
    XMLReader xmlReader;

    String completeListSize;
    int currentDocsSent = 0;
    int currentIndex = 0;

    Transformer xformer;

    String configFile;

    private final String LAST_HARVEST = "last_run";
    private final String LAST_MESSAGE = "last_message";
    JSONObject statusJson;

    Indexer indexer;
    HarvesterJobData jobData;
    FileHandler logFile;

    public OAIHarvester(String configFile) throws Exception {
        this.configFile = configFile;
        this.jobData = new HarvesterJobData(new VDKJobData(configFile, new JSONObject()));
        init();
    }

    public OAIHarvester(HarvesterJobData jobData) throws Exception {
        this.jobData = jobData;
        this.configFile = jobData.getConfigFile();
        init();
    }


    private void init() throws Exception {
        logger = Logger.getLogger(OAIHarvester.class.getName() + "_" + this.configFile);
        
        File fdef = FileUtils.toFile(Options.class.getResource("/cz/incad/vdkcommon/oai.json"));

        indexer = new Indexer(this.jobData.getVDKJobData());
        String json = FileUtils.readFileToString(fdef, "UTF-8");
        opts = new JSONObject(json);
        File f = new File(jobData.getConfigFile());
        if (f.exists() && f.canRead()) {
            json = FileUtils.readFileToString(f, "UTF-8");
            JSONObject confCustom = new JSONObject(json);
            Iterator keys = confCustom.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                logger.log(Level.INFO, "key {0} will be overrided", key);
                opts.put(key, confCustom.get(key));
            }
        } else {
            logger.log(Level.INFO, "File {0} is not present, using default configuration", jobData.getConfigFile());
        }

        try {
            File dir = new File(jobData.getHomeDir() + "logs");
            if (!dir.exists()) {
                boolean success = dir.mkdirs();
                if (!success) {
                    logger.log(Level.WARNING, "Can''t create logs directory");
                }
            }

        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
        xmlReader = new XMLReader();

        xformer = TransformerFactory.newInstance().newTransformer();

        logger.info("Harvester initialized");

    }

    private void getRecordsFromDisk() throws Exception {
        logger.info("Processing dowloaded files");
        getRecordsFromDir(new File(jobData.getPathToData()));

    }

    private void getRecordsFromDir(File dir) throws Exception {
        // check interrupted thread
        if (jobData.isInterrupted()) {

            logger.log(Level.INFO, "HARVESTER INTERRUPTED");
            return;
        }
        File[] children = dir.listFiles();
        Arrays.sort(children);
        for (File child : children) {// check interrupted thread
            if (jobData.isInterrupted()) {

                logger.log(Level.INFO, "HARVESTER INTERRUPTED");
                return;
            }
            if (currentDocsSent >= jobData.getMaxDocuments() && jobData.getMaxDocuments() > 0) {
                break;
            }
            if (child.isDirectory()) {
                getRecordsFromDir(child);
            } else {
                String identifier;
                logger.log(Level.INFO, "Loading file {0}", child.getPath());
                xmlReader.loadXmlFromFile(child);
                NodeList nodes = xmlReader.getListOfNodes("//oai:record");
                for (int j = 0; j < nodes.getLength(); j++) {
                    // check interrupted thread
                    if (jobData.isInterrupted()) {

                        logger.log(Level.INFO, "HARVESTER INTERRUPTED");
                        return;
                    }
                    if (currentIndex > jobData.getStartIndex()) {
                        identifier = xmlReader.getNodeValue("//oai:record[position()=" + (j + 1) + "]/oai:header/oai:identifier/text()");
                        

                        processRecord(nodes.item(j), identifier, j + 1);
                    }
                    currentIndex++;
                }
                logger.log(Level.INFO, "number: {0}", currentDocsSent);
            }
        }
    }

    public int harvest() throws Exception {

        try {
            
            
            logFile = new FileHandler(jobData.getHomeDir() + "logs" + File.separator + jobData.getVDKJobData().getConfigSimpleName() + ".log");
            logFile.setFormatter(new SimpleFormatter());

            logger.addHandler(logFile);

            long startTime = (new Date()).getTime();
            currentIndex = 0;

            if (jobData.isFromDisk()) {
                getRecordsFromDisk();
            } else {
                File statusFile = new File(jobData.getStatusFile());

                if (statusFile.exists()) {
                    statusJson = new JSONObject(FileUtils.readFileToString(statusFile, "UTF-8"));
                } else {
                    statusJson = new JSONObject();
                    statusJson.put(LAST_HARVEST, getInitialDate());
                }

                if (jobData.getResumptionToken() != null) {
                    logger.log(Level.INFO, "updating with resumptionToken: {0}", jobData.getResumptionToken());
                    getRecordWithResumptionToken(jobData.getResumptionToken());
                } else {
                    if (jobData.isFullIndex()) {
                        jobData.setFrom(getInitialDate());
                    } else {
                        jobData.setFrom(statusJson.optString(LAST_HARVEST, getInitialDate()));

                    }
                    logger.log(Level.INFO, "updating from: " + jobData.getFrom());
                    update(jobData.getFrom());
                }
            }

            long timeInMiliseconds = (new Date()).getTime() - startTime;
            logger.log(Level.INFO, "HARVEST SUCCESS {0} records", currentDocsSent);
            logger.info(formatElapsedTime(timeInMiliseconds));

        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        } finally{
            logFile.close();
            logger.removeHandler(logFile);
        }
        return currentDocsSent;
    }

    private void writeStatus(String from) throws FileNotFoundException, IOException {
        statusJson.put(LAST_HARVEST, from);
        File statusFile = new File(jobData.getStatusFile());
        FileUtils.writeStringToFile(statusFile, statusJson.toString());
    }

    private void update(String from) throws Exception {
        Calendar c_from = Calendar.getInstance();
        c_from.setTime(jobData.getSdfoai().parse(from));
        Calendar c_to = Calendar.getInstance();
        c_to.setTime(jobData.getSdfoai().parse(from));

        c_to.add(jobData.getInterval(), 1);

        String to;
        Date date = new Date();
        //jobData.getSdfoai().setTimeZone(TimeZone.getTimeZone("GMT"));

        if (jobData.getTo() == null) {
            to = jobData.getSdfoai().format(date);
        } else {
            to = jobData.getTo();
        }
        Date final_date = jobData.getSdfoai().parse(to);
        Date current = c_to.getTime();

        while (current.before(final_date)) {
            if (jobData.isInterrupted()) {
                break;
            }
            update(jobData.getSdfoai().format(c_from.getTime()), jobData.getSdfoai().format(current));
            c_to.add(jobData.getInterval(), 1);
            c_from.add(jobData.getInterval(), 1);
            current = c_to.getTime();
        }
        if (!jobData.isInterrupted()) {
            update(jobData.getSdfoai().format(c_from.getTime()), jobData.getSdfoai().format(final_date));
            writeStatus(to);
        }

    }

    private void update(String from, String until) throws Exception {
        logger.log(Level.INFO, "Harvesting from: {0} until: {1}", new Object[]{from, until});
        //responseDate = from;
        writeStatus(from);
        getRecords(from, until);
    }

    private void getRecordWithResumptionToken(String resumptionToken) throws Exception {
        while (resumptionToken != null && !resumptionToken.equals("")) {
            resumptionToken = getRecords("?verb=" + opts.getString("verb") + "&resumptionToken=" + resumptionToken);
        }
    }

    private String getInitialDate() throws Exception {
        logger.log(Level.INFO, "Retrieving initial date...");

        String urlString = opts.getString("baseUrl") + "?verb=Identify";

        URL url = new URL(urlString.replace("\n", ""));

        logger.log(Level.FINE, "url: {0}", url.toString());
        xmlReader.readUrl(url.toString());
        return xmlReader.getNodeValue("//oai:Identify/oai:earliestDatestamp/text()");
    }

    private void getRecords(String from, String until) throws Exception {
        String query = String.format("?verb=%s&from=%s&until=%s&metadataPrefix=%s&set=%s",
                opts.getString("verb"),
                from,
                until,
                jobData.getMetadataPrefix(),
                opts.getString("set"));
        jobData.setResumptionToken(getRecords(query));
        while (jobData.getResumptionToken() != null && !jobData.getResumptionToken().equals("")) {
            jobData.setResumptionToken(getRecords("?verb=" + opts.getString("verb") + "&resumptionToken=" + jobData.getResumptionToken()));
        }
    }

    private String getRecords(String query) throws Exception {

        if (jobData.isInterrupted()) {
            logger.log(Level.INFO, "HARVESTER INTERRUPTED");
            return null;
        }

        String urlString = opts.getString("baseUrl") + query;
        URL url = new URL(urlString.replace("\n", ""));
        logger.log(Level.INFO, "reading url: {0}", url.toString());
        try {
            xmlReader.readUrl(url.toString());
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.toString());
            logger.log(Level.WARNING, "retrying url: {0}", url.toString());
            xmlReader.readUrl(url.toString());
        }
        String error = xmlReader.getNodeValue("//oai:error/@code");
        if (error.equals("")) {
            completeListSize = xmlReader.getNodeValue("//oai:resumptionToken/@completeListSize");
            String date;
            String identifier;

            String fileName = null;
            String recdate = xmlReader.getNodeValue("//oai:record[position()=1]/oai:header/oai:datestamp/text()");
            if (jobData.isSaveToDisk()) {
                fileName = writeNodeToFile(xmlReader.getNodeElement(),
                        recdate,
                        xmlReader.getNodeValue("//oai:record[position()=1]/oai:header/oai:identifier/text()"));
                        writeStatus(recdate);
            }
            NodeList nodes = xmlReader.getListOfNodes("//oai:record");
            if (jobData.isOnlyIdentifiers()) {
                //TODO
            } else {
                if (!jobData.isOnlyHarvest() && currentIndex > jobData.getStartIndex()) {

                    for (int i = 0; i < nodes.getLength(); i++) {
                        identifier = xmlReader.getNodeValue("//oai:record[position()=" + (i + 1) + "]/oai:header/oai:identifier/text()");
                        recdate = xmlReader.getNodeValue("//oai:record[position()=" + (i + 1) + "]/oai:header/oai:datestamp/text()");
                        
                        // check interrupted thread
                        if (jobData.isInterrupted()) {

                            logger.log(Level.INFO, "HARVESTER INTERRUPTED");
                            statusJson.put(LAST_MESSAGE, "Harvester interrupted by user");
                            return null;
                        }
                        processRecord(nodes.item(i), identifier, i + 1);
                        currentIndex++;
                        statusJson.put(LAST_MESSAGE, "Harvested " + currentIndex + " records");
                        writeStatus(recdate);
                        logger.log(Level.FINE, "number: {0} of {1}", new Object[]{(currentDocsSent), completeListSize});
                    }
                }
            }

            return xmlReader.getNodeValue("//oai:resumptionToken/text()");
        } else {
            logger.log(Level.INFO, "{0} for url {1}", new Object[]{error, urlString});
        }
        return null;
    }

    private String writeNodeToFile(Node node, String date, String identifier) throws Exception {
        String dirName = opts.getString("indexDirectory") + File.separatorChar + jobData.getSdf().format(jobData.getSdfoai().parse(date));

        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                logger.log(Level.WARNING, "Can''t create: {0}", dirName);
            }
        }
        String xmlFileName = dirName + File.separatorChar + identifier.substring(opts.getString("identifierPrefix").length()) + ".xml";

        Source source = new DOMSource(node);
        File file = new File(xmlFileName);
        Result result = new StreamResult(file);
        xformer.transform(source, result);
        return xmlFileName;
    }

    private String nodeToString(Node node, int pos) throws Exception {

        String xslt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xsl:stylesheet version=\"1.0\" xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  >"
                + "<xsl:output omit-xml-declaration=\"yes\" method=\"xml\" indent=\"yes\" encoding=\"UTF-8\" />"
                + "<xsl:template  match=\"/\"><xsl:copy-of select=\"//oai:ListRecords/oai:record[position()=" + pos + "]\" /></xsl:template>"
                + "</xsl:stylesheet>";
        Transformer xformer2 = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslt)));
        StringWriter sw = new StringWriter();

        Source source = new DOMSource(node);

        xformer2.transform(source, new StreamResult(sw));
        return sw.toString();
    }

    private String nodeToString(Node node) throws Exception {

        StringWriter sw = new StringWriter();

        Source source = new DOMSource(node);

        xformer.transform(source, new StreamResult(sw));
        //System.out.println("sw.toString(): " + sw.toString());
        return sw.toString();
    }

    private String formatElapsedTime(long timeInMiliseconds) {
        long hours, minutes, seconds;
        long timeInSeconds = timeInMiliseconds / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        return hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
    }

    public static void main(String[] args) throws Exception {

        try {
            OAIHarvester oh = new OAIHarvester("nkc_vdk");
            //oh.setSaveToDisk(true);
//            oh.setFromDisk(true);
//            oh.setPathToData("/home/alberto/.vdkcr/OAI/harvest/NKC/2014/08/28/09/20/45");

            oh.harvest();

            //oh.harvest("-cfgFile nkp_vdk -dontIndex -fromDisk ", null, null);
            //oh.harvest("-cfgFile VKOL -dontIndex -saveToDisk -onlyHarvest", null, null);201304191450353201304220725599VKOLOAI:VKOL-M
            //oh.harvest("-cfgFile VKOL -dontIndex -saveToDisk -onlyHarvest jobData.getResumptionToken() 201304191450353201304220725599VKOLOAI:VKOL-M", null, null);
            //oh.harvest("-cfgFile MZK01-VDK -dontIndex -saveToDisk -onlyHarvest", null, null);
            //oh.harvest("-cfgFile MZK03-VDK -dontIndex -saveToDisk -onlyHarvest", null, null);
            //oh.harvest("-cfgFile nkp_vdk -dontIndex -saveToDisk -onlyHarvest ", null, null);
            //oh.harvest("-cfgFile nkp_vdk -dontIndex -saveToDisk -onlyHarvest -jobData.getResumptionToken() 201305160612203201305162300009NKC-VDK:NKC-VDKM", null, null);
        } catch (Exception ex) {
            System.out.println(ex);
        } 

    }

    /**
     *
     * @param node the xml node in the file
     * @param identifier the identifier of the oai record
     * @param index the index of the node in the xml file
     */
    private void processRecord(Node node, String identifier, int index) throws InterruptedException, Exception {
        logger.log(Level.INFO, "Processing record {0} ...", identifier);
        if (node != null) {

            String error = xmlReader.getNodeValue(node, "/oai:error/@code");
            if (error == null || error.equals("")) {

                String urlZdroje = opts.getString("baseUrl")
                        + "?verb=GetRecord&identifier=" + identifier
                        + "&metadataPrefix=" + jobData.getMetadataPrefix()
                        + "#set=" + opts.getString("set");

                if ("deleted".equals(xmlReader.getNodeValue(node, "./oai:header/@status"))) {
                    if (jobData.isFullIndex()) {
                        logger.log(Level.FINE, "Skip deleted record when fullindex");
                        return;
                    }
                    //zpracovat deletes
                    indexer.removeDoc(identifier);
                } else {
//                    String xmlStr = nodeToString(xmlReader.getNodeElement(), index);
                    String xmlStr = nodeToString(node);

                    String hlavninazev = xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']/text()");

                    String cnbStr = xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='015']/marc:subfield[@code='a']/text()");

                    JSONObject slouceni = Slouceni.fromXml(xmlStr);

                    

                    try {
                        indexer.store(identifier, 
                                slouceni.getString("docCode"), 
                                slouceni.getString("codeType"), 
                                Bohemika.isBohemika(xmlStr), 
                                xmlStr);
                        currentDocsSent++;
                    } catch (Exception ex) {
                        if (jobData.isContinueOnDocError()) {
                            logger.log(Level.WARNING, "Error writing doc to db. Id: {0}", identifier);
                        } else {
                            throw new Exception(ex);
                        }
                    }

                }
            } else {
                logger.log(Level.SEVERE, "Can't proccess xml {0}", error);
            }
        }
    }
}
