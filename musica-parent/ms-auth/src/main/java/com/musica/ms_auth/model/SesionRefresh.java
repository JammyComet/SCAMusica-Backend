package com.musica.ms_auth.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "sesion_refresh", indexes = @Index(name="idx_refresh_usuario", columnList="usuario_id"))
@Data
public class SesionRefresh {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="usuario_id",nullable=false)
    private Long usuarioId;
    @Column(name="token_hash",nullable=false,unique=true,length=64)
    private String tokenHash;
    @Column(name="expira_en",nullable=false)
    private Instant expiraEn;
}
