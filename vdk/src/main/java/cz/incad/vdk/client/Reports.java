package cz.incad.vdk.client;

import cz.incad.vdkcommon.DbUtils;
import cz.incad.vdkcommon.Knihovna;
import cz.incad.vdkcommon.Options;
import cz.incad.vdkcommon.solr.IndexerQuery;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONException;

public class Reports extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(Reports.class.getName());
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


    private static JSONArray wantedJSON(ResultSet rs) throws Exception {
        JSONArray ja = new JSONArray();
        while (rs.next()) {
            JSONObject j = new JSONObject();
            j.put("wanted_id", rs.getInt("wanted_id"));
            j.put("zaznamoffer", rs.getInt("zaznamoffer"));
            j.put("wanted", rs.getBoolean("wants"));
            j.put("knihovna", rs.getString("code"));
            j.put("date", rs.getString("update_timestamp"));
            ja.put(j);
        }
        return ja;
    }

    private static JSONArray getWantedById(Connection conn, int ZaznamOffer_id) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where zo.ZaznamOffer_id=? "
                + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, ZaznamOffer_id);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONArray getWanted(Connection conn) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer";
        PreparedStatement ps = conn.prepareStatement(sql);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONArray getLibraryWantedByCode(Connection conn, String docCode, int idKnihovna) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
                + "where zo.uniquecode=? and w.knihovna=? "
                + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer";
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
                + "and w.knihovna=k.knihovna_id and zo.zaznamoffer_id=w.zaznamoffer";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, idKnihovna);

        ResultSet rs = ps.executeQuery();
        JSONArray ja = wantedJSON(rs);
        rs.close();
        return ja;
    }

    private static JSONArray getWantedByCode(Connection conn, String docCode) throws Exception {
        String sql = "select w.zaznamoffer, w.wanted_id, w.wants, w.update_timestamp, k.code from WANTED w, KNIHOVNA k, ZAZNAMOFFER zo "
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
            JSONObject fields) throws Exception {

        JSONObject j = new JSONObject();
        j.put("ZaznamOffer_id", ZaznamOffer_id);
        j.put("uniqueCode", uniqueCode);
        if (title == null) {
            if (fields.has("245a")) {
                j.put("title", fields.getString("245a"));
            } else {
                SolrQuery query = new SolrQuery("code:" + uniqueCode);
                query.addField("title");
                SolrDocumentList docs = IndexerQuery.query(query);
                Iterator<SolrDocument> iter = docs.iterator();
                if (iter.hasNext()) {
                    SolrDocument resultDoc = iter.next();
                    j.put("title", resultDoc.getFirstValue("title"));
                }
            }
        } else {
            j.put("title", title);
        }
        if (zaznam != null) {
            j.put("zaznam", zaznam);
        }

        j.put("exemplar", exemplar);
        j.put("knihovna", knihovna);
        j.put("fields", fields);
        return j;
    }

    public static JSONObject getOffer(String id) throws Exception {

        Connection conn = null;
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
                        new JSONObject(rs.getString("fields")));

                j.put("wanted", getWantedById(conn, zoId));
                json.put(Integer.toString(zoId), j);
            }
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
        Connection conn = null;
        JSONObject json = new JSONObject();
        try {
            conn = DbUtils.getConnection();
            String sql = "select ZAZNAMDEMAND.*, KNIHOVNA.code from ZAZNAMDEMAND, KNIHOVNA where ZAZNAMDEMAND.knihovna=KNIHOVNA.knihovna_id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                json.put(rs.getString("zaznamdemand_id"), jsonZaznamDemand(rs));
            }
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
        Calendar now = Calendar.getInstance();
        Calendar o = Calendar.getInstance();
        o.setTime(offerDate);
        o.add(Calendar.DATE, Options.getInstance().getInt("expirationDays", 35));
        JSONObject j = new JSONObject();
        j.put("id", id);
        j.put("nazev", nazev);
        j.put("knihovna", knihovna);
        j.put("closed", closed);
        j.put("date", sdf.format(offerDate));
        j.put("expires", sdf.format(o.getTime()));
        j.put("expired", !o.after(now));

        return j;
    }

    public static JSONObject getOffers() throws Exception {

        Connection conn = null;
        JSONObject json = new JSONObject();
        try {
            conn = DbUtils.getConnection();
            String sql = "select OFFER.*, KNIHOVNA.code from OFFER, KNIHOVNA where OFFER.knihovna=KNIHOVNA.knihovna_id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Date offerDate = rs.getDate("update_timestamp");
                JSONObject j = offerJSON(offerDate,
                        rs.getString("offer_id"),
                        rs.getString("nazev"),
                        rs.getString("code"),
                        rs.getBoolean("closed"));

                json.put(rs.getString("offer_id"), j);
            }
        } catch (Exception ex) {
            json.put("error", ex);
        } 
        return json;
    }

    

    enum Actions {

        GETLIBRARYWANTED {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {

                        resp.setContentType("application/json");
                        JSONObject json = new JSONObject();
                        PrintWriter out = resp.getWriter();

                        Connection conn = null;

                        try {
                            Knihovna kn = (Knihovna) req.getSession().getAttribute("knihovna");
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

                        Connection conn = null;

                        try {
                            Knihovna kn = (Knihovna) req.getSession().getAttribute("knihovna");
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

                        Connection conn = null;

                        try {
                            //Knihovna kn = (Knihovna) req.getSession().getAttribute("knihovna");
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

                        Connection conn = null;

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
                            out.println(getOffer(id).toString());

                        } catch (Exception ex) {
                            PrintWriter out = resp.getWriter();
                            out.println(ex);
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
