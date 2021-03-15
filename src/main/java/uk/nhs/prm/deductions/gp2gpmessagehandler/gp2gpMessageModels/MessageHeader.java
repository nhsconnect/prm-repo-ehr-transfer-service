package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageHeader {
    @JacksonXmlProperty(localName = "Action", namespace = "eb")
    public String action;

    @JacksonXmlProperty(localName = "ConversationId", namespace = "eb")
    public String conversationId;

    @JacksonXmlProperty(localName = "MessageData", namespace = "eb")
    public MessageData messageData;
}
