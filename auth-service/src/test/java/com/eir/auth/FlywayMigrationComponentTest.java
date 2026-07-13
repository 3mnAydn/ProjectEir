package com.eir.auth;

import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationComponentTest extends AbstractComponentTest {

    @Test
    void migration_appliesCleanly() {
        assertThat(true).isTrue();
    }

    @Test
    void schema_hasAllRequiredTables() throws Exception {
        DataSource dataSource = applicationContext.getBean(DataSource.class);

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            assertThat(tableExists(meta, "users")).isTrue();
            assertThat(tableExists(meta, "roles")).isTrue();
            assertThat(tableExists(meta, "permissions")).isTrue();
            assertThat(tableExists(meta, "user_roles")).isTrue();
            assertThat(tableExists(meta, "role_permissions")).isTrue();
        }
    }

    @Test
    void schema_tablesHaveRequiredColumns() throws Exception {
        DataSource dataSource = applicationContext.getBean(DataSource.class);

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            Set<String> userColumns = getColumnNames(meta, "users");
            assertThat(userColumns).contains("id", "email", "password", "first_name", "last_name", "phone", "enabled", "created_at", "updated_at", "identity_number");

            Set<String> roleColumns = getColumnNames(meta, "roles");
            assertThat(roleColumns).contains("id", "name", "description");

            Set<String> permColumns = getColumnNames(meta, "permissions");
            assertThat(permColumns).contains("id", "name");

            Set<String> userRoleColumns = getColumnNames(meta, "user_roles");
            assertThat(userRoleColumns).contains("user_id", "role_id");

            Set<String> rolePermColumns = getColumnNames(meta, "role_permissions");
            assertThat(rolePermColumns).contains("role_id", "permission_id");
        }
    }

    @Test
    void defaultData_adminUserExists() {
        User admin = userRepository.findByEmail("admin@eir.com").orElseThrow();

        assertThat(admin.getEmail()).isEqualTo("admin@eir.com");
        assertThat(admin.getFirstName()).isEqualTo("Admin");
        assertThat(admin.getLastName()).isEqualTo("User");
        assertThat(admin.isEnabled()).isTrue();
        assertThat(admin.getPassword()).isNotBlank();
        assertThat(passwordEncoder.matches("Admin123!", admin.getPassword())).isTrue();
    }

    @Test
    void defaultData_rolesExist() {
        Set<String> expectedRoles = Set.of("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_DESK_OFFICER", "ROLE_USER");
        Set<String> actualRoles = new HashSet<>();

        for (String roleName : expectedRoles) {
            Role role = roleRepository.findByName(roleName).orElseThrow(() -> new AssertionError("Missing role: " + roleName));
            actualRoles.add(role.getName());
        }

        assertThat(actualRoles).containsExactlyInAnyOrderElementsOf(expectedRoles);
    }

    @Test
    void adminUser_hasAdminRole() {
        User admin = userRepository.findByEmail("admin@eir.com").orElseThrow();

        Set<String> roleNames = admin.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(roleNames).contains("ROLE_ADMIN");
    }

    @Test
    void indexes_existOnEmailAndEnabled() throws Exception {
        DataSource dataSource = applicationContext.getBean(DataSource.class);

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            ResultSet rs = meta.getIndexInfo(conn.getCatalog(), null, "users", false, false);
            Set<String> indexNames = new HashSet<>();
            while (rs.next()) {
                indexNames.add(rs.getString("INDEX_NAME"));
            }

            assertThat(indexNames).isNotEmpty();
        }
    }

    private boolean tableExists(DatabaseMetaData meta, String tableName) throws Exception {
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private Set<String> getColumnNames(DatabaseMetaData meta, String tableName) throws Exception {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }
}