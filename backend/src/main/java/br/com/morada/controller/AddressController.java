package br.com.morada.controller;

import br.com.morada.dto.AddressRequest;
import br.com.morada.dto.AddressResponse;
import br.com.morada.security.AuthenticatedUser;
import br.com.morada.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/users/{userId}/addresses")
    public List<AddressResponse> list(@PathVariable Long userId, @AuthenticationPrincipal AuthenticatedUser actor) {
        return addressService.listByUser(userId, actor);
    }

    @PostMapping("/users/{userId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(
        @PathVariable Long userId,
        @Valid @RequestBody AddressRequest request,
        @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        return addressService.create(userId, request, actor);
    }

    @PutMapping("/addresses/{addressId}")
    public AddressResponse update(
        @PathVariable Long addressId,
        @Valid @RequestBody AddressRequest request,
        @AuthenticationPrincipal AuthenticatedUser actor
    ) {
        return addressService.update(addressId, request, actor);
    }

    @PatchMapping("/addresses/{addressId}/primary")
    public AddressResponse setPrimary(@PathVariable Long addressId, @AuthenticationPrincipal AuthenticatedUser actor) {
        return addressService.setPrimary(addressId, actor);
    }

    @DeleteMapping("/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long addressId, @AuthenticationPrincipal AuthenticatedUser actor) {
        addressService.delete(addressId, actor);
    }
}
