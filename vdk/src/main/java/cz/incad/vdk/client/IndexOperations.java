package cz.incad.vdk.client;

import cz.incad.vdkcommon.DbUtils;
import cz.incad.vdkcommon.Knihovna;
import cz.incad.vdkcommon.SolrIndexerCommiter;
import cz.incad.vdkcommon.solr.Indexer;
import cz.incad.vdkcommon.solr.IndexerQuery;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class IndexOperations extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(IndexOperations.class.getName());
    public static final String ACTION_NAME = "action";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String actionNameParam = req.getParameter(ACTION_NAME);
            if (actionNameParam != null) {
                Actions actionToDo = IndexOperations.Actions.valueOf(actionNameParam);
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

    private static void removeAllDemands() throws Exception {
        SolrQuery query = new SolrQuery("poptavka:[* TO *]");
        query.addField("code");
        SolrDocumentList docs = IndexerQuery.query(query);
        Iterator<SolrDocument> iter = docs.iterator();
        while (iter.hasNext()) {
            StringBuilder sb = new StringBuilder();

            SolrDocument resultDoc = iter.next();
            String docCode = (String) resultDoc.getFieldValue("code");
            sb.append("<add><doc>");
            sb.append("<field name=\"code\">")
                    .append(docCode)
                    .append("</field>");
            sb.append("<field name=\"md5\">")
                    .append(docCode)
                    .append("</field>");

            sb.append("<field name=\"poptavka\" update=\"set\" null=\"true\" />");
            sb.append("<field name=\"poptavka_ext\" update=\"set\" null=\"true\" />");
            sb.append("</doc></add>");
            SolrIndexerCommiter.postData(sb.toString());
            SolrIndexerCommiter.postData("<commit/>");
        }
    }

    enum Actions {

        INDEXWANTED {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                            Indexer indexer = new Indexer(f);
                            indexer.indexWanted(Integer.parseInt(req.getParameter("id")));
                            json.put("message", "Reakce pridana.");
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REMOVEALLWANTED {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                            Indexer indexer = new Indexer(f);
                            indexer.removeAllWanted();
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        INDEXALLWANTED {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {

                            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                            Indexer indexer = new Indexer(f);
                            indexer.removeAllWanted();
                            indexer.indexAllWanted();
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        INDEXALLOFFERS {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            String f = System.getProperty("user.home") + File.separator
                            + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                            Indexer indexer = new Indexer(f);
                            indexer.removeAllOffers();
                            indexer.indexAllOffers();
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        INDEXOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                            Indexer indexer = new Indexer(f);
                            indexer.indexOffer(Integer.parseInt(req.getParameter("id")));
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REMOVEOFFER {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                            Indexer indexer = new Indexer(f);
                            indexer.removeOffer(Integer.parseInt(req.getParameter("id")));
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REMOVEALLOFFERS {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                            Indexer indexer = new Indexer(f);
                            indexer.removeAllOffers();
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        ADDDEMAND {
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
                                String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                Indexer indexer = new Indexer(f);
                                indexer.indexDemand(kn.getCode(),
                                        req.getParameter("docCode"),
                                        req.getParameter("zaznam"),
                                        req.getParameter("ex"));
                            }
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REMOVEDEMAND {
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
                                String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                Indexer indexer = new Indexer(f);
                                indexer.removeDemand(kn.getCode(),
                                        req.getParameter("docCode"),
                                        req.getParameter("zaznam"),
                                        req.getParameter("ex"));
                            }
                        } catch (Exception ex) {
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        INDEXALLDEMANDS {
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
                                String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                Indexer indexer = new Indexer(f);
                                indexer.indexAllDemands();
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REMOVEALLDEMANDS {
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
                                String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                Indexer indexer = new Indexer(f);
                                indexer.removeAllDemands();
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REINDEXDOCS {
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
                                    String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                    Indexer indexer = new Indexer(f);
                                    indexer.reindex();
                                } else {
                                    json.put("error", "rights.insuficient");
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REINDEX {
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
                                    String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                    Indexer indexer = new Indexer(f);
                                    indexer.reindex();
//                                    indexAllOffers(DbUtils.getConnection());
//                                    indexAllDemands(DbUtils.getConnection());
//                                    indexAllWanted(DbUtils.getConnection());
                                } else {
                                    json.put("error", "rights.insuficient");
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                },
        REINDEXDOC {
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
                                    String f = System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "jobs" + File.separator + "indexer.json";
                                    Indexer indexer = new Indexer(f);

                                    indexer.reindexDocByIdentifier(req.getParameter("code"));
                                } else {
                                    json.put("error", "rights.insuficient");
                                }
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                            json.put("error", ex.toString());
                        }
                        out.println(json.toString());
                    }
                };

        abstract void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception;
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
