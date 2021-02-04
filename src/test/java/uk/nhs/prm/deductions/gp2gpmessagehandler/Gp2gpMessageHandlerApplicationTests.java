package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class Gp2gpMessageHandlerApplicationTests {
	@Autowired
	JmsTemplate jmsTemplate;

	@Value("${activemq.inboundQueue}")
	private String inboundQueue;

	@Value("${activemq.outboundQueue}")
	private String outboundQueue;

	@Value("${activemq.unhandledQueue}")
	private String unhandledQueue;


	String ehrRequest = "--e01d9133-5058-45e5-a884-9189f468c805\n" +
			"Content-Id:<ContentRoot>\n" +
			"Content-Type: text/xml; charset=UTF-8\n" +
			"\n" +
			"<soap:Envelope xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\" xmlns:hl7ebxml=\"urn:hl7-org:transport/ebxml/DSTUv1.0\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
			"    <soap:Header>\n" +
			"        <eb:MessageHeader eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
			"            <eb:From>\n" +
			"                <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">N82668-820670</eb:PartyId>\n" +
			"            </eb:From>\n" +
			"            <eb:To>\n" +
			"                <eb:PartyId eb:type=\"urn:nhs:names:partyType:ocs+serviceInstance\">B86041-822103</eb:PartyId>\n" +
			"            </eb:To>\n" +
			"            <eb:CPAId>1b09c9557a7794ff6fd2</eb:CPAId>\n" +
			"            <eb:ConversationId>DFF5321C-C6EA-468E-BBC2-B0E48000E071</eb:ConversationId>\n" +
			"            <eb:Service>urn:nhs:names:services:gp2gp</eb:Service>\n" +
			"            <eb:Action>RCMR_IN010000UK05</eb:Action>\n" +
			"            <eb:MessageData>\n" +
			"                <eb:MessageId>DFF5321C-C6EA-468E-BBC2-B0E48000E071</eb:MessageId>\n" +
			"                <eb:Timestamp>2020-11-16T17:13:38.682Z</eb:Timestamp>\n" +
			"                <eb:TimeToLive>2020-11-16T23:28:38.682Z</eb:TimeToLive>\n" +
			"            </eb:MessageData>\n" +
			"            <eb:DuplicateElimination/>\n" +
			"        </eb:MessageHeader>\n" +
			"        <eb:AckRequested eb:version=\"2.0\" soap:mustUnderstand=\"1\" soap:actor=\"urn:oasis:names:tc:ebxml-msg:actor:nextMSH\" eb:signed=\"false\"/>\n" +
			"    </soap:Header>\n" +
			"    <soap:Body>\n" +
			"        <eb:Manifest eb:version=\"2.0\" soap:mustUnderstand=\"1\">\n" +
			"            <eb:Reference xlink:href=\"cid:Content1@e-mis.com/EMISWeb/GP2GP2.2A\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
			"                <eb:Description xml:lang=\"en\">RCMR_IN010000UK05</eb:Description>\n" +
			"                <hl7ebxml:Payload style=\"HL7\" encoding=\"XML\" version=\"3.0\"/>\n" +
			"            </eb:Reference>\n" +
			"        </eb:Manifest>\n" +
			"    </soap:Body>\n" +
			"</soap:Envelope>\n" +
			"\n" +
			"--e01d9133-5058-45e5-a884-9189f468c805\n" +
			"Content-Id:<Content1@e-mis.com/EMISWeb/GP2GP2.2A>\n" +
			"Content-Transfer-Encoding: 8bit\n" +
			"Content-Type: application/xml; charset=UTF-8\n" +
			"\n" +
			"<RCMR_IN010000UK05 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" type=\"Message\" xmlns=\"urn:hl7-org:v3\">\n" +
			"    <id root=\"DFF5321C-C6EA-468E-BBC2-B0E48000E071\"/>\n" +
			"    <creationTime value=\"20201116171338\"/>\n" +
			"    <versionCode code=\"V3NPfIT3.1.10\"/>\n" +
			"    <interactionId root=\"2.16.840.1.113883.2.1.3.2.4.12\" extension=\"RCMR_IN010000UK05\"/>\n" +
			"    <processingCode code=\"P\"/>\n" +
			"    <processingModeCode code=\"T\"/>\n" +
			"    <acceptAckCode code=\"NE\"/>\n" +
			"    <communicationFunctionRcv type=\"CommunicationFunction\" typeCode=\"RCV\">\n" +
			"        <device type=\"Device\" classCode=\"DEV\" determinerCode=\"INSTANCE\">\n" +
			"            <id root=\"1.2.826.0.1285.0.2.0.107\" extension=\"200000001161\"/>\n" +
			"        </device>\n" +
			"    </communicationFunctionRcv>\n" +
			"    <communicationFunctionSnd type=\"CommunicationFunction\" typeCode=\"SND\">\n" +
			"        <device type=\"Device\" classCode=\"DEV\" determinerCode=\"INSTANCE\">\n" +
			"            <id root=\"1.2.826.0.1285.0.2.0.107\" extension=\"200000000205\"/>\n" +
			"        </device>\n" +
			"    </communicationFunctionSnd>\n" +
			"    <ControlActEvent type=\"ControlAct\" classCode=\"CACT\" moodCode=\"EVN\">\n" +
			"        <author1 type=\"Participation\" typeCode=\"AUT\">\n" +
			"            <AgentSystemSDS type=\"RoleHeir\" classCode=\"AGNT\">\n" +
			"                <agentSystemSDS type=\"Device\" classCode=\"DEV\" determinerCode=\"INSTANCE\">\n" +
			"                    <id root=\"1.2.826.0.1285.0.2.0.107\" extension=\"200000000205\"/>\n" +
			"                </agentSystemSDS>\n" +
			"            </AgentSystemSDS>\n" +
			"        </author1>\n" +
			"        <subject type=\"ActRelationship\" typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
			"            <EhrRequest type=\"ActHeir\" classCode=\"EXTRACT\" moodCode=\"RQO\">\n" +
			"                <id root=\"041CA2AE-3EC6-4AC9-942F-0F6621CC0BFC\"/>\n" +
			"                <recordTarget type=\"Participation\" typeCode=\"RCT\">\n" +
			"                    <patient type=\"Patient\" classCode=\"PAT\">\n" +
			"                        <id root=\"2.16.840.1.113883.2.1.4.1\" extension=\"9692294935\"/>\n" +
			"                    </patient>\n" +
			"                </recordTarget>\n" +
			"                <author type=\"Participation\" typeCode=\"AUT\">\n" +
			"                    <AgentOrgSDS type=\"RoleHeir\" classCode=\"AGNT\">\n" +
			"                        <agentOrganizationSDS type=\"Organization\" classCode=\"ORG\" determinerCode=\"INSTANCE\">\n" +
			"                            <id root=\"1.2.826.0.1285.0.1.10\" extension=\"N82668\"/>\n" +
			"                        </agentOrganizationSDS>\n" +
			"                    </AgentOrgSDS>\n" +
			"                </author>\n" +
			"                <destination type=\"Participation\" typeCode=\"DST\">\n" +
			"                    <AgentOrgSDS type=\"RoleHeir\" classCode=\"AGNT\">\n" +
			"                        <agentOrganizationSDS type=\"Organization\" classCode=\"ORG\" determinerCode=\"INSTANCE\">\n" +
			"                            <id root=\"1.2.826.0.1285.0.1.10\" extension=\"B86041\"/>\n" +
			"                        </agentOrganizationSDS>\n" +
			"                    </AgentOrgSDS>\n" +
			"                </destination>\n" +
			"            </EhrRequest>\n" +
			"        </subject>\n" +
			"    </ControlActEvent>\n" +
			"</RCMR_IN010000UK05>\n" +
			"\n" +
			"--e01d9133-5058-45e5-a884-9189f468c805--";

	@Test
	void shouldPassThroughMessagesForOldWorker() {
		//action: send a message on the inbound q
		jmsTemplate.send(inboundQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				BytesMessage bytesMessage = session.createBytesMessage();
				bytesMessage.writeUTF(ehrRequest);
				return bytesMessage;
			}
		});

		//assertion: verify the message gets on the outbound q
		jmsTemplate.setReceiveTimeout(5000);
		BytesMessage message = (BytesMessage) jmsTemplate.receive(outboundQueue);
		assertNotNull(message);
		try {
			String stringMessage = message.readUTF();
			assertThat(stringMessage, equalTo(ehrRequest));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Test
	void shouldSendMalformedMessagesToUnhandledQ() {
		String malformedMessage = "clearly not a GP2GP message";

		jmsTemplate.send(inboundQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				BytesMessage bytesMessage = session.createBytesMessage();
				bytesMessage.writeUTF(malformedMessage);
				return bytesMessage;
			}
		});

		//assertion: verify the message gets on the outbound q
		jmsTemplate.setReceiveTimeout(5000);
		BytesMessage message = (BytesMessage) jmsTemplate.receive(unhandledQueue);
		assertNotNull(message);
		try {
			String stringMessage = message.readUTF();
			assertThat(stringMessage, equalTo(malformedMessage));

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
