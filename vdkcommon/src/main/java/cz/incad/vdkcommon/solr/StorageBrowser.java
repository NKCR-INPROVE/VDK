package cz.incad.vdkcommon.solr;

import cz.incad.vdkcommon.Options;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class StorageBrowser implements Iterable<Object> {

    private static final Logger logger = Logger.getLogger(StorageBrowser.class.getName());

    StorageIterator _iterator;
    ArrayList<Object> docs;
    int index;
    int rows;
    int numDocs;
    int numFound;
    String cursor;
    String fl;
    String wt = "json";
    String host;
    String core;
    String initStart = null;

    String idField;
    String browseField;
    String browseFieldType;
    boolean browseFieldSort = true;
    boolean byTerms = false;

    boolean _init;
    private final Options opts;

    public StorageBrowser() throws Exception {
        opts = Options.getInstance();

        this.host = opts.getString("solrHost", "http://localhost:8080/solr") + "/"
                + opts.getString("solrIdCore", "vdk_id");
        this.rows = opts.getInt("indexer.browse.rows", 100);

        this.idField = opts.getString("indexer.browse.idfield", "id");
        this.browseField = opts.getString("indexer.browse.field", "timestamp");
        this.browseFieldType = opts.getString("indexer.browse.field.type", "long");
        this.browseFieldSort = opts.getBoolean("indexer.browse.field.sort", true);
        this.byTerms = opts.getBoolean("indexer.browse.byTerms", false);

        docs = new ArrayList<Object>();
        _init = false;
        this.cursor = "*";
        _iterator = new StorageIterator();
    }

    public void setStart(String start) {
        this.cursor = start;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setFl(String fl) {
        this.fl = fl;
    }

    public void setWt(String wt) {
        this.wt = wt;
    }

    private void getDocs(String cursor) throws Exception {

        _init = true;
        docs.clear();

        String urlStr = host + "/select?wt=" + wt
                //+ "&q=*:*&cursorMark=" + cursor;
                + "&q=" + browseField + ":{" + cursor + "%20TO%20NOW]";
//
//        if (initStart != null) {
//            urlStr += "&fq=" + initStart;
//        }
        if(fl == null){
            //urlStr += "&fl=*," + browseField;
        }else if(!fl.contains(browseField)){
            urlStr += "&fl=" + fl + "," + browseField;
        }else{
            urlStr += "&fl=" + fl;
        }
        urlStr += "&rows=" + rows + "&start=0";
        if (browseFieldSort) {
            urlStr += "&sort=" + browseField + "+asc";
        }
        logger.log(Level.INFO, "urlStr: {0}", urlStr);

        java.net.URL url = new java.net.URL(urlStr);
        InputStream is;
        StringWriter resp = new StringWriter();
        try {
            is = url.openStream();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
        }

        org.apache.commons.io.IOUtils.copy(is, resp, "UTF-8");
        JSONObject json = new JSONObject(resp.toString());
        JSONObject response = json.getJSONObject("response");
        numFound = response.getInt("numFound");
        JSONArray jdocs = response.getJSONArray("docs");
        numDocs = jdocs.length();
        for (int i = 0; i < jdocs.length(); i++) {
            docs.add(jdocs.getJSONObject(i));
        }
        //this.cursor = json.getString("nextCursorMark");
        if(numDocs > 0){
            this.cursor = jdocs.getJSONObject(numDocs-1).getString(browseField);
        }

    }

    @Override
    public Iterator iterator() {
        if (!_init) {
            try {
                
                getDocs(cursor);
            } catch (Exception ex) {
                Logger.getLogger(StorageBrowser.class.getName()).log(Level.SEVERE, "Error retrieving docs for iterator", ex);
                return null;
            }
        }
        return _iterator;
    }

    protected class StorageIterator implements Iterator<Object> {
        
        int index = 0;

        public StorageIterator() {
        }

        @Override
        public boolean hasNext() {
            return docs.size() > 0;
        }

        @Override
        public Object next() {
            Object ret = docs.get(index);
            index++;
            try {
                if(index == docs.size()){
                    index = 0;
                    getDocs(cursor);
                }
            } catch (Exception ex) {
                Logger.getLogger(StorageBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }

            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
