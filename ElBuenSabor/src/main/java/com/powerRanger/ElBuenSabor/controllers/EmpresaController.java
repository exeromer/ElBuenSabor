package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.EmpresaRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpresaResponseDTO;
import com.powerRanger.ElBuenSabor.services.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    @GetMapping("")
    public ResponseEntity<List<EmpresaResponseDTO>> getAll() throws Exception {
        return ResponseEntity.ok(empresaService.findAllEmpresas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> getOne(@PathVariable Integer id) throws Exception {
        return ResponseEntity.ok(empresaService.findEmpresaById(id));
    }

    @PostMapping("")
    public ResponseEntity<EmpresaResponseDTO> save(@Valid @RequestBody EmpresaRequestDTO dto) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.createEmpresa(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody EmpresaRequestDTO dto) throws Exception {
        return ResponseEntity.ok(empresaService.updateEmpresa(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) throws Exception {
        empresaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}