package uk.gov.di.ipv.cri.experian.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.ExperianGateway;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.experian.util.TestDataCreator.createTestPersonIdentity;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {

    private IdentityVerificationService identityVerificationService;
    @Mock private ExperianGateway mockExperianGateway;

    @BeforeEach
    void setUp() {
        this.identityVerificationService = new IdentityVerificationService(mockExperianGateway);
    }

    @Test
    void shouldInvokeTheCrossCoreGateway() throws IOException, InterruptedException {
        final String identityCheckResult = "identity-check-response";
        PersonIdentity testPersonIdentity = createTestPersonIdentity();
        when(this.mockExperianGateway.performIdentityCheck(testPersonIdentity))
                .thenReturn(identityCheckResult);

        String result = this.identityVerificationService.verifyIdentity(testPersonIdentity);

        verify(mockExperianGateway).performIdentityCheck(testPersonIdentity);
        assertEquals(identityCheckResult, result);
    }

    @Test
    void shouldReturnNullWhenAnExceptionOccurs() throws IOException, InterruptedException {
        PersonIdentity testPersonIdentity = createTestPersonIdentity();
        when(this.mockExperianGateway.performIdentityCheck(testPersonIdentity))
                .thenThrow(new IOException());

        String result = this.identityVerificationService.verifyIdentity(testPersonIdentity);

        assertNull(result);
    }
}
