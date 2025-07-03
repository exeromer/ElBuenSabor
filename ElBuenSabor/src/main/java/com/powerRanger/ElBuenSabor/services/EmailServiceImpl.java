package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void enviarFacturaPorEmail(Factura factura) {
        // Verificación para asegurar que tenemos todos los datos necesarios
        if (factura == null || factura.getPedido() == null || factura.getPedido().getCliente() == null) {
            System.err.println("ERROR: Faltan datos en la factura para poder enviar el email.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String to = factura.getPedido().getCliente().getEmail();
            String subject = "Tu Factura del Pedido #" + factura.getPedido().getId() + " de El Buen Sabor";

            // Construcción del cuerpo del email en formato HTML
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");
            sb.append("<h1>¡Gracias por tu compra!</h1>");
            sb.append("<p>Hola ").append(factura.getPedido().getCliente().getNombre()).append(",</p>");
            sb.append("<p>Adjuntamos el detalle de tu factura para el pedido <b>#").append(factura.getPedido().getId()).append("</b>:</p>");
            sb.append("<hr>");
            sb.append("<h3>Detalle de la Compra:</h3>");
            sb.append("<table border='1' cellpadding='5' style='border-collapse: collapse;'>");
            sb.append("<tr style='background-color:#f2f2f2;'><th>Cantidad</th><th>Producto</th><th>Subtotal</th></tr>");

            factura.getDetallesFactura().forEach(detalle -> {
                sb.append("<tr>");
                sb.append("<td style='text-align:center;'>").append(detalle.getCantidad()).append("</td>");
                sb.append("<td>").append(detalle.getDenominacionArticulo()).append("</td>");
                sb.append("<td style='text-align:right;'>$").append(String.format("%.2f", detalle.getSubTotal())).append("</td>");
                sb.append("</tr>");
            });

            sb.append("</table>");
            sb.append("<h3>Total: $").append(String.format("%.2f", factura.getTotalVenta())).append("</h3>");
            sb.append("<hr>");
            sb.append("<p>¡Que lo disfrutes!</p>");
            sb.append("<p>El equipo de El Buen Sabor.</p>");
            sb.append("</body></html>");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(sb.toString(), true); // true indica que el texto es HTML

            mailSender.send(message);
            System.out.println("Email de factura enviado exitosamente a " + to);

        } catch (Exception e) {
            System.err.println("Error al enviar el email de la factura: " + e.getMessage());
            // No relanzamos la excepción para no detener el flujo principal si el email falla
        }
    }
    @Override
    public void enviarNotaDeCreditoPorEmail(Factura facturaAnulada) {
        if (facturaAnulada == null || facturaAnulada.getPedido() == null || facturaAnulada.getPedido().getCliente() == null) {
            System.err.println("ERROR: Faltan datos para enviar la nota de crédito por email.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String to = facturaAnulada.getPedido().getCliente().getEmail();
            // LÓGICA MODIFICADA: Cambiamos el asunto del correo
            String subject = "Nota de Crédito - Anulación de tu Pedido #" + facturaAnulada.getPedido().getId();

            // LÓGICA MODIFICADA: Cambiamos el cuerpo del correo
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");
            sb.append("<h1>Notificación de Anulación y Nota de Crédito</h1>");
            sb.append("<p>Hola ").append(facturaAnulada.getPedido().getCliente().getNombre()).append(",</p>");
            sb.append("<p>Te informamos que tu pedido <b>#").append(facturaAnulada.getPedido().getId()).append("</b> ha sido anulado. A continuación, te presentamos la nota de crédito correspondiente, que refleja la anulación de los siguientes ítems:</p>");
            sb.append("<hr>");
            sb.append("<h3>Detalle de la Nota de Crédito (Factura Anulada #" + facturaAnulada.getId() + "):</h3>");
            sb.append("<table border='1' cellpadding='5' style='border-collapse: collapse;'>");
            sb.append("<tr style='background-color:#f2f2f2;'><th>Cantidad</th><th>Producto</th><th>Subtotal</th></tr>");

            facturaAnulada.getDetallesFactura().forEach(detalle -> {
                sb.append("<tr>");
                sb.append("<td style='text-align:center;'>").append(detalle.getCantidad()).append("</td>");
                sb.append("<td>").append(detalle.getDenominacionArticulo()).append("</td>");
                sb.append("<td style='text-align:right;'>$").append(String.format("%.2f", detalle.getSubTotal())).append("</td>");
                sb.append("</tr>");
            });

            sb.append("</table>");
            sb.append("<h3>Monto Acreditado: $").append(String.format("%.2f", facturaAnulada.getTotalVenta())).append("</h3>");
            sb.append("<hr>");
            sb.append("<p>El monto ha sido acreditado a tu favor. Si tienes alguna consulta, no dudes en contactarnos.</p>");
            sb.append("<p>El equipo de El Buen Sabor.</p>");
            sb.append("</body></html>");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(sb.toString(), true);

            mailSender.send(message);
            System.out.println("Email de Nota de Crédito enviado exitosamente a " + to);

        } catch (Exception e) {
            System.err.println("Error al enviar el email de la nota de crédito: " + e.getMessage());
        }
    }
}