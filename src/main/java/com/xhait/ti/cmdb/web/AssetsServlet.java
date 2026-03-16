package com.xhait.ti.cmdb.web;

import java.io.IOException;

import com.xhait.ti.cmdb.assets.AssetsDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Demo servlet for the Assets collection.
 *
 * GET  /assets  -> list latest assets
 * POST /assets  -> create a new asset
 */
public class AssetsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LogManager.getLogger(AssetsServlet.class);

	private final AssetsDao dao = new AssetsDao();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("ENTER doGet uri={} query={}", req.getRequestURI(), req.getQueryString());
		try {
			log.info("Listing latest assets");
			req.setAttribute("assets", dao.listLatest(50));
			req.getRequestDispatcher("/WEB-INF/jsp/assets.jsp").forward(req, resp);
			log.debug("Forwarded to assets.jsp");
		} catch (RuntimeException ex) {
			log.error("Unhandled error in doGet", ex);
			sendConfigError(resp, ex);
		} finally {
			log.debug("EXIT doGet status={}", resp.getStatus());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("ENTER doPost uri={}", req.getRequestURI());
		try {
			String name = req.getParameter("name");
			String type = req.getParameter("type");
			String owner = req.getParameter("owner");

			log.info("Creating asset name='{}' type='{}' owner='{}'", name, type, owner);
			dao.insert(name, type, owner);
			resp.sendRedirect(req.getContextPath() + "/assets");
			log.debug("Redirected to /assets");
		} catch (RuntimeException ex) {
			log.error("Unhandled error in doPost", ex);
			sendConfigError(resp, ex);
		} finally {
			log.debug("EXIT doPost status={}", resp.getStatus());
		}
	}

	private static void sendConfigError(HttpServletResponse resp, RuntimeException ex) throws IOException {
		log.debug("ENTER sendConfigError message={}", ex.getMessage());
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		resp.setContentType("text/html; charset=UTF-8");
		resp.getWriter().println("<!doctype html><html><head><meta charset='utf-8'><title>MongoDB not configured</title></head><body>");
		resp.getWriter().println("<h1>MongoDB connection isn't configured</h1>");
		resp.getWriter().println("<p>This demo expects the following settings:</p>");
		resp.getWriter().println("<ul>" +
				"<li><code>MONGODB_URI</code> (e.g. <code>mongodb://localhost:27017</code>)</li>" +
				"<li><code>MONGODB_DB</code> (e.g. <code>cmdb</code>)</li>" +
				"</ul>");
		resp.getWriter().println("<p>Error: <code>" + escape(ex.getMessage()) + "</code></p>");
		resp.getWriter().println("</body></html>");
		log.debug("EXIT sendConfigError()");
	}

	private static String escape(String s) {
		log.debug("ENTER escape()");
		if (s == null) {
			log.debug("EXIT escape -> empty");
			return "";
		}
		String out = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
		log.debug("EXIT escape()");
		return out;
	}
}