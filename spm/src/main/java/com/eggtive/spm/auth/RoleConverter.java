package com.eggtive.spm.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Abstraction for extracting roles from a JWT token.
 * Swap implementations to change auth providers without touching SecurityConfig.
 */
public interface RoleConverter extends Converter<Jwt, Collection<GrantedAuthority>> {
}
