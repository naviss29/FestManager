package com.festmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "creneau")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Creneau {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(name = "nb_benevoles_requis", nullable = false)
    private Integer nbBenevolesRequis;
}
