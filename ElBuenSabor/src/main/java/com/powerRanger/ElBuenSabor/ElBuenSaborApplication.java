package com.powerRanger.ElBuenSabor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElBuenSaborApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElBuenSaborApplication.class, args);
		// Ya no se llama a insertarDatos() ni actualizarArticulo() desde aquí.
		// El DataInitializer lo hará automáticamente.
	}
}