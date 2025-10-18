package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plan; // PREMIUM

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
