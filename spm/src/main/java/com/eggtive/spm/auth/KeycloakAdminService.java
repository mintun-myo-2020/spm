package com.eggtive.spm.auth;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Calls the Keycloak Admin REST API to create users and assign realm roles.
 * Uses client_credentials grant via the spm-backend confidential client.
 */
@Service
public class KeycloakAdminService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminService.class);

    private final RestClient restClient;
    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private long tokenExpiresAt; // epoch millis

    public KeycloakAdminService(
            @Value("${app.keycloak.server-url}") String serverUrl,
            @Value("${app.keycloak.realm}") String realm,
            @Value("${app.keycloak.admin-client-id}") String clientId,
            @Value("${app.keycloak.admin-client-secret}") String clientSecret) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.builder().build();
    }

    /**
     * Creates a user in Keycloak with the given credentials and role.
     * Returns the Keycloak user ID (sub claim).
     */
    public String createUser(String email, String firstName, String lastName, String password, String roleName) {
        String token = getAdminToken();
        String keycloakId = createKeycloakUser(token, email, firstName, lastName, password);
        assignRealmRole(token, keycloakId, roleName);
        return keycloakId;
    }

    /**
     * Deletes a user from Keycloak. Used for rollback if DB save fails.
     */
    public void deleteUser(String keycloakId) {
        try {
            String token = getAdminToken();
            restClient.delete()
                .uri(serverUrl + "/admin/realms/{realm}/users/{id}", realm, keycloakId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to rollback Keycloak user {}: {}", keycloakId, e.getMessage());
        }
    }

    /**
     * Enables or disables a user in Keycloak.
     */
    public void setUserEnabled(String keycloakId, boolean enabled) {
        try {
            String token = getAdminToken();
            restClient.put()
                .uri(serverUrl + "/admin/realms/{realm}/users/{id}", realm, keycloakId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", enabled))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to set enabled={} for Keycloak user {}: {}", enabled, keycloakId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized String getAdminToken() {
        // Return cached token if still valid (with 30s buffer)
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAt - 30_000) {
            return cachedToken;
        }

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        try {
            Map<String, Object> response = restClient.post()
                .uri(serverUrl + "/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to obtain admin token");
            }

            cachedToken = (String) response.get("access_token");
            int expiresIn = response.containsKey("expires_in") ? ((Number) response.get("expires_in")).intValue() : 60;
            tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L);

            return cachedToken;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak authentication failed", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Authentication service unavailable");
        }
    }

    private String createKeycloakUser(String token, String email, String firstName, String lastName, String password) {
        Map<String, Object> userRep = Map.of(
            "username", email,
            "email", email,
            "firstName", firstName,
            "lastName", lastName,
            "enabled", true,
            "emailVerified", true,
            "credentials", List.of(Map.of(
                "type", "password",
                "value", password,
                "temporary", false
            ))
        );

        try {
            var response = restClient.post()
                .uri(serverUrl + "/admin/realms/{realm}/users", realm)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userRep)
                .retrieve()
                .toBodilessEntity();

            // Keycloak returns the user ID in the Location header
            String location = response.getHeaders().getFirst("Location");
            if (location == null) {
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Keycloak did not return user location");
            }
            return location.substring(location.lastIndexOf('/') + 1);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("409")) {
                throw new AppException(ErrorCode.CONFLICT, "User already exists in identity provider");
            }
            log.error("Failed to create Keycloak user for email {}", email, e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "User creation failed");
        }
    }

    @SuppressWarnings("unchecked")
    private void assignRealmRole(String token, String keycloakId, String roleName) {
        // Look up the role representation
        Map<String, Object> role = restClient.get()
            .uri(serverUrl + "/admin/realms/{realm}/roles/{role}", realm, roleName)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

        if (role == null) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Role assignment failed");
        }

        // Assign the role to the user
        restClient.post()
            .uri(serverUrl + "/admin/realms/{realm}/users/{id}/role-mappings/realm", realm, keycloakId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(List.of(role))
            .retrieve()
            .toBodilessEntity();
    }
}
