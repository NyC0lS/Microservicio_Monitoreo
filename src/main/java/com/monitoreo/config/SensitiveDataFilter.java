package com.monitoreo.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Filtro para enmascarar datos sensibles en logs y respuestas.
 * Protege información personal y financiera según regulaciones de privacidad.
 */
@Component
public class SensitiveDataFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(SensitiveDataFilter.class);
    
    // Patrones para identificar datos sensibles
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\+?[1-9]\\d{1,14}\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    
    // Valores de reemplazo
    private static final String EMAIL_MASK = "***@***.***";
    private static final String PHONE_MASK = "***-***-****";
    private static final String CREDIT_CARD_MASK = "****-****-****-****";
    private static final String SSN_MASK = "***-**-****";
    private static final String IP_MASK = "***.***.***.***";
    private static final String GENERIC_MASK = "***";
    
    /**
     * Enmascara datos sensibles en un texto
     */
    public String maskSensitiveData(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String masked = text;
        
        // Enmascarar emails
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(EMAIL_MASK);
        
        // Enmascarar teléfonos
        masked = PHONE_PATTERN.matcher(masked).replaceAll(PHONE_MASK);
        
        // Enmascarar tarjetas de crédito
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll(CREDIT_CARD_MASK);
        
        // Enmascarar SSN
        masked = SSN_PATTERN.matcher(masked).replaceAll(SSN_MASK);
        
        // Enmascarar IPs
        masked = IP_PATTERN.matcher(masked).replaceAll(IP_MASK);
        
        return masked;
    }
    
    /**
     * Enmascara un email específico
     */
    public String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return EMAIL_MASK;
        }
        
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        
        String maskedLocal = localPart.length() <= 2 ? 
            localPart.substring(0, 1) + "*" : 
            localPart.substring(0, 1) + "*".repeat(localPart.length() - 2) + localPart.substring(localPart.length() - 1);
        
        int dotIndex = domain.lastIndexOf('.');
        String maskedDomain = dotIndex > 0 ? 
            domain.substring(0, 1) + "*" + domain.substring(dotIndex) : 
            domain.substring(0, 1) + "*";
        
        return maskedLocal + "@" + maskedDomain;
    }
    
    /**
     * Enmascara un número de teléfono
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        
        // Remover caracteres no numéricos
        String digits = phone.replaceAll("\\D", "");
        
        if (digits.length() < 10) {
            return PHONE_MASK;
        }
        
        // Mantener solo los últimos 4 dígitos
        String lastFour = digits.substring(digits.length() - 4);
        return "***-***-" + lastFour;
    }
    
    /**
     * Enmascara un número de tarjeta de crédito
     */
    public String maskCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }
        
        // Remover espacios y guiones
        String digits = cardNumber.replaceAll("\\s|-", "");
        
        if (digits.length() < 13 || digits.length() > 19) {
            return CREDIT_CARD_MASK;
        }
        
        // Mantener solo los últimos 4 dígitos
        String lastFour = digits.substring(digits.length() - 4);
        return "****-****-****-" + lastFour;
    }
    
    /**
     * Enmascara un SSN
     */
    public String maskSSN(String ssn) {
        if (ssn == null || ssn.isEmpty()) {
            return ssn;
        }
        
        // Remover caracteres no numéricos
        String digits = ssn.replaceAll("\\D", "");
        
        if (digits.length() != 9) {
            return SSN_MASK;
        }
        
        // Mantener solo los últimos 4 dígitos
        String lastFour = digits.substring(5);
        return "***-**-" + lastFour;
    }
    
    /**
     * Enmascara una dirección IP
     */
    public String maskIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return IP_MASK;
        }
        
        // Mantener solo el primer octeto
        return parts[0] + ".***.***.***";
    }
    
    /**
     * Enmascara un ID de usuario (mantiene solo los primeros y últimos caracteres)
     */
    public String maskUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return userId;
        }
        
        if (userId.length() <= 4) {
            return GENERIC_MASK;
        }
        
        return userId.substring(0, 2) + "*".repeat(userId.length() - 4) + userId.substring(userId.length() - 2);
    }
    
    /**
     * Enmascara un monto monetario (mantiene solo el rango)
     */
    public String maskAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return amount;
        }
        
        try {
            double value = Double.parseDouble(amount);
            if (value < 100) {
                return "< $100";
            } else if (value < 1000) {
                return "$100 - $999";
            } else if (value < 10000) {
                return "$1K - $9K";
            } else {
                return "> $10K";
            }
        } catch (NumberFormatException e) {
            return GENERIC_MASK;
        }
    }
    
    /**
     * Verifica si un texto contiene datos sensibles
     */
    public boolean containsSensitiveData(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(text).find() ||
               PHONE_PATTERN.matcher(text).find() ||
               CREDIT_CARD_PATTERN.matcher(text).find() ||
               SSN_PATTERN.matcher(text).find() ||
               IP_PATTERN.matcher(text).find();
    }
    
    /**
     * Serializador JSON personalizado para enmascarar datos sensibles
     */
    public static class SensitiveDataSerializer extends JsonSerializer<String> {
        private final SensitiveDataFilter filter = new SensitiveDataFilter();
        
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null && filter.containsSensitiveData(value)) {
                gen.writeString(filter.maskSensitiveData(value));
            } else {
                gen.writeString(value);
            }
        }
    }
} 