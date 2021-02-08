package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Gp2gpMessageHandlerApplication {
	private static final Logger logger = LogManager.getLogger(Gp2gpMessageHandlerApplication.class);

	public static void main(String[] args) {
		logger.trace("This is a TRACE message.");
		logger.debug("This is a DEBUG message.");
		logger.info("This is an INFO message.");
		logger.warn("This is a WARN message.");
		logger.error("You guessed it, an ERROR message.");

		SpringApplication.run(Gp2gpMessageHandlerApplication.class, args);
	}

}
