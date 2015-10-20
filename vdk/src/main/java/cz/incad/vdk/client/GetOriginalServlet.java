/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdk.client;

import cz.incad.vdkcommon.DbUtils;
import cz.incad.vdkcommon.solr.IndexerQuery;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author alberto
 */
public class GetOriginalServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(GetOriginalServlet.class.getName());

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String id = request.getParameter("id");
            String path = request.getParameter("path");
            String signatura = request.getParameter("signatura");
            String carkod = request.getParameter("carkod");
            if (signatura != null && !signatura.equals("")) {

                ArrayList<String> fq = new ArrayList<String>();
                String fqp = request.getParameter("format");
                if (fqp != null && !"".equals(fqp)) {
                    fq.add("format:" + fqp);
                }
                String zdroj = request.getParameter("zdroj");
//                if (zdroj != null && !"".equals(zdroj)) {
//                    fq.add("zdroj:\"" + zdroj + "\"");
//                }
                byIndexField("signatura", signatura, out, (String[]) fq.toArray(new String[fq.size()]), zdroj);
            } else if (carkod != null && !carkod.equals("")) {

                ArrayList<String> fq = new ArrayList<String>();
                String fqp = request.getParameter("format");
                if (fqp != null && !"".equals(fqp)) {
                    fq.add("format:" + fqp);
                }
                String zdroj = request.getParameter("zdroj");
                //if (zdroj != null && !"".equals(zdroj)) {
                //    fq.add("zdroj:\"" + zdroj + "\"");
                //}
                byIndexField("carkod", carkod, out, (String[]) fq.toArray(new String[fq.size()]), zdroj);
            } else if (path == null || path.equals("")) {
                if(request.getParameter("wt") != null && request.getParameter("wt").equals("xml")){
                    response.setContentType("text/xml;charset=UTF-8");
                    fromDb(id, out, false);
                }else{
                    response.setContentType("text/html;charset=UTF-8");
                    fromDb(id, out, true);
                }
            } else {
                Transformer transformer;

                TransformerFactory tfactory = TransformerFactory.newInstance();

                InputStream stylesheet = this.getServletContext().getResourceAsStream("/WEB-INF/original.xsl");
                StreamSource xslt = new StreamSource(stylesheet);
                transformer = tfactory.newTransformer(xslt);

                transformer.setParameter("id", id);
                StreamResult destStream = new StreamResult(new StringWriter());
                transformer.transform(new StreamSource(new File(path)), destStream);
                StringWriter sw = (StringWriter) destStream.getWriter();
                out.print(sw.toString());
            }
        } catch (Exception ex) {

            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString());
            out.print(ex.toString());
        } finally {
            out.close();
        }
    }

    private void fromDb(String id, PrintWriter out, boolean html) throws SQLException, TransformerException {
        if (html) {
            Transformer transformer;

            TransformerFactory tfactory = TransformerFactory.newInstance();

            InputStream stylesheet = this.getServletContext().getResourceAsStream("/WEB-INF/marc.xsl");
            StreamSource xslt = new StreamSource(stylesheet);
            transformer = tfactory.newTransformer(xslt);
            StreamResult destStream = new StreamResult(new StringWriter());
            transformer.transform(new StreamSource(new StringReader(DbUtils.getXml(id))), destStream);
            StringWriter sw = (StringWriter) destStream.getWriter();
            out.print(sw.toString());
        } else {
            String xml = DbUtils.getXml(id);
            if(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")){
                xml = xml.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length());
            }
            out.println(xml);
        }
    }
    
    private boolean matchIdZdroj(String id, String zdroj){
        return id.contains(zdroj);
    }
    

    private void byIndexField(String field, String value, PrintWriter out, String[] fq, String zdroj) throws SQLException, SolrServerException, IOException, TransformerException {
        LOGGER.log(Level.INFO, "getting by " + field);
        SolrDocumentList docs = IndexerQuery.queryOneField(field + ":\"" + value + "\"", new String[]{"id", "zdroj"}, fq);

        out.println("<recordList>");
        Iterator<SolrDocument> iter = docs.iterator();
        while (iter.hasNext()) {

            SolrDocument resultDoc = iter.next();
            Collection<Object> ids = resultDoc.getFieldValues("id");

            Iterator<Object> it = ids.iterator();
            while (it.hasNext()) {
                String id = (String) it.next();
                if (zdroj == null || matchIdZdroj(id, zdroj)) {
                    LOGGER.log(Level.INFO, "getting xml for id: {0}", id);
                    fromDb(id, out, false);
                }
            }
        }
        out.println("</recordList>");

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
