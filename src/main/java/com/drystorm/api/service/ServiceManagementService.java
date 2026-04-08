package com.drystorm.api.service;

import com.drystorm.api.dto.request.ServiceRequest;
import com.drystorm.api.dto.response.ServiceResponse;
import com.drystorm.api.entity.Service;
import com.drystorm.api.exception.BusinessException;
import com.drystorm.api.exception.ResourceNotFoundException;
import com.drystorm.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;

    // ─── Listagens ────────────────────────────────────────────────────────────
    public List<ServiceResponse> findAllActive() {
        return serviceRepository.findAllActiveOrderedByCategoryAndPrice()
                .stream().map(ServiceResponse::from).toList();
    }

    public Map<String, List<ServiceResponse>> findGroupedByCategory() {
        return serviceRepository.findAllActiveOrderedByCategoryAndPrice()
                .stream()
                .map(ServiceResponse::from)
                .collect(Collectors.groupingBy(s -> s.getCategoryLabel()));
    }

    public ServiceResponse findById(Long id) {
        return serviceRepository.findByIdAndActiveTrue(id)
                .map(ServiceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────
    @Transactional
    public ServiceResponse create(ServiceRequest req) {
        if (serviceRepository.existsByNameAndActiveTrue(req.getName())) {
            throw new BusinessException("Já existe um serviço ativo com o nome: " + req.getName());
        }

        Service service = Service.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .durationMinutes(req.getDurationMinutes())
                .category(req.getCategory())
                .build();

        Service saved = serviceRepository.save(service);
        log.info("Serviço criado: {} (id={})", saved.getName(), saved.getId());
        return ServiceResponse.from(saved);
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceRequest req) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));

        boolean nameChanged = !service.getName().equalsIgnoreCase(req.getName());
        if (nameChanged && serviceRepository.existsByNameAndActiveTrue(req.getName())) {
            throw new BusinessException("Já existe um serviço ativo com o nome: " + req.getName());
        }

        service.setName(req.getName());
        service.setDescription(req.getDescription());
        service.setPrice(req.getPrice());
        service.setDurationMinutes(req.getDurationMinutes());
        service.setCategory(req.getCategory());

        log.info("Serviço atualizado: {} (id={})", service.getName(), service.getId());
        return ServiceResponse.from(serviceRepository.save(service));
    }

    @Transactional
    public void deactivate(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
        service.setActive(false);
        serviceRepository.save(service);
        log.info("Serviço desativado: {} (id={})", service.getName(), id);
    }

    @Transactional
    public ServiceResponse reactivate(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
        service.setActive(true);
        log.info("Serviço reativado: {} (id={})", service.getName(), id);
        return ServiceResponse.from(serviceRepository.save(service));
    }
}
