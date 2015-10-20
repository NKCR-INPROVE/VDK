package cz.incad.vdk.client;

import cz.incad.vdkcommon.solr.IndexerQuery;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class SearchServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());
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
                Actions actionToDo = SearchServlet.Actions.valueOf(actionNameParam);
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

    enum Actions {
        BYQUERY {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json;charset=UTF-8");
                        PrintWriter out = resp.getWriter();
                        try {
                            String q = req.getParameter("q");
                            SolrQuery query = new SolrQuery();
                            
                            query.setQuery(q);
                            query.set("q.op", "AND");
                            if (req.getParameterValues("fq[]") != null) {
                                for (String fq : req.getParameterValues("fq[]")) {
                                    query.addFilterQuery(fq);
                                }
                            }
                            if (req.getParameterValues("fq") != null) {
                                for (String fq : req.getParameterValues("fq")) {
                                    query.addFilterQuery(fq);
                                }
                            }
                            JSONObject json = new JSONObject(IndexerQuery.json(query));
                            out.println(json.toString());
                            
                        } catch (Exception ex) {
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }
                    }
                },

        BYFIELD {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json;charset=UTF-8");
                        PrintWriter out = resp.getWriter();
                        try {
                            String field = req.getParameter("field");
                            String value = req.getParameter("value");
                            SolrQuery query = new SolrQuery();
                            
                            query.setQuery(field + ":\"" + value + "\"");
                            //query.addFilterQuery(field + ":\"" + value + "\"");
                            JSONObject json = new JSONObject(IndexerQuery.json(query));
                            out.println(json.toString());
                        } catch (Exception ex) {
                            JSONObject json = new JSONObject();
                            json.put("error", ex.toString());
                            out.println(json.toString());
                        }
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
