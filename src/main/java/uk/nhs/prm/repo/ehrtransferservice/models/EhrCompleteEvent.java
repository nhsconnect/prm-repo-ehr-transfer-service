package uk.nhs.prm.repo.ehrtransferservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EhrCompleteEvent {
    private UUID conversationId;
}
