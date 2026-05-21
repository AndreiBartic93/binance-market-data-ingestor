package org.ingestor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "exchanges",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_exchanges_code", columnNames = "code")
        }
)
public class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Internal exchange code, ex: BINANCE, COINBASE, KRAKEN
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    //Human-Readable exchange name, ex: Binance, Coinbase, Kraken
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "rest_base_url")
    private String restBaseUrl;

    @Column(name = "websocket_base_url")
    private String websocketBaseUrl;

    //Allows enabling/disabling an exchange without deleting it.
    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.active = true;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

}
