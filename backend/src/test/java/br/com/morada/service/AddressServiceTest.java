package br.com.morada.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.morada.domain.Address;
import br.com.morada.domain.User;
import br.com.morada.domain.UserRole;
import br.com.morada.dto.AddressRequest;
import br.com.morada.dto.AddressResponse;
import br.com.morada.dto.CepResponse;
import br.com.morada.repository.AddressRepository;
import br.com.morada.security.AuthenticatedUser;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserService userService;

    @Mock
    private CepService cepService;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private AuthenticatedUser actor;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Helena Costa");
        user.setCpf("11144477735");
        user.setBirthDate(LocalDate.of(1995, 5, 20));
        user.setRole(UserRole.USER);

        actor = new AuthenticatedUser(1L, user.getCpf(), UserRole.USER);

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void firstAddressBecomesPrimaryEvenWhenRequestDoesNotAskForIt() {
        AddressRequest request = new AddressRequest("01001-000", "100", null, false);

        when(userService.findEntity(1L)).thenReturn(user);
        when(addressRepository.countByUserId(1L)).thenReturn(0L);
        when(cepService.findAddress(request.cep())).thenReturn(cepDaPracaDaSe());

        AddressResponse response = addressService.create(1L, request, actor);

        assertThat(response.primaryAddress()).isTrue();
        verify(addressRepository).clearPrimaryForUser(1L);
    }

    @Test
    void settingNewAddressAsPrimaryClearsPreviousPrimaryAddress() {
        AddressRequest request = new AddressRequest("01001-000", "200", "Apto 12", true);

        when(userService.findEntity(1L)).thenReturn(user);
        when(addressRepository.countByUserId(1L)).thenReturn(1L);
        when(cepService.findAddress(request.cep())).thenReturn(cepDaPracaDaSe());

        AddressResponse response = addressService.create(1L, request, actor);

        assertThat(response.primaryAddress()).isTrue();
        verify(addressRepository).clearPrimaryForUser(1L);
    }

    @Test
    void deletingPrimaryAddressPromotesAnotherAddressFromSameUser() {
        Address primary = address(10L, true);
        Address next = address(11L, false);

        when(addressRepository.findById(10L)).thenReturn(Optional.of(primary));
        when(addressRepository.findFirstByUserIdOrderByIdAsc(1L)).thenReturn(Optional.of(next));

        addressService.delete(10L, actor);

        assertThat(next.isPrimaryAddress()).isTrue();
        verify(addressRepository).delete(primary);
        verify(addressRepository).flush();
    }

    private Address address(Long id, boolean primaryAddress) {
        Address address = new Address();
        address.setId(id);
        address.setUser(user);
        address.setCep("01001000");
        address.setNumber("100");
        address.setStreet("Praca da Se");
        address.setDistrict("Se");
        address.setCity("Sao Paulo");
        address.setState("SP");
        address.setPrimaryAddress(primaryAddress);
        return address;
    }

    private CepResponse cepDaPracaDaSe() {
        return new CepResponse("01001000", "Praca da Se", "Se", "Sao Paulo", "SP");
    }
}
