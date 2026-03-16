package com.xhait.ti.cmdb.web;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Simple endpoint to confirm the app is deployed and serving requests. */
public class HealthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LogManager.getLogger(HealthServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.debug("ENTER doGet uri={}", req.getRequestURI());
		resp.setContentType("text/plain; charset=UTF-8");
		resp.getWriter().println("OK");
		resp.getWriter().println("contextPath=" + req.getContextPath());
		resp.getWriter().println("servletPath=" + req.getServletPath());
		log.debug("EXIT doGet status={}", resp.getStatus());
	}
}