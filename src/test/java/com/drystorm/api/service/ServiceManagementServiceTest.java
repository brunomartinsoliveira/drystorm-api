package com.drystorm.api.service;

import com.drystorm.api.dto.request.ServiceRequest;
import com.drystorm.api.dto.response.ServiceResponse;
import com.drystorm.api.entity.Service;
import com.drystorm.api.exception.BusinessException;
import com.drystorm.api.exception.ResourceNotFoundException;
import com.drystorm.api.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceManagementService - Testes Unitários")
class ServiceManagementServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceManagementService serviceManagement;

    private Service mockService;

    @BeforeEach
    void setUp() {
        mockService = Service.builder()
                .id(1L)
                .name("Camiseta Dryfit Training")
                .description("Camiseta manga curta em dryfit")
                .price(new BigDecimal("79.90"))
                .durationMinutes(30)
                .category(Service.ServiceCategory.CAMISETAS)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("findAllActive - deve retornar lista de serviços ativos")
    void findAllActive_returnsList() {
        given(serviceRepository.findAllActiveOrderedByCategoryAndPrice())
                .willReturn(List.of(mockService));

        List<ServiceResponse> result = serviceManagement.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Camiseta Dryfit Training");
        assertThat(result.get(0).getDurationFormatted()).isEqualTo("30 min");
    }

    @Test
    @DisplayName("create - deve criar serviço com sucesso")
    void create_success() {
        ServiceRequest req = buildRequest("Shorts Dryfit Performance");
        given(serviceRepository.existsByNameAndActiveTrue("Shorts Dryfit Performance")).willReturn(false);
        given(serviceRepository.save(any())).willReturn(
                Service.builder().id(2L).name("Shorts Dryfit Performance")
                        .price(new BigDecimal("94.90")).durationMinutes(30)
                        .category(Service.ServiceCategory.SHORTS).active(true).build());

        ServiceResponse result = serviceManagement.create(req);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Shorts Dryfit Performance");
        assertThat(result.getDurationFormatted()).isEqualTo("30 min");
    }

    @Test
    @DisplayName("create - deve lançar exceção para nome duplicado")
    void create_duplicateNameThrowsException() {
        ServiceRequest req = buildRequest("Camiseta Dryfit Training");
        given(serviceRepository.existsByNameAndActiveTrue("Camiseta Dryfit Training")).willReturn(true);

        assertThatThrownBy(() -> serviceManagement.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Camiseta Dryfit Training");
    }

    @Test
    @DisplayName("deactivate - deve desativar serviço existente")
    void deactivate_success() {
        given(serviceRepository.findById(1L)).willReturn(Optional.of(mockService));
        given(serviceRepository.save(any())).willReturn(mockService);

        serviceManagement.deactivate(1L);

        assertThat(mockService.getActive()).isFalse();
        then(serviceRepository).should().save(mockService);
    }

    @Test
    @DisplayName("deactivate - deve lançar exceção para id inexistente")
    void deactivate_notFoundThrowsException() {
        given(serviceRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> serviceManagement.deactivate(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findById - deve retornar serviço ativo")
    void findById_returnsActive() {
        given(serviceRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(mockService));

        ServiceResponse result = serviceManagement.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCategoryLabel()).isEqualTo("Camisetas");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private ServiceRequest buildRequest(String name) {
        ServiceRequest req = new ServiceRequest();
        req.setName(name);
        req.setDescription("Descrição do serviço");
        req.setPrice(new BigDecimal("94.90"));
        req.setDurationMinutes(30);
        req.setCategory(Service.ServiceCategory.SHORTS);
        return req;
    }
}
