package com.eir.auth;

import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.repository.RoleRepository;
import com.eir.auth.repository.UserRepository;
import com.eir.auth.token.TokenFactory;
import com.eir.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestAuthApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Rollback(false)
@Commit
public abstract class AbstractComponentTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected TokenFactory tokenFactory;

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @MockBean
    protected org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
        // Don't delete roles - they're created by Flyway migration
        // roleRepository.deleteAll();
        seedDefaultData();
    }

    private void seedDefaultData() {
        // Seed default roles - use ensureRole to avoid duplicates
        Role adminRole = ensureRole("ROLE_ADMIN", "System administrator");
        Role doctorRole = ensureRole("ROLE_DOCTOR", "Doctor");
        Role deskOfficerRole = ensureRole("ROLE_DESK_OFFICER", "Desk officer");
        Role userRole = ensureRole("ROLE_USER", "Default user");

        // Seed default admin user
        User admin = userRepository.findByEmail("admin@eir.com").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setEmail("admin@eir.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setIdentityNumber("11111111111");
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole, userRole));
            userRepository.save(admin);
        } else {
            // Ensure admin has correct roles
            admin.setRoles(Set.of(adminRole, userRole));
            admin.setEnabled(true);
            userRepository.save(admin);
        }
    }

    protected Role ensureRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }

    protected User createTestUser(String email, String password, String firstName, String lastName, String identityNumber, Set<Role> roles) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIdentityNumber(identityNumber);
        user.setEnabled(true);
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
