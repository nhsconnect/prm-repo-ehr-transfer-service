package uk.nhs.prm.repo.ehrtransferservice.activemq;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ForceXercesParserSoLogbackDoesNotBlowUpWhenUsingSwiftMqClient implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
    }
}