package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class Gp2gpMessageHandlerApplication {
    //private static final String LOGGER_WITH_CUSTOM_LAYOUT = "LOGGER_WITH_CUSTOM_LAYOUT";
//	private static Logger logger = LogManager.getLogger("JSON_LAYOUT_APPENDER");
    private static final Logger logger = LogManager.getLogger("LOGGER_WITH_CUSTOM_LAYOUT");

    void toLogWithTheCustomLayout(Map<String, Object> message) {
        logger.info(new CustomMessage(message));
    }

    public static void main(String[] args) {
        logger.trace("This is a TRACE message.");
        logger.debug("This is a DEBUG message.");
        logger.info("This is an INFO message.");
        logger.warn("This is a WARN message.");
//        logger.error();

        SpringApplication.run(Gp2gpMessageHandlerApplication.class, args);
    }

}
