package uk.nhs.prm.repo.ehrtransferservice.json_models;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EhrExtractMessageTest {
    @Test
    public void shouldSerializeToJson() {
        String jsonText = new Gson().toJson(new EhrExtractMessage(UUID.fromString("ef90e1ec-5948-4ed6-b4d2-a3fbaebc5717")));
        assertThat(jsonText, equalTo("{\"messageId\":\"ef90e1ec-5948-4ed6-b4d2-a3fbaebc5717\"}"));
    }
}