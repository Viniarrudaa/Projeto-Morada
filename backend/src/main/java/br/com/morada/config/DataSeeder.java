package br.com.morada.config;

import br.com.morada.domain.User;
import br.com.morada.domain.UserRole;
import br.com.morada.repository.UserRepository;
import br.com.morada.service.CpfService;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CpfService cpfService;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminCpf;
    private final String adminBirthDate;
    private final String adminPassword;

    public DataSeeder(
        UserRepository userRepository,
        CpfService cpfService,
        PasswordEncoder passwordEncoder,
        @Value("${app.admin.name}") String adminName,
        @Value("${app.admin.cpf}") String adminCpf,
        @Value("${app.admin.birth-date}") String adminBirthDate,
        @Value("${app.admin.password}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.cpfService = cpfService;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminCpf = adminCpf;
        this.adminBirthDate = adminBirthDate;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        String preparedAdminCpf = cpfService.normalizeAndValidate(adminCpf);
        LocalDate preparedBirthDate = LocalDate.parse(adminBirthDate);

        var savedAdminUser = userRepository.findByCpf(preparedAdminCpf);
        if (savedAdminUser.isPresent()) {
            User admin = savedAdminUser.get();
            prepareAdmin(admin, preparedAdminCpf, preparedBirthDate);
            userRepository.save(admin);
            return;
        }

        var currentAdminUser = userRepository.findFirstByRole(UserRole.ADMIN);
        if (currentAdminUser.isPresent()) {
            User admin = currentAdminUser.get();
            prepareAdmin(admin, preparedAdminCpf, preparedBirthDate);
            userRepository.save(admin);
            return;
        }

        User admin = new User();
        prepareAdmin(admin, preparedAdminCpf, preparedBirthDate);
        userRepository.save(admin);
    }

    private void prepareAdmin(User admin, String preparedAdminCpf, LocalDate preparedBirthDate) {
        admin.setName(adminName);
        admin.setCpf(preparedAdminCpf);
        admin.setBirthDate(preparedBirthDate);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);
    }
}
