package ru.babobka.vsjsw.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Created by dolgopolov.a on 12.01.16.
 */
class LogBuilder {

	private LogBuilder() {

	}

	static Logger build(Class<?> clazz, String runningFolder, String prefix) throws IOException {

		Logger logger = Logger.getLogger(clazz.getName());
		logger.setUseParentHandlers(false);
		File folder = new File(runningFolder + File.separator);
		if (!folder.exists()) {
			folder.mkdir();
		}
		String fileName = folder.getAbsolutePath() + File.separator + prefix + "_" + System.currentTimeMillis()
				+ ".log";
		FileHandler fh = new FileHandler(fileName);
		Handler ch = new ConsoleHandler();
		LogFormatter formatter = new LogFormatter();
		ch.setFormatter(formatter);
		logger.addHandler(ch);
		fh.setFormatter(formatter);
		logger.addHandler(fh);
		return logger;

	}
}
