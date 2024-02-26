package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
}
