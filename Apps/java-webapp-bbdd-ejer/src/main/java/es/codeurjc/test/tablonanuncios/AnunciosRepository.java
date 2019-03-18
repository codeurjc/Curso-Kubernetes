package es.codeurjc.test.tablonanuncios;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnunciosRepository extends JpaRepository<Anuncio, Long> {

}