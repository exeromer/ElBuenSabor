package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.BaseEntity;
import com.powerRanger.ElBuenSabor.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<E extends BaseEntity, R extends BaseRepository<E, Integer>> implements BaseService<E, Integer> {

    protected R baseRepository;

    public BaseServiceImpl(R baseRepository) {
        this.baseRepository = baseRepository;
    }

    @Override
    @Transactional
    public List<E> findAll() throws Exception {
        try {
            return baseRepository.findAll();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public E findById(Integer id) throws Exception {
        try {
            Optional<E> entityOptional = baseRepository.findById(id);
            if(entityOptional.isEmpty()){
                throw new Exception("No se encontró el registro con el id: " + id);
            }
            return entityOptional.get();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public E save(E entity) throws Exception {
        try {
            return baseRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public E update(Integer id, E entity) throws Exception {
        try {
            Optional<E> entityOptional = baseRepository.findById(id);
            if (entityOptional.isEmpty()) {
                throw new Exception("No se encontró el registro con el id: " + id);
            }
            // El ID ya viene en la entidad, pero nos aseguramos que sea el correcto.
            // La entidad que llega del request body no tiene ID, pero el del path variable sí.
            entity.setId(id);
            return baseRepository.save(entity);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Integer id) throws Exception {
        try {
            if (baseRepository.existsById(id)) {
                baseRepository.deleteById(id);
                return true;
            } else {
                throw new Exception("No se encontró el registro con el id: " + id);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}