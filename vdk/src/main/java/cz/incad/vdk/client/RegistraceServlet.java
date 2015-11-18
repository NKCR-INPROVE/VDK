/*
 * Copyright (C) 2015 alberto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.vdk.client;

import static cz.incad.vdk.client.Reports.LOGGER;
import cz.incad.vdkcommon.Options;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author alberto
 */
public class RegistraceServlet extends HttpServlet {

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
        resp.setContentType("text/plain");
                try {
                    PrintWriter out = resp.getWriter();
                    String body = "";

                        Options opts = Options.getInstance();
                        
                        String from = opts.getString("admin.email");
                        String to = opts.getString("admin.email");
            
                    try {
                        Properties properties = System.getProperties();
                        Session session = Session.getDefaultInstance(properties);

                        MimeMessage message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(from));
                        message.addRecipient(Message.RecipientType.TO,
                                new InternetAddress(to));

                        message.setSubject("Žádost o registraci");

                        
                    for (Object obj : req.getParameterMap().entrySet()) {
                        Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) obj;
                        if (entry.getValue() != null && entry.getValue().length > 0) {
                            body += entry.getKey() +  entry.getValue()[0] + "\n";
                        } else {
                            body += entry.getKey() +  entry.getValue() + "\n";
                        }
                    }
                        message.setText(body);

                        Transport.send(message);
                        out.println("Sent message successfully....");
                        LOGGER.fine("Sent message successfully....");
                    } catch (MessagingException ex) {
                        LOGGER.log(Level.SEVERE, "Error sending email to: {0}, from {1} ", new Object[]{to, from});
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                    

                } catch (Exception ex) {
                    PrintWriter out = resp.getWriter();
                    out.println(ex);
                }

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
