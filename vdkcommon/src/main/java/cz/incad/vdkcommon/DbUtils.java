package cz.incad.vdkcommon;

import cz.incad.vdkcommon.solr.IndexerQuery;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class DbUtils {

    static final Logger logger = Logger.getLogger(DbUtils.class.getName());
    private static Connection conn;


    public enum Roles {

        ADMIN("admin"),
        SOURCELIB("sourcelib"),
        USER("user"),
        LIB("lib");

        String value;

        Roles(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    };

    public static boolean isOracle(Connection conn) throws SQLException {
        DatabaseMetaData p = conn.getMetaData();
        return p.getDatabaseProductName().toLowerCase().contains("oracle");
    }

    public static Connection getConnection() throws NamingException, SQLException {
        if (conn == null || conn.isClosed()) {
            
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/vdk");
            conn = ds.getConnection();
        }
        return conn;
    }

    public static Connection getConnection(String className,
            String url, String username, String password) throws NamingException, SQLException, ClassNotFoundException {
        Class.forName(className);
        return DriverManager.getConnection(url, username, password);
    }

    public static String getXml(String id) throws SQLException {
        
        try {
            Options opts = Options.getInstance();
            SolrQuery query = new SolrQuery("id:\"" + id + "\"");
            query.addField("xml");
            query.setRows(1);
            SolrDocumentList docs = IndexerQuery.query(opts.getString("solrIdCore", "vdk_id"), query);
            Iterator<SolrDocument> iter = docs.iterator();
            if (iter.hasNext()) {
                SolrDocument resultDoc = iter.next();
                return (String) resultDoc.getFieldValue("xml");
            }else {
                return "<xml/>";
            }

        } catch (Exception ex) {
            return ex.toString();
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }

    public static String regenerateCodes() throws SQLException {
        String ret = "";
        int total = 0;
        String usql = "update zaznam set uniqueCode=?, codeType=? where zaznam_id=?";
        String sql = "select zaznam_id, sourceXML from zaznam";
        try {

            getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            PreparedStatement ups = conn.prepareStatement(usql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            //logger.log(Level.INFO, rs.getString("sourceXML"));
                // check interrupted thread
                if (Thread.currentThread().isInterrupted()) {
                    logger.log(Level.INFO, "REGENERATE MD5 CODE INTERRUPTED. Total records: {0}", total);
                    throw new InterruptedException();
                }
                int id = 0;
                try {
                    id = rs.getInt("zaznam_id");
                    logger.log(Level.INFO, "processing record " + id);
                    JSONObject slouceni = Slouceni.fromXml(rs.getString("sourceXML"));
                    logger.log(Level.INFO, "uniqueCode: {0}, type: {1}", 
                            new Object[]{slouceni.getString("docCode"),
                                slouceni.getString("codeType")
                            });

                    ups.setString(1, slouceni.getString("docCode"));
                    ups.setString(2, slouceni.getString("codeType"));
                    ups.setInt(3, id);
                    ups.executeUpdate();
                    logger.log(Level.INFO, "Record id {0} updated. Total: {1}", new Object[]{id, total});

                } catch (SQLException ex) {
                    logger.log(Level.WARNING, "Error in record " + id, ex);
                }
                total++;
            }
            //conn.commit();
            rs.close();

        } catch (Exception ex) {
            return ex.toString();
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
        return ret;
    }
}
