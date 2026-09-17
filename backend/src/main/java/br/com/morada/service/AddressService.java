package br.com.morada.service;

import br.com.morada.domain.Address;
import br.com.morada.domain.User;
import br.com.morada.dto.AddressRequest;
import br.com.morada.dto.AddressResponse;
import br.com.morada.dto.CepResponse;
import br.com.morada.exception.ApiException;
import br.com.morada.repository.AddressRepository;
import br.com.morada.security.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;
    private final CepService cepService;

    public AddressService(AddressRepository addressRepository, UserService userService, CepService cepService) {
        this.addressRepository = addressRepository;
        this.userService = userService;
        this.cepService = cepService;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listByUser(Long userId, AuthenticatedUser actor) {
        requireSameUserOrAdmin(userId, actor);
        userService.findEntity(userId);

        return addressRepository.findByUserIdOrderByPrimaryAddressDescIdAsc(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest request, AuthenticatedUser actor) {
        requireSameUserOrAdmin(userId, actor);
        User user = userService.findEntity(userId);
        CepResponse cep = cepService.findAddress(request.cep());
        boolean firstAddress = addressRepository.countByUserId(userId) == 0;
        boolean shouldBePrimary = firstAddress || request.primaryAddress();

        if (shouldBePrimary) {
            addressRepository.clearPrimaryForUser(userId);
        }

        Address address = new Address();
        address.setUser(user);
        copyFromRequest(address, request, cep);
        address.setPrimaryAddress(shouldBePrimary);

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long addressId, AddressRequest request, AuthenticatedUser actor) {
        Address address = findAddressForActor(addressId, actor);
        CepResponse cep = cepService.findAddress(request.cep());
        boolean wasPrimary = address.isPrimaryAddress();

        copyFromRequest(address, request, cep);

        if (request.primaryAddress()) {
            addressRepository.clearPrimaryForUser(address.getUser().getId());
            address.setPrimaryAddress(true);
        } else if (wasPrimary && addressRepository.countByUserId(address.getUser().getId()) > 1) {
            address.setPrimaryAddress(false);
            promoteAnotherAddress(address);
        } else {
            address.setPrimaryAddress(wasPrimary);
        }

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse setPrimary(Long addressId, AuthenticatedUser actor) {
        Address address = findAddressForActor(addressId, actor);
        addressRepository.clearPrimaryForUser(address.getUser().getId());
        address.setPrimaryAddress(true);
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long addressId, AuthenticatedUser actor) {
        Address address = findAddressForActor(addressId, actor);
        Long userId = address.getUser().getId();
        boolean wasPrimary = address.isPrimaryAddress();

        addressRepository.delete(address);
        addressRepository.flush();

        if (wasPrimary) {
            addressRepository.findFirstByUserIdOrderByIdAsc(userId)
                .ifPresent(next -> {
                    next.setPrimaryAddress(true);
                    addressRepository.save(next);
                });
        }
    }

    private Address findAddressForActor(Long addressId, AuthenticatedUser actor) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> ApiException.notFound("address_not_found", "Endereço não encontrado."));

        requireSameUserOrAdmin(address.getUser().getId(), actor);
        return address;
    }

    private void requireSameUserOrAdmin(Long userId, AuthenticatedUser actor) {
        if (!actor.isAdmin() && !actor.id().equals(userId)) {
            throw ApiException.forbidden("Você só pode gerenciar seus próprios endereços.");
        }
    }

    private void promoteAnotherAddress(Address current) {
        addressRepository.findByUserIdOrderByPrimaryAddressDescIdAsc(current.getUser().getId()).stream()
            .filter(candidate -> !candidate.getId().equals(current.getId()))
            .findFirst()
            .ifPresent(next -> next.setPrimaryAddress(true));
    }

    private void copyFromRequest(Address address, AddressRequest request, CepResponse cep) {
        address.setCep(cep.cep());
        address.setNumber(request.number().trim());
        address.setComplement(blankToNull(request.complement()));
        address.setStreet(cep.street());
        address.setDistrict(cep.district());
        address.setCity(cep.city());
        address.setState(cep.state());
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
            address.getId(),
            address.getUser().getId(),
            address.getCep(),
            address.getNumber(),
            address.getComplement(),
            address.getStreet(),
            address.getDistrict(),
            address.getCity(),
            address.getState(),
            address.isPrimaryAddress()
        );
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
