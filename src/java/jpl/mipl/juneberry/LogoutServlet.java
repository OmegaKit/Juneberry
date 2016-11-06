package jpl.mipl.juneberry;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import the.treevotee.SimpleLogger;

/**
 * @author Xing
 */
public class LogoutServlet extends HttpServlet {

    private final SimpleLogger logger = SimpleLogger.getLogger(LogoutServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.logger.info("init");
    } 

    public void destroy() {
        this.logger.info("destroy");
        super.destroy();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        do_it(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        do_it(request, response);
    }

    private void do_it(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        request.getSession().invalidate();
        return;
    }
}
