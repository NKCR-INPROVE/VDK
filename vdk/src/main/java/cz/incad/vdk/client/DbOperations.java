package cz.incad.vdk.client;

import au.com.bytecode.opencsv.CSVReader;
import cz.incad.vdkcommon.DbUtils;
import cz.incad.vdkcommon.Knihovna;
import cz.incad.vdkcommon.Options;
import cz.incad.vdkcommon.Slouceni;
import cz.incad.vdkcommon.VDKScheduler;
import cz.incad.vdkcommon.solr.Indexer;
import cz.incad.vdkcommon.solr.IndexerQuery;
import cz.incad.vdkcommon.solr.Storage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONException;

public class DbOperations extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(DbOperations.class.getName());
    public static final String ACTION_NAME = "action";
    static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param req servlet req
     * @param resp servlet resp
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        try {
            String actionNameParam = req.getParameter(ACTION_NAME);
            if (actionNameParam != null) {
                Actions actionToDo = Actions.valueOf(actionNameParam);
                actionToDo.doPerform(req, resp);
            } else {
                PrintWriter out = resp.getWriter();
                out.print("actionNameParam -> " + actionNameParam);
            }
        } catch (IOException e1) {
            LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
            PrintWriter out = resp.getWriter();
            out.print(e1.toString());
        } catch (SecurityException e1) {
            LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e1) {
            LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
            out.print(e1.toString());
        }
    }

    public static int insertOfferPg(Connection conn, String name, int idKnihovna, InputStream uploadedStream) throws Exception {
        int retVal = 0;
        String sql = "insert into OFFER (nazev, bdata, knihovna, update_timestamp, closed, archived) "
                + "values (?,?,?, NOW(), false, false) returning offer_id";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        if (uploadedStream != null) {
            ps.setBinaryStream(2, uploadedStream, uploadedStream.available());
        } else {
            ps.setNull(2, Types.BINARY);
        }
        ps.setInt(3, idKnihovna);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            retVal = rs.getInt(1);
        }
        rs.close();
        return retVal;

    }

    public static int insertDemandPg(Connection conn, String name, int idKnihovna) throws Exception {
        int retVal = 0;
        String sql = "insert into DEMAND (nazev, knihovna, update_timestamp, closed) values (?,?, NOW(), false) returning demand_id";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setInt(2, idKnihovna);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            retVal = rs.getInt(1);
        }
        rs.close();
        return retVal;

    }

    public static int closeDemand(Connection conn, int id) throws Exception {

        String sql = "update DEMAND set closed=? where demand_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setBoolean(1, true);
        ps.setInt(2, id);
        return ps.executeUpdate();
    }

    public static int closeOffer(Connection conn, int offerid) throws Exception {

        String sql = "update OFFER set closed=?, datum=? where offer_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setBoolean(1, true);
        ps.setDate(2, new java.sql.Date(new Date().getTime()));
        ps.setInt(3, offerid);
        return ps.executeUpdate();
    }

    public static int insertOfferOracle(Connection conn, String name, int idKnihovna, InputStream uploadedStream) throws Exception {
        int retVal = 1;
        String sql1 = "select Offer_ID_SQ.nextval from dual";
        ResultSet rs = conn.prepareStatement(sql1).executeQuery();
        if (rs.next()) {
            retVal = rs.getInt(1);
        }
        rs.close();

        String sql = "insert into OFFER (nazev, knihovna, update_timestamp, offer_id, closed, archived) values (?,?, sysdate, ?, 0, 0)";
        LOGGER.log(Level.INFO, "executing " + sql + "\nparams: {0}, {1}, {2}", new Object[]{name, idKnihovna, retVal});
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
//        if (uploadedStream != null) {
//            ps.setBinaryStream(2, uploadedStream, uploadedStream.available());
//        } else {
//            ps.setNull(2, Types.BLOB);
//        }
        ps.setInt(2, idKnihovna);
        ps.setInt(3, retVal);
        ps.executeUpdate();
        return retVal;

    }

    public static void reactionToOffer(Connection conn, int zaznam_offer, int pr_knihovna) throws SQLException, Exception {
        String sql;
        Date d = new Date();
        sql = "update ZAZNAMOFFER set pr_knihovna=?, pr_timestamp=? where zaznamoffer_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        if(pr_knihovna == -1){
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setNull(2, java.sql.Types.DATE);
        }else{
            ps.setInt(1, pr_knihovna);
            ps.setDate(2, new java.sql.Date(d.getTime()));
        }
        ps.setInt(3, zaznam_offer);
        ps.executeUpdate();
        sql = "select uniquecode from ZAZNAMOFFER where zaznamoffer_id=?";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, zaznam_offer);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
            Indexer indexer = new Indexer(f);
            String uniqueCode = rs.getString("uniquecode");
            indexer.removeDocOffers(uniqueCode);
            indexer.indexDocOffers(uniqueCode);
        }
        rs.close();
    }

    public static void changeWantOffer(Connection conn, int zaznam_offer, int knihovna, boolean wanted) throws Exception {

        String sql = "update WANTED set wants=?, update_timestamp=? "
                + "where knihovna=? and ZaznamOffer=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setBoolean(1, wanted);
        ps.setDate(2, new java.sql.Date(new Date().getTime()));
        ps.setInt(3, knihovna);
        ps.setInt(4, zaznam_offer);
        ps.executeUpdate();

        if (wanted) {
            reactionToOffer(conn, zaznam_offer, knihovna);
        }else{
            reactionToOffer(conn, zaznam_offer, -1);
        }

    }

    public static int insertWantOffer(Connection conn, int zaznam_offer, int knihovna, boolean wanted) throws Exception {

        int newid = 1;
        if (DbUtils.isOracle(conn)) {
            String sql1 = "select Wanted_ID_SQ.nextval from dual";
            ResultSet rs = conn.prepareStatement(sql1).executeQuery();
            if (rs.next()) {
                newid = rs.getInt(1);
            }
            rs.close();

            String sql = "insert into WANTED (ZaznamOffer, knihovna, wants, wanted_id, update_timestamp) values (?,?,?,?,sysdate)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, zaznam_offer);
            ps.setInt(2, knihovna);
            ps.setBoolean(3, wanted);
            ps.setInt(4, newid);
            ps.executeUpdate();
        } else {
            String sql = "insert into WANTED (ZaznamOffer, knihovna, wants,update_timestamp) values (?,?,?,NOW()) returning wanted_id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, zaznam_offer);
            ps.setInt(2, knihovna);
            ps.setBoolean(3, wanted);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                newid = rs.getInt(1);
            } else {
                newid = 0;
            }
        }
        if (wanted) {
            reactionToOffer(conn, zaznam_offer, knihovna);
        }

        return newid;
    }

    public static int insertNabidka(Connection conn,
            int idKnihovna,
            String zaznam_id,
            String exemplar_id,
            String docCode,
            int idOffer,
            String line) throws SQLException {

        if (DbUtils.isOracle(conn)) {
            String sql1 = "select ZaznamOffer_ID_SQ.nextval from dual";
            ResultSet rs = conn.prepareStatement(sql1).executeQuery();
            int idW = 1;
            if (rs.next()) {
                idW = rs.getInt(1);
            }
            rs.close();

            String sql = "insert into ZaznamOffer "
                    + "(uniqueCode, zaznam, exemplar, knihovna, ZaznamOffer_id, offer, fields,update_timestamp) "
                    + "values (?,?,?,?,?,?,?,sysdate)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, docCode);
            ps.setString(2, zaznam_id == null ? "" : zaznam_id);
            ps.setString(3, exemplar_id == null ? "" : exemplar_id);
            ps.setInt(4, idKnihovna);
            ps.setInt(5, idW);
            ps.setInt(6, idOffer);
            ps.setString(7, line);
            ps.executeUpdate();
            return idW;
        } else {
            String sql = "insert into ZaznamOffer "
                    + "(uniqueCode, zaznam, exemplar, knihovna,offer,fields,update_timestamp) "
                    + "values (?,?,?,?,?,?,NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, docCode);
            ps.setString(2, zaznam_id == null ? "" : zaznam_id);
            ps.setString(3, exemplar_id == null ? "" : exemplar_id);
            ps.setInt(4, idKnihovna);
            ps.setInt(5, idOffer);
            ps.setString(6, line);
            int idW = ps.executeUpdate();
            return idW;
        }
        //indexWeOffer(conn, id, docCode, "md5");
    }

    public static void removeZaznamOffer(Connection conn,
            int idKnihovna,
            int ZaznamOffer_id) throws Exception {

        String sql = "delete from ZaznamOffer where ZaznamOffer_id=? and knihovna=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, ZaznamOffer_id);
        ps.setInt(2, idKnihovna);
        ps.executeUpdate();
    }

    public static void removeDemand(Connection conn,
            int id, int idKnihovna) throws Exception {

        String sql = "delete from ZaznamDemand where zaznamdemand_id=? and knihovna=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.setInt(2, idKnihovna);
        ps.executeUpdate();

    }

    public static void insertToDemand(Connection conn,
            int idKnihovna,
            String zaznam_id,
            String exemplar_id,
            String docCode,
            String line) throws Exception {

        if (DbUtils.isOracle(conn)) {
            String sql1 = "select ZaznamDemand_ID_SQ.nextval from dual";
            ResultSet rs = conn.prepareStatement(sql1).executeQuery();
            int idW = 1;
            if (rs.next()) {
                idW = rs.getInt(1);
            }

            String sql = "insert into ZaznamDemand "
                    + "(uniqueCode, zaznam, exemplar, knihovna, ZaznamDemand_id, fields, update_timestamp) "
                    + "values (?,?,?,?,?,?,sysdate)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, docCode);
            ps.setString(2, zaznam_id);
            if (exemplar_id == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, exemplar_id);
            }
            ps.setInt(4, idKnihovna);
            ps.setInt(5, idW);
            ps.setString(6, line);
            ps.executeUpdate();
        } else {
            String sql = "insert into ZaznamDemand "
                    + "(uniqueCode, zaznam, exemplar, knihovna,fields, update_timestamp) "
                    + "values (?,?,?,?,?,NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, docCode);
            ps.setString(2, zaznam_id);
            if (exemplar_id == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, exemplar_id);
            }
            ps.setInt(4, idKnihovna);
            ps.setString(5, line);
            ps.executeUpdate();
        }
        //indexWeOffer(conn, id, docCode, "md5");
    }

    public static int insertDemandOracle(Connection conn, String name, int idKnihovna) throws Exception {
        int retVal = 1;
        String sql1 = "select Demand_ID_SQ.nextval from dual";
        ResultSet rs = conn.prepareStatement(sql1).executeQuery();
        if (rs.next()) {
            retVal = rs.getInt(1);
        }
        rs.close();

        String sql = "insert into DEMAND (nazev, knihovna, demand_id, update_timestamp, closed) values (?,?, ?, sysdate, 0)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setInt(2, idKnihovna);
        ps.setInt(3, retVal);
        ps.executeUpdate();
        return retVal;

    }

    public static void processStream(Connection conn, InputStream is, int idKnihovna, int idOffer, JSONObject json) throws Exception {
        processStream(conn, new InputStreamReader(is), idKnihovna, idOffer, json);
    }

    public static void processStream(Connection conn, Reader reader, int idKnihovna, int idOffer, JSONObject json) throws Exception {
        try {
            CSVReader parser = new CSVReader(reader, '\t', '\"', false);
            String[] parts = parser.readNext();
            int lines = 0;
            while (parts != null) {
                if (!(parts.length == 1 && parts[0].equals(""))) {

                    JSONObject slouceni = Slouceni.fromCSVStringArray(parts);
                    String docCode = slouceni.getString("docCode");
                    insertNabidka(conn, idKnihovna, null, null, docCode, idOffer, slouceni.toString());
                    lines++;
                }
                parts = parser.readNext();
            }
            json.put("message", "imported " + lines + " lines to offer: " + idOffer);
        } catch (IOException | JSONException | SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            json.put("error", "Error processing file: " + ex.toString());
            //throw new Exception("Not valid csv file. Separator must be tabulator and line must be ", ex);
        }
    }

    private static JSONArray wantedJSON(ResultSet rs) throws Exception {
        JSONArray ja = new JSONArray();
        while (rs.next()) {
            JSONObject j = new JSONObject();
            j.put("wanted_id", rs.getInt("wanted_id"));
            j.put("zaznamoffer", rs.getInt("zaznamoffer"));
            j.put("wanted", rs.getBoolean("wants"));
            j.put("knihovna", rs.getString("code"));
            j.put("priorita", rs.getString("priorita"));
            j.put("date", rs.getString("update_timestamp"));
            ja.put(j);
        }
        return ja;
    }

    private static JSONArray getWantedById(Connection conn, int ZaznamOffer_id) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code, k.priorita "
                + "from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where zo.ZaznamOffer_id=? "
                + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer order by k.priorita, zo.update_timestamp";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, ZaznamOffer_id);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONArray getWanted(Connection conn) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code, k.priorita "
                + "from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer order by k.priorita, zo.update_timestamp";
        PreparedStatement ps = conn.prepareStatement(sql);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONArray getLibraryWantedByCode(Connection conn, String docCode, int idKnihovna) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where zo.uniquecode=? and w.knihovna=? "
                + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer order by zo.update_timestamp";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, docCode);
        ps.setInt(2, idKnihovna);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONArray getLibraryWanted(Connection conn, int idKnihovna) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where k.knihovna_id=? "
                + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer order by zo.update_timestamp";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idKnihovna);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONArray getWantedByCode(Connection conn, String docCode) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code, k.priorita "
                + "from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where zo.uniquecode=? "
                + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, docCode);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONObject jsonZaznamOffer(int ZaznamOffer_id,
            String uniqueCode,
            String title,
            String zaznam,
            String exemplar,
            String knihovna,
            String pr_knihovna,
            JSONObject fields) throws Exception {

        JSONObject j = new JSONObject();
        j.put("ZaznamOffer_id", ZaznamOffer_id);
        j.put("uniqueCode", uniqueCode);
        Object solrTitle = null;
        if (title == null || zaznam == null) {
            SolrQuery query = new SolrQuery("code:" + uniqueCode);
            query.addField("title");
            query.addField("id");
            SolrDocumentList docs = IndexerQuery.query(query);
            Iterator<SolrDocument> iter = docs.iterator();
            if (iter.hasNext()) {
                SolrDocument resultDoc = iter.next();
                solrTitle = resultDoc.getFirstValue("title");
                j.put("zaznam_asoc", new JSONArray(resultDoc.getFieldValues("id")));
            }
        }
        if (title == null) {
            if (fields.has("245a")) {
                j.put("title", fields.getString("245a"));
            } else {
                j.put("title", solrTitle);
            }
        } else {
            j.put("title", title);
        }
        if (zaznam != null) {
            j.put("zaznam", zaznam);
        }

        j.put("exemplar", exemplar);
        j.put("knihovna", knihovna);
        j.put("pr_knihovna", pr_knihovna);
        j.put("fields", fields);
        return j;
    }

    public static ResultSet reportOffer(String id) throws Exception {

        Connection conn = DbUtils.getConnection();

        String sql = "SELECT z.hlavniNazev, zo.zaznamoffer_id, zo.uniqueCode, zo.zaznam, zo.exemplar, zo.fields "
                + "FROM Zaznam z "
                + "RIGHT OUTER JOIN zaznamOffer zo "
                + "ON zo.zaznam=z.identifikator "
                + "where zo.offer=" + id;
        PreparedStatement ps = conn.prepareStatement(sql);

        return ps.executeQuery();
    }

    public static JSONObject getOfferRows(String id) throws Exception {

        Connection conn;
        JSONObject json = new JSONObject();
        try {
            conn = DbUtils.getConnection();

            String sql = "select * from ZaznamOffer where offer=" + id;
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                int zoId = rs.getInt("ZaznamOffer_id");
                String zaznam = rs.getString("zaznam");
                JSONObject j = jsonZaznamOffer(zoId,
                        rs.getString("uniqueCode"),
                        null,
                        zaznam,
                        rs.getString("exemplar"),
                        rs.getString("knihovna"),
                        rs.getString("pr_knihovna"),
                        new JSONObject(rs.getString("fields")));

                j.put("wanted", getWantedById(conn, zoId));
                json.put(Integer.toString(zoId), j);
            }
            rs.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            json.put("error", ex);
        }
        return json;
    }

    private static JSONObject jsonZaznamDemand(ResultSet rs) throws Exception {

        JSONObject j = new JSONObject();

        Date date = rs.getDate("update_timestamp");
        j.put("zaznamdemand_id", rs.getString("zaznamdemand_id"));
        j.put("knihovna", rs.getString("code"));
        j.put("code", rs.getString("uniqueCode"));
        j.put("zaznam", rs.getString("zaznam"));
        j.put("exemplar", rs.getString("exemplar"));
        j.put("fields", new JSONObject(rs.getString("fields")));
        j.put("date", sdf.format(date));

        SolrQuery query = new SolrQuery("code:" + rs.getString("uniqueCode"));
        query.addField("title");
        SolrDocumentList docs = IndexerQuery.query(query);
        Iterator<SolrDocument> iter = docs.iterator();
        if (iter.hasNext()) {
            SolrDocument resultDoc = iter.next();
            j.put("title", resultDoc.getFirstValue("title"));
        }

        return j;
    }

    public static JSONObject getDemands() throws Exception {
        Connection conn;
        JSONObject json = new JSONObject();
        try {
            conn = DbUtils.getConnection();
            String sql = "select ZAZNAMDEMAND.*, KNIHOVNA.code from ZAZNAMDEMAND, KNIHOVNA where ZAZNAMDEMAND.knihovna=KNIHOVNA.knihovna_id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                json.put(rs.getString("zaznamdemand_id"), jsonZaznamDemand(rs));
            }
            rs.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            json.put("error", ex);
        }
        return json;
    }

    private static JSONObject offerJSON(Date offerDate,
            String id,
            String nazev,
            String knihovna,
            boolean closed) throws JSONException, IOException {
        JSONObject j = new JSONObject();
        j.put("id", id);
        j.put("nazev", nazev);
        j.put("knihovna", knihovna);
        j.put("closed", closed);
        if (offerDate != null) {
            Calendar now = Calendar.getInstance();
            Calendar o = Calendar.getInstance();
            o.setTime(offerDate);
            o.add(Calendar.DATE, Options.getInstance().getInt("expirationDays", 7) * 3);
            j.put("date", sdf.format(offerDate));
            j.put("expires", sdf.format(o.getTime()));
            j.put("expired", !o.after(now));
        }

        return j;
    }

    public static JSONObject getProtocol(int id) throws SQLException {
        Connection conn = null;
        JSONObject json = new JSONObject();
        try {
            conn = DbUtils.getConnection();
            String sql = "select OFFER.*, KNIHOVNA.code from OFFER, KNIHOVNA where OFFER.knihovna=KNIHOVNA.knihovna_id and offer_id=" + id;
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Date offerDate = rs.getDate("datum");
                JSONObject j = offerJSON(offerDate,
                        rs.getString("offer_id"),
                        rs.getString("nazev"),
                        rs.getString("code"),
                        rs.getBoolean("closed"));

                json.put("offer", j);
                Knihovna kn = new Knihovna(rs.getString("code"));
                json.put("knihovna", kn.getJson());
            }
            rs.close();

            JSONObject kns = new JSONObject();
            json.put("prejimajici", kns);
            sql = "select * from ZaznamOffer where offer=" + id;
            PreparedStatement psr = conn.prepareStatement(sql);

            rs = psr.executeQuery();
            while (rs.next()) {
                int pr_knihovna = rs.getInt("pr_knihovna");
                if (pr_knihovna == 0) {
                    continue;
                }
                if (!kns.has("pr_" + pr_knihovna)) {
                    Knihovna kn = new Knihovna(pr_knihovna);
                    JSONObject jpr = new JSONObject();
                    jpr.put("rows", new JSONArray());
                    jpr.put("knihovna", kn.getJson());
                    kns.put("pr_" + pr_knihovna, jpr);
                }
                int zoId = rs.getInt("ZaznamOffer_id");
                String zaznam = rs.getString("zaznam");
                JSONObject j = jsonZaznamOffer(zoId,
                        rs.getString("uniqueCode"),
                        null,
                        zaznam,
                        rs.getString("exemplar"),
                        rs.getString("knihovna"),
                        rs.getString("pr_knihovna"),
                        new JSONObject(rs.getString("fields")));

                kns.getJSONObject("pr_" + pr_knihovna).getJSONArray("rows").put(j);
            }
            rs.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            json.put("error", ex);
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
        return json;
    }

    public static JSONObject getOffer(int id) throws Exception {

        Connection conn;
        JSONObject json = new JSONObject();
        try {
            conn = DbUtils.getConnection();
            String sql = "select OFFER.*, KNIHOVNA.code from OFFER, KNIHOVNA where OFFER.knihovna=KNIHOVNA.knihovna_id and offer_id=" + id;
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Date offerDate = rs.getDate("datum");
                JSONObject j = offerJSON(offerDate,
                        rs.getString("offer_id"),
                        rs.getString("nazev"),
                        rs.getString("code"),
                        rs.getBoolean("closed"));

                json.put(rs.getString("offer_id"), j);
            }
            rs.close();
        } catch (NamingException ex) {
            json.put("error", ex);
        } catch (SQLException ex) {
            json.put("error", ex);
        } catch (JSONException ex) {
            json.put("error", ex);
        } catch (IOException ex) {
            json.put("error", ex);
        }
        return json;
    }

    public static JSONObject getOffers() throws Exception {

        Connection conn;
        JSONObject json = new JSONObject();
        try {
            conn = DbUtils.getConnection();
            String sql = "select OFFER.*, KNIHOVNA.code from OFFER, KNIHOVNA where OFFER.knihovna=KNIHOVNA.knihovna_id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Date offerDate = rs.getDate("datum");
                JSONObject j = offerJSON(offerDate,
                        rs.getString("offer_id"),
                        rs.getString("nazev"),
                        rs.getString("code"),
                        rs.getBoolean("closed"));

                json.put(rs.getString("offer_id"), j);
            }
            rs.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            json.put("error", ex);
        }
        return json;
    }

    //public static int getIdKnihovna_(String code, Connection conn) throws Exception {
    public static int getIdKnihovna(HttpServletRequest req) throws Exception {
        String user = req.getRemoteUser();
        Knihovna kn = LoggedController.knihovna(req);
        if (kn != null) {
            return kn.getId();
        }
        return 0;
    }

    public static ArrayList<String> getRoles() throws Exception {

        Connection conn;
        ArrayList<String> ret = new ArrayList<String>();
        try {
            conn = DbUtils.getConnection();
            String sql = "select distinct(name) from ROLE";
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(rs.getString(1));
            }
            rs.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static ArrayList<Knihovna> getUsers() throws Exception {

        Connection conn;
        ArrayList<Knihovna> ret = new ArrayList<Knihovna>();
        try {
            conn = DbUtils.getConnection();
            String sql = "select code from KNIHOVNA";
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(new Knihovna(rs.getString("code")));
            }
            rs.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    enum Actions {

        GETLIBRARYWANTED {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        Connection conn;

                        try {
                            Knihovna kn = LoggedController.knihovna(req);
                            if (kn != null) {
                                conn = DbUtils.getConnection();
                                JSONArray ja = getLibraryWanted(conn, kn.getId());
                                json.put(kn.getCode(), ja);
                            } else {
                                json.put("error", "nejste prihlasen");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "get wanted failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        GETLIBRARYWANTEDBYCODE {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        Connection conn;

                        try {
                            Knihovna kn = LoggedController.knihovna(req);
                            if (kn != null) {
                                conn = DbUtils.getConnection();
                                String code = req.getParameter("code");
                                JSONArray ja = getLibraryWantedByCode(conn, code, kn.getId());
                                json.put(code, ja);
                            } else {
                                json.put("error", "nejste prihlasen");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "get wanted failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        GETWANTEDBYCODE {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        Connection conn;

                        try {
                            //Knihovna kn = LoggedController.knihovna(req);
                            //if (kn != null) {
                            conn = DbUtils.getConnection();
                            String code = req.getParameter("code");
                            JSONArray ja = getWantedByCode(conn, code);
                            json.put(code, ja);
//                            } else {
//                                json.put("error", "nejste prihlasen");
//                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "get wanted failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        GETWANTED {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();

                        Connection conn;

                        try {
                            conn = DbUtils.getConnection();
                            JSONArray ja = getWanted(conn);
                            out.println(ja.toString());
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "get wanted failed", ex);
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                        }
                    }
                },
        REACTIONTOOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        int zaznam_offer = Integer.parseInt(req.getParameter("zaznam_offer"));
                        boolean wanted = Boolean.parseBoolean(req.getParameter("wanted"));
                        boolean isNew = Boolean.parseBoolean(req.getParameter("isnew"));

                        Connection conn;

                        Knihovna kn = LoggedController.knihovna(req);

                        try {
                            if (kn != null) {
                                conn = DbUtils.getConnection();
                                if (isNew) {
                                    int newid = insertWantOffer(conn, zaznam_offer, kn.getId(), wanted);
                                    json.put("id", newid);
                                } else {
                                    changeWantOffer(conn, zaznam_offer, kn.getId(), wanted);
                                }
                                json.put("message", "Reakce pridana");
                            } else {
                                json.put("error", "nejste prihlasen");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "want offer failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        WANTOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        int zaznam_offer = Integer.parseInt(req.getParameter("zaznam_offer"));
                        boolean wanted = Boolean.parseBoolean(req.getParameter("wanted"));

                        Connection conn;

                        Knihovna kn = LoggedController.knihovna(req);

                        try {
                            if (kn != null) {
                                conn = DbUtils.getConnection();

                                int newid = insertWantOffer(conn, zaznam_offer, kn.getId(), wanted);
                                json.put("message", "Reakce pridana. Id: " + newid);
                                json.put("id", newid);
                            } else {
                                json.put("error", "nejste prihlasen");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "want offer failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        SAVEVIEW {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("text/plain");
                        PrintWriter out = resp.getWriter();

                        String query = req.getQueryString().replace("action=SAVEVIEW&", "");
                        String name = req.getParameter("viewName");
                        boolean isGlobal = "on".equals(req.getParameter("viewGlobal"));

                        Connection conn;

                        Knihovna kn = LoggedController.knihovna(req);
                        int idKnihovna = 0;
                        if (kn != null) {
                            idKnihovna = kn.getId();
                        } else {
                            out.println("Operation not allowed. Not logged.");
                            return;
                        }
                        try {
                            conn = DbUtils.getConnection();
                            if (DbUtils.isOracle(conn)) {
                                String sql1 = "select Pohled_ID_SQ.nextval from dual";
                                ResultSet rs = conn.prepareStatement(sql1).executeQuery();
                                int idPohled = 1;
                                if (rs.next()) {
                                    idPohled = rs.getInt(1);
                                }
                                rs.close();

                                String sql = "insert into POHLED "
                                + "(nazev, query, knihovna, isGlobal, pohled_id, update_timestamp) "
                                + "values (?,?,?,?,?,sysdate)";
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ps.setString(1, name);
                                ps.setString(2, query);
                                ps.setInt(3, idKnihovna);
                                ps.setBoolean(4, isGlobal);
                                ps.setInt(5, idPohled);
                                ps.executeUpdate();
                            } else {

                                String sql = "insert into POHLED "
                                + "(nazev, query, knihovna, isGlobal,update_timestamp) "
                                + "values (?,?,?,?,NOW())";
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ps.setString(1, name);
                                ps.setString(2, query);
                                ps.setInt(3, idKnihovna);
                                ps.setBoolean(4, isGlobal);
                                ps.executeUpdate();
                            }
                            ;
                            out.println("Pohled " + name + " ulozen:");
                            out.println("query " + query);

                        } catch (Exception ex) {
                            out.println(ex);
                        }

                    }
                },
        LOADVIEWS {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("text/plain");
                        PrintWriter out = resp.getWriter();
                        Connection conn;
                        try {
                            conn = DbUtils.getConnection();
                            String sql;
                            if (DbUtils.isOracle(conn)) {
                                sql = "select * from POHLED where isGlobal=1 OR knihovna=" + getIdKnihovna(req);
                            } else {
                                sql = "select * from POHLED where isGlobal='true' OR knihovna=" + getIdKnihovna(req);
                            }

                            PreparedStatement ps = conn.prepareStatement(sql);
                            ResultSet rs = ps.executeQuery();
                            JSONObject json = new JSONObject();
                            JSONArray jarray = new JSONArray();
                            json.put("knihovna", getIdKnihovna(req));
                            json.put("views", jarray);
                            while (rs.next()) {
                                JSONObject o = new JSONObject();
                                o.put("nazev", rs.getString("nazev"));
                                o.put("query", rs.getString("query"));
                                o.put("isGlobal", rs.getBoolean("isGlobal"));
                                o.put("knihovna", rs.getInt("knihovna"));
                                jarray.put(o);
                            }
                            rs.close();
                            out.println(json.toString());

                        } catch (Exception ex) {
                            out.println(ex);
                        }

                    }
                },
        CLOSEOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("text/plain");
                        PrintWriter out = resp.getWriter();
                        int offerid = Integer.parseInt(req.getParameter("id"));

                        Connection conn;
                        try {
                            conn = DbUtils.getConnection();
                            out.print(closeOffer(conn, offerid));

                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "upload failed", ex);
                            out.println(ex);
                        }
                    }
                },
        CLOSEDEMAND {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("text/plain");
                        PrintWriter out = resp.getWriter();
                        int id = Integer.parseInt(req.getParameter("id"));

                        Connection conn;
                        try {
                            conn = DbUtils.getConnection();
                            out.print(closeDemand(conn, id));

                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "action failed", ex);
                            out.println(ex);
                        }
                    }
                },
        NEWDEMAND {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("text/plain");
                        PrintWriter out = resp.getWriter();
                        String name = req.getParameter("name");
                        Connection conn;

                        Knihovna kn = LoggedController.knihovna(req);
                        int idKnihovna = 0;
                        if (kn != null) {
                            idKnihovna = kn.getId();
                        }
                        try {
                            conn = DbUtils.getConnection();
                            int id = 0;
                            if (DbUtils.isOracle(conn)) {
                                id = insertDemandOracle(conn, name, idKnihovna);
                            } else {
                                id = insertDemandPg(conn, name, idKnihovna);
                            }

                            Calendar now = Calendar.getInstance();
                            Calendar o = Calendar.getInstance();
                            o.add(Calendar.DATE, Options.getInstance().getInt("expirationDays", 35));
                            JSONObject j = new JSONObject();
                            j.put("id", Integer.toString(id));
                            j.put("nazev", name);
                            j.put("knihovna", idKnihovna);
                            j.put("closed", false);
                            j.put("date", sdf.format(o.getTime()));
                            j.put("expires", sdf.format(o.getTime()));
                            j.put("expired", !o.after(now));

                            out.println(j.toString());

                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "upload failed", ex);
                            out.println(ex);
                        }
                    }
                },
        NEWOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("text/plain");
                        PrintWriter out = resp.getWriter();
                        String name = req.getParameter("offerName");
                        Connection conn;

                        Knihovna kn = LoggedController.knihovna(req);
                        int idKnihovna = 0;
                        try {
                            if (kn != null) {
                                idKnihovna = kn.getId();
                                conn = DbUtils.getConnection();
                                int idOffer = 0;
                                if (DbUtils.isOracle(conn)) {
                                    idOffer = insertOfferOracle(conn, name, idKnihovna, null);
                                } else {
                                    idOffer = insertOfferPg(conn, name, idKnihovna, null);
                                }

                                Calendar now = Calendar.getInstance();
                                JSONObject j = offerJSON(now.getTime(),
                                        Integer.toString(idOffer),
                                        name,
                                        kn.getCode(),
                                        false);

                                out.println(j.toString());
                            }

                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "upload failed", ex);
                            out.println(ex);
                        }
                    }
                },
        OFFERTOJSON {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("text/plain");
                        PrintWriter out = resp.getWriter();

                        Map<String, String> map = new HashMap<String, String>();

                        map.put("isbn", req.getParameter("isbn"));

                        map.put("issn", req.getParameter("issn"));
                        map.put("ccnb", req.getParameter("ccnb"));
                        map.put("245a", req.getParameter("titul"));
                        map.put("245n", req.getParameter("f245n"));
                        map.put("245p", req.getParameter("f245p"));
                        map.put("250a", req.getParameter("f250a"));
                        map.put("100a", req.getParameter("f100a"));
                        map.put("110a", req.getParameter("f110a"));
                        map.put("111a", req.getParameter("f111a"));
                        map.put("260a", req.getParameter("f260"));

                        JSONObject slouceni = Slouceni.fromMap(map);
                        out.println(slouceni);

                    }
                },
        IMPORTTOOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        //resp.setContentType("application/json");
                        resp.setContentType("text/plain");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        /// Create a factory for disk-based file items
                        FileItemFactory factory = new DiskFileItemFactory();

                        // Create a new file upload handler
                        ServletFileUpload upload = new ServletFileUpload(factory);

                        // Parse the request
                        List /* FileItem */ items = upload.parseRequest(req);

                        Iterator iter = items.iterator();

                        int idOffer = 0;
                        String format = "CSV";
                        while (iter.hasNext()) {
                            FileItem item = (FileItem) iter.next();

                            if (item.isFormField()) {
                                LOGGER.log(Level.INFO, "------ {0} param value : {1}", new Object[]{item.getFieldName(), item.getString()});
                                if (item.getFieldName().equals("id")) {
                                    idOffer = Integer.parseInt(item.getString());
                                } else if (item.getFieldName().equals("fileFormat")) {
                                    format = item.getString();
                                }
                            }
                        }
                        if (idOffer == 0) {
                            json.put("error", "nabidka ne platna");
                        } else {
                            iter = items.iterator();
                            while (iter.hasNext()) {
                                FileItem item = (FileItem) iter.next();
                                if (item.isFormField()) {
                                    continue;
                                }
                                InputStream uploadedStream = item.getInputStream();
                                byte[] bytes = IOUtils.toByteArray(uploadedStream);
                                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

                                //uploadedStream.mark(uploadedStream.available());
                                Connection conn;

                                Knihovna kn = LoggedController.knihovna(req);
                                int idKnihovna = 0;
                                try {
                                    if (kn != null) {

                                        idKnihovna = kn.getId();
                                        conn = DbUtils.getConnection();
                                        if (format.equals("ALEPH")) {
                                            TransformerFactory tfactory = TransformerFactory.newInstance();
                                            StreamSource xslt = new StreamSource(new File(Options.getInstance().getString("alephXSL", "aleph_to_csv.xsl")));
                                            StringWriter sw = new StringWriter();
                                            StreamResult destStream = new StreamResult(sw);
                                            Transformer transformer = tfactory.newTransformer(xslt);
                                            transformer.transform(new StreamSource(bais), destStream);

                                            //json.put("cvs", sw.toString());
                                            //out.println(sw.toString());
                                            processStream(conn, new StringReader(sw.toString()), idKnihovna, idOffer, json);
                                        } else {
                                            processStream(conn, bais, idKnihovna, idOffer, json);
                                        }
                                    } else {
                                        json.put("error", "nejste prihlasen");
                                    }
                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, "import to offer failed", ex);
                                    json.put("error", ex.toString());
                                }
                                uploadedStream.close();
                            }
                        }
                        out.println(json.toString());
                    }
                },
        IMPORTTOOFFEROLD {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        /// Create a factory for disk-based file items
                        FileItemFactory factory = new DiskFileItemFactory();

                        // Create a new file upload handler
                        ServletFileUpload upload = new ServletFileUpload(factory);

                        // Parse the request
                        List /* FileItem */ items = upload.parseRequest(req);

                        Iterator iter = items.iterator();

                        int idOffer = 0;
                        while (iter.hasNext()) {
                            FileItem item = (FileItem) iter.next();

                            if (item.isFormField()) {
                                LOGGER.log(Level.INFO, "------ {0} param value : {1}", new Object[]{item.getFieldName(), item.getString()});
                                if (item.getFieldName().equals("id")) {
                                    idOffer = Integer.parseInt(item.getString());
                                }
                            }
                        }
                        if (idOffer == 0) {
                            json.put("error", "nabidka ne platna");
                        } else {
                            iter = items.iterator();
                            while (iter.hasNext()) {
                                FileItem item = (FileItem) iter.next();
                                if (item.isFormField()) {
                                    continue;
                                }
                                InputStream uploadedStream = item.getInputStream();
                                byte[] bytes = IOUtils.toByteArray(uploadedStream);
                                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

                                //uploadedStream.mark(uploadedStream.available());
                                Connection conn;

                                Knihovna kn = LoggedController.knihovna(req);
                                int idKnihovna = 0;
                                try {
                                    if (kn != null) {

                                        idKnihovna = kn.getId();
                                        conn = DbUtils.getConnection();
                                        processStream(conn, bais, idKnihovna, idOffer, json);

                                    } else {
                                        json.put("error", "nejste prihlasen");
                                    }
                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, "import to offer failed", ex);
                                    json.put("error", ex.toString());
                                }
                                uploadedStream.close();
                            }
                        }
                        out.println(json.toString());
                    }
                },
        ADDFORMTOOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        int idOffer = Integer.parseInt(req.getParameter("id"));

                        Connection conn;

                        Knihovna kn = LoggedController.knihovna(req);

                        try {
                            if (kn != null) {
                                int idKnihovna = kn.getId();

                                conn = DbUtils.getConnection();
                                Map<String, String> parts = new HashMap<String, String>();
                                boolean exists = false;

                                //String[] parts = new String[11];
                                parts.put("isbn", req.getParameter("isbn"));
                                parts.put("issn", req.getParameter("issn"));
                                parts.put("ccnb", req.getParameter("ccnb"));
                                parts.put("245a", req.getParameter("titul"));
                                parts.put("245n", req.getParameter("f245n"));
                                parts.put("245p", req.getParameter("f245p"));
                                parts.put("250a", req.getParameter("f250a"));
                                parts.put("100a", req.getParameter("f100a"));
                                parts.put("110a", req.getParameter("f110a"));
                                parts.put("111a", req.getParameter("f111a"));
                                parts.put("260a", req.getParameter("f260"));
                                parts.put("cena", req.getParameter("cena"));
                                parts.put("comment", req.getParameter("comment"));

                                JSONObject slouceni = Slouceni.fromMap(parts);
                                String docCode = slouceni.getString("docCode");

                                exists = Storage.docExistsByCode(docCode);

                                JSONObject fields = new JSONObject(parts);
                                int newid = insertNabidka(conn, idKnihovna, null, null, docCode, idOffer, fields.toString());

                                json = jsonZaznamOffer(newid,
                                        docCode,
                                        req.getParameter("titul"),
                                        null,
                                        null,
                                        kn.getCode(),
                                        null,
                                        fields);
                                if (exists) {
                                    json.put("message", "Nabidka pridana. Generovany kod: " + docCode + " uz existuje");
                                } else {
                                    json.put("message", "Nabidka pridana. Kod: " + docCode);
                                }
                            } else {
                                json.put("error", "nejste prihlasen");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "add to offer failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        ADDDOCTOOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        int idOffer = Integer.parseInt(req.getParameter("id"));
                        String zaznam_id = req.getParameter("zaznam");
                        String exemplar_id = req.getParameter("ex");
                        String docCode = req.getParameter("docCode");

                        Connection conn;

                        Knihovna kn = LoggedController.knihovna(req);

                        try {
                            if (kn != null) {
                                int idKnihovna = kn.getId();

                                conn = DbUtils.getConnection();

                                boolean exists = false;

                                JSONObject fields = new JSONObject();
                                fields.put("comment", req.getParameter("comment"));
                                fields.put("cena", req.getParameter("cena"));
                                int newid = insertNabidka(conn, idKnihovna, zaznam_id, exemplar_id, docCode, idOffer, fields.toString());

                                json = jsonZaznamOffer(newid,
                                        docCode,
                                        req.getParameter("titul"),
                                        zaznam_id,
                                        exemplar_id,
                                        kn.getCode(),
                                        null,
                                        fields);
                                if (exists) {

                                    json.put("message", "Nabidka pridana. Generovany kod: " + docCode + " uz existuje");
                                } else {
                                    json.put("message", "Nabidka pridana. Kod: " + docCode);
                                }
                            } else {
                                json.put("error", "nejste prihlasen");
                            }
                        } catch (SQLException ex) {
                            LOGGER.log(Level.SEVERE, "add to offer failed", ex);
                            json.put("error", ex.toString());
                            json.put("getSQLState", ex.getSQLState());
                        }
                        out.println(json.toString());
                    }
                },
        ADDTODEMAND {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        String zaznam_id = req.getParameter("zaznam");
                        String exemplar_id = req.getParameter("ex");
                        String docCode = req.getParameter("docCode");

                        Connection conn;

                        try {
                            Knihovna kn = LoggedController.knihovna(req);
                            if (kn != null) {
                                conn = DbUtils.getConnection();
                                Map<String, String> parts = new HashMap<String, String>();
                                boolean exists = false;
                                parts.put("comment", req.getParameter("comment"));

                                insertToDemand(conn, kn.getId(), zaznam_id, exemplar_id, docCode, (new JSONObject(parts)).toString());
                                String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                Indexer indexer = new Indexer(f);
                                indexer.indexDemand(kn.getCode(),
                                        docCode,
                                        zaznam_id,
                                        exemplar_id);
                                
                                if (exists) {
                                    json.put("message", "Poptavka pridana. Generovany kod: " + docCode + " uz existuje");
                                } else {
                                    json.put("message", "Poptavka pridana. Kod: " + docCode);
                                }
                                
                            } else {
                                json.put("error", "rights.notlogged");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "add to demand failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REMOVEZAZNAMOFFER {

                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        Connection conn;

                        try {
                            Knihovna kn = LoggedController.knihovna(req);
                            if (kn != null) {
                                if (kn.hasRole(DbUtils.Roles.LIB)) {
                                    int ZaznamOffer_id = Integer.parseInt(req.getParameter("ZaznamOffer_id"));
                                    conn = DbUtils.getConnection();
                                    removeZaznamOffer(conn, kn.getId(), ZaznamOffer_id);
                                    json.put("message", "Zaznam z nabidky odstranen");
                                } else {
                                    json.put("error", "rights.insuficient");
                                }
                            } else {
                                json.put("error", "rights.notlogged");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "remove offer failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REMOVEDEMAND {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();
                        Connection conn;
                        try {
                            Knihovna kn = LoggedController.knihovna(req);

                            if (kn != null) {
                                conn = DbUtils.getConnection();
                                removeDemand(conn,
                                        Integer.parseInt(req.getParameter("id")),
                                        kn.getId());
                                json.put("message", "Poptavka odstranena");
                            } else {
                                json.put("error", "rights.notlogged");
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "remove demand failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        DOWNLOADOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        //resp.setContentType("text/plain");
                        String id = req.getParameter("id");
                        Connection conn;
                        try {
                            conn = DbUtils.getConnection();

                            String sql = "select * from OFFER where offer_id=?";
                            PreparedStatement ps = conn.prepareStatement(sql);
                            ps.setInt(1, new Integer(id));
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                byte[] bytes = rs.getBytes("bdata");
                                resp.getOutputStream().write(bytes);
                            }
                            rs.close();

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            out.println(ex);
                        }

                    }
                },
        INFO {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("text/plain");
                        String id = req.getParameter("id");
                        Connection conn;
                        try {
                            PrintWriter out = resp.getWriter();
                            conn = DbUtils.getConnection();
                            DatabaseMetaData p = conn.getMetaData();
                            out.println(p.getDatabaseProductName());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            out.println(ex);
                        }

                    }
                },
        GETDEMANDS {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("text/plain");
                        try {
                            PrintWriter out = resp.getWriter();
                            out.println(getDemands().toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            out.println(ex);
                        }
                    }
                },
        GETOFFERS {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            out.println(getOffers().toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }

                    }
                },
        GETOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("text/plain");
                        try {
                            PrintWriter out = resp.getWriter();
                            String id = req.getParameter("id");
                            out.println(getOfferRows(id).toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            out.println(ex);
                        }

                    }
                },
        GETPROTOCOL {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            int id = Integer.parseInt(req.getParameter("id"));
                            out.println(getProtocol(id).toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            out.println(ex);
                        }

                    }
                },
        REGENERATECODES {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            out.println(DbUtils.regenerateCodes());
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "remove demand failed", ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());

                    }
                },
        GETSOURCES {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            JSONObject ret = new JSONObject();
                            Connection conn = DbUtils.getConnection();
                            String sql = "select * from ZDROJ";
                            PreparedStatement ps = conn.prepareStatement(sql);

                            ResultSet rs = ps.executeQuery();
                            while (rs.next()) {
                                JSONObject json = new JSONObject();
                                json.put("name", rs.getString("nazev"));
                                json.put("conf", rs.getString("parametry"));
                                json.put("cron", rs.getString("cron"));
                                ret.put(rs.getString("nazev"), json);
                            }
                            rs.close();
                            out.println(ret.toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }

                    }
                },
        SAVESOURCE {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            JSONObject ret = new JSONObject();
                            Connection conn = DbUtils.getConnection();
                            String sql = "update ZDROJ set cron=? where nazev=?";
                            PreparedStatement ps = conn.prepareStatement(sql);
                            ps.setString(1, req.getParameter("cron"));
                            ps.setString(2, req.getParameter("name"));

                            ps.executeUpdate();

                            VDKScheduler.addJob(req.getParameter("name"),
                                    req.getParameter("cron"),
                                    req.getParameter("conf"));

                            ret.put("message", "Source saved.");
                            out.println(ret.toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }

                    }
                },
        GETUSERS {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            ArrayList<Knihovna> kns = getUsers();

                            JSONObject json = new JSONObject();
                            for (Knihovna k : kns) {
                                json.put(k.getCode(), k.getJson());
                            }
                            out.println(json.toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }

                    }
                },
        SAVEUSER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            Knihovna kn = LoggedController.knihovna(req);
                            if (kn == null) {
                                json.put("error", "rights.notlogged");
                            } else {
                                if (kn.hasRole(DbUtils.Roles.ADMIN)) {

                                    String code = req.getParameter("code");
                                    Knihovna uk = new Knihovna(code);
                                    uk.setNazev(req.getParameter("name"));
                                    uk.setEmail(req.getParameter("email"));
                                    uk.setSigla(req.getParameter("sigla"));
                                    uk.setAdresa(req.getParameter("adresa"));
                                    uk.setTelefon(req.getParameter("telefon"));
                                    uk.setPriorita(Integer.parseInt(req.getParameter("priorita")));
                                    uk.saveToDb();

                                    json = uk.getJson();
                                } else {
                                    json.put("error", "rights.insuficient");
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                            json.put("error", ex.toString());
                        }

                        out.println(json.toString());

                    }
                },
        SCRIPT {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
//                            Knihovna kn = LoggedController.knihovna(req);
//                            if (kn == null) {
//                                json.put("error", "rights.notlogged");
//                            } else {
//                                if (kn.hasRole(DbUtils.Roles.ADMIN)) {

                            Connection conn = DbUtils.getConnection();
                            String sql = req.getParameter("s");
//                                    sql = "alter table zaznamoffer add pr_timestamp TIMESTAMP";
                            PreparedStatement ps = conn.prepareStatement(sql);
//                                    ps.execute();
//                                    sql = "alter table zaznamoffer add pr_knihovna INT";
//                                    ps = conn.prepareStatement(sql);
//                                    ps.execute();
//                                    ps = conn.prepareStatement(sql);
                            ps.execute();
//                                    sql = "alter table knihovna add sigla VARCHAR(10)";
//                                    ps = conn.prepareStatement(sql);
//                                    ps.execute();
//                                    sql = "alter table knihovna add adresa VARCHAR(255)";
//                                    ps = conn.prepareStatement(sql);
//                                    ps.execute();

//                                } else {
//                                    json.put("error", "rights.insuficient");
//                                }
//                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                            json.put("error", ex.toString());
                        }

                        out.println(json.toString());

                    }
                },
        GETCONF {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            Knihovna kn = LoggedController.knihovna(req);
                            if (kn == null) {
                                out.println("{\"error\": \"rights.notlogged\"}");
                            } else {
                                if (kn.hasRole(DbUtils.Roles.ADMIN)) {
                                    out.println(Options.getInstance().toString());
                                } else {
                                    out.println("{\"error\": \"rights.insuficient\"}");
                                }
                            }
                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }

                    }
                },
        SAVECONF {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            Knihovna kn = LoggedController.knihovna(req);
                            if (kn == null) {
                                out.println("{\"error\": \"rights.notlogged\"}");
                            } else {
                                if (kn.hasRole(DbUtils.Roles.ADMIN)) {
                                    Options opts = Options.getInstance();
                                    JSONObject json = opts.asJSON();
                                    json.put("expirationDays", req.getParameter("exp"));
                                    json.put("admin.email", req.getParameter("email"));
                                    json.put("admin.email.offer.body", req.getParameter("emailBody"));
                                    opts.save();
                                    out.println(opts.toString());
                                } else {
                                    out.println("{\"error\": \"rights.insuficient\"}");
                                }
                            }
                        } catch (IOException | JSONException ex) {
                            PrintWriter out = resp.getWriter();
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }

                    }
                },
        GETROLES {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        try {
                            PrintWriter out = resp.getWriter();
                            ArrayList<String> rs = getRoles();

                            JSONArray json = new JSONArray();
                            for (String r : rs) {
                                json.put(r);
                            }
                            out.println(json.toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }

                    }
                };

        abstract void doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
