package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.entity.IngestionProfileSubscription;
import org.ingestor.repository.IngestionProfileRepository;
import org.ingestor.repository.IngestionProfileSubscriptionRepository;
import org.ingestor.repository.MarketDataSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngestionProfileSubscriptionService {

    private final IngestionProfileSubscriptionRepository profileSubscriptionRepository;
    private final IngestionProfileRepository ingestionProfileRepository;
    private final MarketDataSubscriptionRepository marketDataSubscriptionRepository;

}
