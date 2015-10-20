package cz.incad.vdkcommon.solr;

import cz.incad.vdkcommon.Options;
import cz.incad.vdkcommon.SolrIndexerCommiter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author alberto
 */
public class IndexerQuery {

    public static SolrDocumentList query(SolrQuery query) throws SolrServerException, IOException {
        SolrServer server = SolrIndexerCommiter.getServer();
        QueryResponse rsp = server.query(query);
        return rsp.getResults();
    }

    public static SolrDocumentList query(String core, SolrQuery query) throws SolrServerException, IOException {
        SolrServer server = SolrIndexerCommiter.getServer(core);
        QueryResponse rsp = server.query(query);
        return rsp.getResults();
    }

    public static SolrDocumentList queryOneField(String q, String[] fields, String[] fq) throws SolrServerException, IOException {
        SolrServer server = SolrIndexerCommiter.getServer();
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.setFilterQueries(fq);
        query.setFields(fields);
        query.setRows(100);
        QueryResponse rsp = server.query(query);
        return rsp.getResults();
    }
    
    public static String xml(String q) throws MalformedURLException, IOException {
        SolrQuery query = new SolrQuery(q);
        query.set("indent", true);

        return xml(query);
    }
    
    public static String xml(SolrQuery query) throws MalformedURLException, IOException {
        
        query.set("indent", true);
        query.set("wt", "xml");

    // use org.apache.solr.client.solrj.util.ClientUtils 
        // to make a URL compatible query string of your SolrQuery
        String urlQueryString = ClientUtils.toQueryString(query, false);
        Options opts = Options.getInstance();
        String solrURL = String.format("%s/%s/select",
                opts.getString("solrHost", "http://localhost:8080/solr"),
                opts.getString("solrCore", "vdk_md5"));
        URL url = new URL(solrURL + urlQueryString);

        // use org.apache.commons.io.IOUtils to do the http handling for you
        String xmlResponse = IOUtils.toString(url, "UTF-8");

        return xmlResponse;
    }
    
    
    
    public static String terms(SolrQuery query) throws MalformedURLException, IOException {
        
        query.set("indent", true);
        query.set("wt", "json");

    // use org.apache.solr.client.solrj.util.ClientUtils 
        // to make a URL compatible query string of your SolrQuery
        String urlQueryString = ClientUtils.toQueryString(query, false);
        Options opts = Options.getInstance();
        String solrURL = String.format("%s/%s/terms",
                opts.getString("solrHost", "http://localhost:8080/solr"),
                opts.getString("solrCore", "vdk_md5"));
        URL url = new URL(solrURL + urlQueryString);

        // use org.apache.commons.io.IOUtils to do the http handling for you
        String xmlResponse = IOUtils.toString(url, "UTF-8");

        return xmlResponse;
    }
    
    public static String json(SolrQuery query) throws MalformedURLException, IOException {
        
        query.set("indent", true);
        query.set("wt", "json");

    // use org.apache.solr.client.solrj.util.ClientUtils 
        // to make a URL compatible query string of your SolrQuery
        String urlQueryString = ClientUtils.toQueryString(query, false);
        Options opts = Options.getInstance();
        String solrURL = String.format("%s/%s/select",
                opts.getString("solrHost", "http://localhost:8080/solr"),
                opts.getString("solrCore", "vdk_md5"));
        URL url = new URL(solrURL + urlQueryString);

        // use org.apache.commons.io.IOUtils to do the http handling for you
        String xmlResponse = IOUtils.toString(url, "UTF-8");

        return xmlResponse;
    }

}
