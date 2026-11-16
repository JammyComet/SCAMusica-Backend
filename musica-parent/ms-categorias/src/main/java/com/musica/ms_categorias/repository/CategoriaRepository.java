package com.musica.ms_categorias.repository;
import com.musica.ms_categorias.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria,Long> { boolean existsByNombreIgnoreCase(String nombre); }
