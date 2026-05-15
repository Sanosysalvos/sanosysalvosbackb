package com.sanosysalvos.ms_notification.controller;

import com.sanosysalvos.ms_notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications") // Es buena práctica definir la ruta base aquí
public class NotificationController { // <-- Abre la clase

    @Autowired
    private EmailService emailService;

   @PostMapping("/send-test")
    public String enviarPrueba(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String mascota = body.get("mascota");
        String mensajeUsuario = body.get("mensaje"); 
        
        // 1. Intentamos sacar el asunto que viene del BFF. 
        // Si por alguna razón viene vacío, le dejamos uno por defecto que SÍ active el 'if' de la plantilla.
        String asunto = body.getOrDefault("asunto", "Alerta de avistamiento de " + mascota);
        
        // 2. IMPORTANTE: Ahora para la plantilla de SendGrid, el "cuerpoMensaje" 
        // debe ser ÚNICAMENTE el texto que escribió el usuario ("Andaba buscando gatas..."), 
        // ya que el diseño del HTML se encarga de poner los títulos y el botón.
        String cuerpoFinal = mensajeUsuario;
        
        // 3. Enviamos los datos limpios al servicio
        emailService.enviarCorreo(email, asunto, cuerpoFinal);
        
        return "Correo enviado a " + email;
    }

} // <-- Esta es la llave final que te faltaba para cerrar la clase