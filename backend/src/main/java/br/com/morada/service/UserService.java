package br.com.morada.service;

import br.com.morada.domain.User;
import br.com.morada.domain.UserRole;
import br.com.morada.dto.CreateUserRequest;
import br.com.morada.dto.RegisterRequest;
import br.com.morada.dto.UserResponse;
import br.com.morada.exception.ApiException;
import br.com.morada.repository.UserRepository;
import br.com.morada.security.AuthenticatedUser;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CpfService cpfService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CpfService cpfService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cpfService = cpfService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        User user = createUser(request.name(), request.cpf(), request.birthDate(), request.password(), UserRole.USER);
        return toResponse(user);
    }

    @Transactional
    public UserResponse createByAdmin(CreateUserRequest request) {
        UserRole role = request.role() == null ? UserRole.USER : request.role();
        User user = createUser(request.name(), request.cpf(), request.birthDate(), request.password(), role);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAll(AuthenticatedUser actor) {
        requireAdmin(actor);
        return userRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findVisibleUser(Long userId, AuthenticatedUser actor) {
        if (!actor.isAdmin() && !actor.id().equals(userId)) {
            throw ApiException.forbidden("Você só pode visualizar seus próprios dados.");
        }

        return userRepository.findById(userId)
            .map(this::toResponse)
            .orElseThrow(() -> ApiException.notFound("user_not_found", "Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public User findByCpfForLogin(String rawCpf) {
        String cpf = cpfService.normalizeAndValidate(rawCpf);
        return userRepository.findByCpf(cpf)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "CPF ou senha inválidos."));
    }

    @Transactional(readOnly = true)
    public User findEntity(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> ApiException.notFound("user_not_found", "Usuário não encontrado."));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getCpf(), user.getBirthDate(), user.getRole());
    }

    public void requireAdmin(AuthenticatedUser actor) {
        if (!actor.isAdmin()) {
            throw ApiException.forbidden("Apenas administradores podem executar esta ação.");
        }
    }

    private User createUser(String name, String rawCpf, java.time.LocalDate birthDate, String password, UserRole role) {
        String cpf = cpfService.normalizeAndValidate(rawCpf);
        if (userRepository.existsByCpf(cpf)) {
            throw ApiException.badRequest("cpf_already_exists", "Já existe um usuário com este CPF.");
        }

        User user = new User();
        user.setName(name.trim());
        user.setCpf(cpf);
        user.setBirthDate(birthDate);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }
}
