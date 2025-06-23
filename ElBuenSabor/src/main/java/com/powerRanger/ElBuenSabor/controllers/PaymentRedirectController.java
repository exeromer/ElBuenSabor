// En src/main/java/com/powerRanger/ElBuenSabor/controllers/PaymentRedirectController.java

package com.powerRanger.ElBuenSabor.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentRedirectController {

    // Este método manejará la redirección desde Mercado Pago
    @GetMapping("/success")
    public ResponseEntity<Void> handleSuccessRedirect(@RequestParam Map<String, String> params) throws URISyntaxException {
        // Redirigimos al usuario a la página de "Mis Pedidos" en el frontend
        URI MisPedidosUri = new URI("http://localhost:5173/mis-pedidos");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(MisPedidosUri);
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // Usamos 303 para redirección post-pago
    }

    // Puedes añadir métodos similares para failure y pending si lo necesitas
    @GetMapping("/failure")
    public ResponseEntity<Void> handleFailureRedirect() throws URISyntaxException {
        URI checkoutUri = new URI("http://localhost:5173/checkout?payment_status=failure");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(checkoutUri);
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }
}