package com.musica.ms_catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musica.ms_catalogo.model.Catalogo;

public interface CatalogoRepository extends JpaRepository<Catalogo, Long> {
    java.util.Optional<Catalogo> findByCodigo(String codigo);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Catalogo c where c.codigo = :codigo")
    java.util.Optional<Catalogo> bloquearCodigo(@org.springframework.data.repository.query.Param("codigo") String codigo);
    boolean existsByCodigo(String codigo);

    boolean existsByNombreIgnoreCaseAndArtistaIgnoreCaseAndFormatoIgnoreCase(
            String nombre,
            String artista,
            String formato
    );

    boolean existsByNombreIgnoreCaseAndArtistaIgnoreCaseAndFormatoIgnoreCaseAndIdNot(
            String nombre,
            String artista,
            String formato,
            Long id
    );

    List<Catalogo> findByArtistaContainingIgnoreCase(String artista);

    List<Catalogo> findByGeneroContainingIgnoreCase(String genero);

    List<Catalogo> findByFormatoIgnoreCase(String formato);
}