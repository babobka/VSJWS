package ru.babobka.vsjsw.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleLogger {

	private final Logger logger;

	public SimpleLogger(Class<?> clazz, String runningFolder, String prefix) throws IOException {
		logger = LogBuilder.build(clazz, runningFolder, prefix);
	}

	public void log(Level level, String message) {
		logger.log(level, message);

	}

	public void log(Level level, Exception e) {
		logger.log(level, getStringFromException(e));
	}

	public void log(String message) {
		logger.log(Level.INFO, message);
	}

	public void log(String message, Exception e) {
		logger.log(Level.SEVERE, message + "\t" + getStringFromException(e));
	}

	public void log(Exception e) {
		logger.log(Level.SEVERE, getStringFromException(e));
	}

	private static String getStringFromException(Exception ex) {
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

}
