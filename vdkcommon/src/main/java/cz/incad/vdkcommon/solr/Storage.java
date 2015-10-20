
package cz.incad.vdkcommon.solr;

import cz.incad.vdkcommon.Options;
import java.io.IOException;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author alberto
 */
public class Storage {

    public static SolrDocument getDoc(String id) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("id:\"" + id + "\"");
        query.setRows(1);
        SolrDocumentList docs = IndexerQuery.query(Options.getInstance().getString("solrIdCore", "vdk_id"), query);
        Iterator<SolrDocument> iter = docs.iterator();
        if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }

    public static SolrDocument getDocByCode(String code) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("code:\"" + code + "\"");
        query.setRows(1);
        SolrDocumentList docs = IndexerQuery.query(Options.getInstance().getString("solrIdCore", "vdk_id"), query);
        Iterator<SolrDocument> iter = docs.iterator();
        if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }

    public static boolean docExistsByCode(String docCode) throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("code:\"" + docCode + "\"");
        query.addField("id");
        query.setRows(1);
        SolrDocumentList docs = IndexerQuery.query(Options.getInstance().getString("solrIdCore", "vdk_id"), query);
        Iterator<SolrDocument> iter = docs.iterator();
        if (iter.hasNext()) {
            return true;
        } else {
            return false;
        }
    }
}
