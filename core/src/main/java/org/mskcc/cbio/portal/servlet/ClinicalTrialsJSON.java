/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.ClinicalTrial;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ClinicalTrialsJSON extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        JSONArray table = new JSONArray();
        String keywordStr = request.getParameter("keywords");
        String study = request.getParameter("study");
        String studyTokens[] = study.trim().split(",");
        String showAll = request.getParameter("showall");
        boolean isShowAll = (showAll != null) && showAll.equals("1");

        // We are going to do an AND operation on these two sets to narrow the list down
        HashSet<ClinicalTrial> studyTrials = new HashSet<ClinicalTrial>();
        HashSet<ClinicalTrial> drugTrials = new HashSet<ClinicalTrial>();

        try {
            DaoClinicalTrial daoClinicalTrial = DaoClinicalTrial.getInstance();
            for (String studyToken : studyTokens) {
                if(studyToken.length() < 5) continue; // prevent overloads!
                List<ClinicalTrial> clinicalTrials = daoClinicalTrial.fuzzySearchClinicalTrials(studyToken);
                studyTrials.addAll(clinicalTrials);
            }

            for (String keyword : keywordStr.split(",")) {
                if(keyword.length() < 5) continue; // prevent overloads!
                List<ClinicalTrial> clinicalTrials = daoClinicalTrial.fuzzySearchClinicalTrials(keyword);
                drugTrials.addAll(clinicalTrials);
            }

            drugTrials.retainAll(studyTrials);
            for (ClinicalTrial clinicalTrial: drugTrials) {
                if(!isShowAll && !clinicalTrial.isActive()) continue;

                JSONArray aRow = new JSONArray();
                aRow.add(clinicalTrial.getId());
                aRow.add(clinicalTrial.getTitle());
                aRow.add(clinicalTrial.getStatus());
                aRow.add(clinicalTrial.getPhase());
                aRow.add(clinicalTrial.getLocation());
                aRow.add(clinicalTrial.getSecondaryId());
                table.add(aRow);
            }

        } catch (DaoException e) {
            throw new ServletException(e);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Searches clinical trials with given keywords";
    }
}