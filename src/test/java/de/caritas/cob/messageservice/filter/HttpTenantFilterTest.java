package de.caritas.cob.messageservice.filter;

import de.caritas.cob.messageservice.api.tenant.TenantResolverService;
import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpTenantFilterTest {

  @InjectMocks HttpTenantFilter httpTenantFilter;

  @Mock private TenantResolverService tenantResolverService;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  @Mock FilterChain filterChain;

  @Test
  void doFilterInternal_Should_NotApply_When_RequestBelongsToTenancyWhiteList()
      throws ServletException, IOException {
    // given
    Mockito.when(request.getRequestURI()).thenReturn("/actuator/health/liveness");

    // when
    httpTenantFilter.doFilterInternal(request, response, filterChain);

    // then
    Mockito.verifyNoInteractions(tenantResolverService);
  }

  @Test
  void doFilterInternal_Should_Apply_When_DoesNotBelongBelongsToTenancyWhiteList()
      throws ServletException, IOException {

    // given
    Mockito.when(request.getRequestURI()).thenReturn("/messages/1");

    // when
    httpTenantFilter.doFilterInternal(request, response, filterChain);

    // then
    Mockito.verify(tenantResolverService).resolve(request);
  }
}
