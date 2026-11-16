package com.musica.ms_auth.repository;

import com.musica.ms_auth.model.SesionRefresh;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SesionRefreshRepository extends JpaRepository<SesionRefresh, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SesionRefresh s where s.tokenHash=:hash")
    Optional<SesionRefresh> buscarBloqueando(@Param("hash") String hash);
    @org.springframework.transaction.annotation.Transactional
    void deleteByUsuarioId(Long usuarioId);
}
