package com.monitoreo.service;

import com.monitoreo.config.SensitiveDataFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private SensitiveDataFilter sensitiveDataFilter;

    @InjectMocks
    private AuditService auditService;

    @Test
    void logSensitiveDataDetected() {
        assertDoesNotThrow(() -> auditService.logSensitiveDataDetected("CreditCard", "PaymentForm", "User made a payment"));
    }

    @Test
    void logDataMasked() {
        assertDoesNotThrow(() -> auditService.logDataMasked("Email", "test@example.com", "t***@example.com", "UserProfile"));
    }

    @Test
    void logSensitiveDataAccess() {
        when(sensitiveDataFilter.maskUserId(anyString())).thenReturn("masked_user");
        
        assertDoesNotThrow(() -> auditService.logSensitiveDataAccess("user123", "PersonalInfo", "READ", "users/123"));
        
        verify(sensitiveDataFilter, times(2)).maskUserId("user123");
    }

    @Test
    void logUnauthorizedAccess() {
        when(sensitiveDataFilter.maskUserId(anyString())).thenReturn("masked_user");

        assertDoesNotThrow(() -> auditService.logUnauthorizedAccess("user456", "AdminPanel", "UPDATE", "/admin", "Insufficient privileges"));

        verify(sensitiveDataFilter, times(2)).maskUserId("user456");
    }

    @Test
    void logMaskingConfiguration() {
        when(sensitiveDataFilter.maskSensitiveData(anyString())).thenReturn("masked_value");
        
        assertDoesNotThrow(() -> auditService.logMaskingConfiguration("ssn_regex", "(...)", true));
        
        verify(sensitiveDataFilter, times(1)).maskSensitiveData("(...)");
    }

    @Test
    void logMaskingError() {
        assertDoesNotThrow(() -> auditService.logMaskingError("PhoneNumber", "Regex timeout", "BatchProcess"));
    }
    
    @Test
    void logAuditSessionStart() {
        when(sensitiveDataFilter.maskUserId(anyString())).thenReturn("masked_user");
        when(sensitiveDataFilter.maskIP(anyString())).thenReturn("masked_ip");

        assertDoesNotThrow(() -> auditService.logAuditSessionStart("session_abc", "user789", "192.168.1.1"));

        verify(sensitiveDataFilter, times(2)).maskUserId("user789");
        verify(sensitiveDataFilter, times(1)).maskIP("192.168.1.1");
    }

    @Test
    void logAuditSessionEnd() {
        when(sensitiveDataFilter.maskUserId(anyString())).thenReturn("masked_user");

        assertDoesNotThrow(() -> auditService.logAuditSessionEnd("session_abc", "user789", 3600));

        verify(sensitiveDataFilter, times(2)).maskUserId("user789");
    }
    
    @Test
    void logComplianceEvent() {
        when(sensitiveDataFilter.maskSensitiveData(anyString())).thenReturn("masked_details");

        assertDoesNotThrow(() -> auditService.logComplianceEvent("GDPR", "Art. 17", "Data Deletion", "User account for user123 deleted."));

        verify(sensitiveDataFilter, times(1)).maskSensitiveData("User account for user123 deleted.");
    }

    @Test
    void logDataRetentionEvent() {
        assertDoesNotThrow(() -> auditService.logDataRetentionEvent("access_logs", "ARCHIVED", "5 years", 10000));
    }
} 