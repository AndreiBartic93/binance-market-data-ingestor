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
        name = "timeframes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_timeframes_code", columnNames = "code")
        }
)
public class Timeframe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Timeframe code used by Binance.
     * Examples: 1m, 5m, 15m, 1h, 4h, 1d.
     */
    private String code;

    /**
     * Timeframe duration expressed in minutes.
     * Examples:
     * 1m  -> 1
     * 1h  -> 60
     * 4h  -> 240
     * 1d  -> 1440
     */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * Allows disabling a timeframe without deleting it.
     */
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
