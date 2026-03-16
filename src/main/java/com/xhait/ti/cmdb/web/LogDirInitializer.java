package com.xhait.ti.cmdb.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ensures the log directory exists before Log4j2 attempts to create/write files.
 */
@WebListener
public class LogDirInitializer implements ServletContextListener {
	private static final Logger log = LogManager.getLogger(LogDirInitializer.class);

	private static final Path LOG_DIR = Path.of("D:\\log_apps");

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		log.debug("ENTER contextInitialized()");
		try {
			Files.createDirectories(LOG_DIR);
			log.info("Log directory ensured: {}", LOG_DIR.toAbsolutePath());
		} catch (IOException e) {
			log.error("Failed to create log directory {}. File logging may fail.", LOG_DIR.toAbsolutePath(), e);
		}
		log.debug("EXIT contextInitialized()");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		log.debug("ENTER contextDestroyed()");
		log.info("Application context destroyed");
		log.debug("EXIT contextDestroyed()");
	}
}
