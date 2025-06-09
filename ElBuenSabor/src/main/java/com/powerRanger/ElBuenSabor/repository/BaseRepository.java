package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.io.Serializable;

@NoRepositoryBean // Indica a Spring que no cree una instancia de esta interfaz directamente.
public interface BaseRepository<E extends BaseEntity, ID extends Serializable> extends JpaRepository<E, ID> {
}