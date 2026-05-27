package com.tecsup.pe.back_zonet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity // 👈 CRÍTICO: Si faltaba esto, este era el causante del error
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNumber;
    private Double amount;
    private LocalDateTime transactionDate;
    private String status; // SUCCESS, FAILED, etc.

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}