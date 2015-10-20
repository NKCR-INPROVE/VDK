package cz.incad.vdk.client;

import cz.incad.vdkcommon.DbUtils;
import cz.incad.vdkcommon.Knihovna;
import cz.incad.vdkcommon.VDKJobData;
import cz.incad.vdkcommon.oai.HarvesterJob;
import cz.incad.vdkcommon.oai.HarvesterJobData;
import cz.incad.vdkcommon.oai.OAIHarvester;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class HarvestOperations extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(HarvestOperations.class.getName());
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
                Actions actionToDo = HarvestOperations.Actions.valueOf(actionNameParam);
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

    private static boolean isLocalHost(HttpServletRequest req) throws ServletException {
        Set<String> localAddresses = new HashSet<String>();
        try {
            localAddresses.add(InetAddress.getLocalHost().getHostAddress());
            for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
                localAddresses.add(inetAddress.getHostAddress());
            }
            return localAddresses.contains(req.getRemoteAddr());
        } catch (IOException e) {
            throw new ServletException("Unable to lookup local addresses");
        }

    }

    enum Actions {

        DISKTODB {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            Knihovna kn = (Knihovna) req.getSession().getAttribute("knihovna");
                            if (kn == null && !isLocalHost(req)) {
                                json.put("error", "rights.notlogged");
                            } else {
                                if (isLocalHost(req) || kn.hasRole(DbUtils.Roles.ADMIN)) {

                                    JSONObject runParams = new JSONObject();
                                    if (req.getParameter("full") != null) {
                                        runParams.put("fullIndex", true);
                                    }
                                    if (req.getParameter("path") != null) {
                                        runParams.put("pathToData", true);
                                    }
                                    runParams.put("fromDisk", true);

                                    HarvesterJobData jobdata = new HarvesterJobData(new VDKJobData(req.getParameter("conf"), runParams));

                                    OAIHarvester oh = new OAIHarvester(jobdata);
                                    oh.harvest();

                                    if (req.getParameter("dontIndex") != null) {
                                        //jobdata.setDontIndex(true);
                                    }

                                    json.put("message", "harvest finished.");
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
        FULL {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            Knihovna kn = (Knihovna) req.getSession().getAttribute("knihovna");
                            if (kn == null && !isLocalHost(req)) {
                                json.put("error", "rights.notlogged");
                            } else {
                                if (isLocalHost(req) || kn.hasRole(DbUtils.Roles.ADMIN)) {
//                                    OAIHarvester oh = new OAIHarvester(req.getParameter("conf"));
//                                    if(req.getParameter("todisk") != null){
//                                        oh.setSaveToDisk(true);
//                                    }
//                                    
//                                    oh.setFullIndex(true);
//                                    oh.harvest();
                                    HarvesterJob hj = new HarvesterJob();
                                    JSONObject runParams = new JSONObject();
                                    if (req.getParameter("todisk") != null) {
                                        runParams.put("saveToDisk", true);
                                    }
                                    runParams.put("fullIndex", true);
                                    HarvesterJobData jobdata = new HarvesterJobData(new VDKJobData(req.getParameter("conf"), runParams));
//                                    new HarvesterJobData(req.getParameter("conf"));

                                    hj.harvestScheduled(jobdata);

                                    json.put("message", "harvest finished.");
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
        RESUMPTION {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            Knihovna kn = (Knihovna) req.getSession().getAttribute("knihovna");
                            if (kn == null && !isLocalHost(req)) {
                                json.put("error", "rights.notlogged");
                            } else {
                                if (isLocalHost(req) || kn.hasRole(DbUtils.Roles.ADMIN)) {

                                    JSONObject runParams = new JSONObject();
                                    runParams.put("saveToDisk", true);
                                    runParams.put("resumptionToken", req.getParameter("token"));

                                    HarvesterJobData jobdata = new HarvesterJobData(new VDKJobData(req.getParameter("conf"), runParams));

                                    OAIHarvester oh = new OAIHarvester(jobdata);
                                    oh.harvest();

                                    json.put("message", "harvest finished.");
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
        UPDATE {
                    @Override
                    void doPerform(HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        JSONObject json = new JSONObject();
                        try {
                            Knihovna kn = (Knihovna) req.getSession().getAttribute("knihovna");
                            if (kn == null) {
                                json.put("error", "rights.notlogged");
                            } else {
                                if (kn.hasRole(DbUtils.Roles.ADMIN)) {

                                    HarvesterJob hj = new HarvesterJob();

                                    JSONObject runParams = new JSONObject();
                                    if (req.getParameter("todisk") != null) {
                                        runParams.put("saveToDisk", true);
                                    }
                                    HarvesterJobData jobdata = new HarvesterJobData(new VDKJobData(req.getParameter("conf"), runParams));

                                    hj.harvestScheduled(jobdata);
                                    json.put("message", "harvest scheduled.");
                                } else {
                                    json.put("error", "rights.insuficient");
                                }
                            }
                        } catch (Exception ex) {
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
