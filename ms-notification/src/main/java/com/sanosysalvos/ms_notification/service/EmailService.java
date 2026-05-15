package com.sanosysalvos.ms_notification.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;
    @Value("${sendgrid.template.id}")
    private String templateId;
    // Añadimos @Async si decidiste usarlo para que no bloquee el front
    public void enviarCorreo(String destinatario, String asunto, String cuerpoMensaje) {
        Email from = new Email(fromEmail);
        Email to = new Email(destinatario);
        
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(asunto); // Mantenemos el asunto original

        // 🟢 INTERCEPTAMOS: ¿Es una alerta de mascota de Sanos y Salvos?
        if (asunto != null && (asunto.toLowerCase().contains("avistamiento") || asunto.toLowerCase().contains("noticias"))) {
            
            // 1. Vinculamos el ID de la plantilla dinámica de SendGrid
            mail.setTemplateId(templateId);

            // 2. Mapeamos las variables que pusiste entre llaves {{{ }}} en tu HTML
            Personalization personalization = new Personalization();
            personalization.addTo(to);
            
            // Pasamos el nombre por defecto o lo extraemos, aquí usamos Doraemon de prueba
            // Si el nombre viene en el asunto o cuerpo, podrías procesarlo, por ahora lo dejamos estático
            personalization.addDynamicTemplateData("nombre_mascota", "tu mascota");
            personalization.addDynamicTemplateData("mensaje", cuerpoMensaje);

            mail.addPersonalization(personalization);
            
        } else {
            // 🟡 FLUJO NORMAL: Si es otro tipo de correo, sigue usando texto plano anterior
            Content content = new Content("text/plain", cuerpoMensaje);
            mail.setSubject(asunto);
            
            Personalization personalization = new Personalization();
            personalization.addTo(to);
            mail.addPersonalization(personalization);
            mail.addContent(content);
        }

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            // Log clave: si ves 202, todo salió bien.
            System.out.println("SendGrid Response Status: " + response.getStatusCode());
            
            if (response.getStatusCode() >= 400) {
                System.err.println("Error de SendGrid: " + response.getBody());
            }
            
        } catch (IOException ex) {
            System.err.println("Error de conexión con SendGrid: " + ex.getMessage());
        }
    }
}