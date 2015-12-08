package cz.incad.vdkcommon.solr;

import cz.incad.vdkcommon.Options;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.common.SolrDocument;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class StorageCodeBrowser implements Iterable<Object> {

    private static final Logger logger = Logger.getLogger(StorageCodeBrowser.class.getName());

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
    
    boolean _init;
    private final Options opts;

    public StorageCodeBrowser() throws Exception {
        opts = Options.getInstance();

        this.host = opts.getString("solrHost", "http://localhost:8080/solr") + "/"
                + opts.getString("solrIdCore", "vdk_id");
        this.rows = opts.getInt("indexer.browse.rows", 100);

        this.idField = opts.getString("indexer.browse.idfield", "id");
        
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

        String urlStr = host + "/terms?wt=" + wt
                + "&terms.fl=code&terms.sort=index&terms.limit="+rows+"&terms.lower=" + cursor;
        
        
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
        JSONObject response = json.getJSONObject("terms");
        JSONArray codes = response.getJSONArray("code");
        numDocs = codes.length();
        for (int i = 0; i < codes.length(); i=i+2) {
            docs.add(codes.getString(i));
        }
        
        if(numDocs >= rows){
            this.cursor = codes.getString(numDocs-2);
        }

    }

    @Override
    public Iterator iterator() {
        if (!_init) {
            try {
                getDocs(cursor);
            } catch (Exception ex) {
                Logger.getLogger(StorageCodeBrowser.class.getName()).log(Level.SEVERE, "Error retrieving docs for iterator", ex);
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
                Logger.getLogger(StorageCodeBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }

            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
