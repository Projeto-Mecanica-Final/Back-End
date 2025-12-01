package com.oficinamecanica.OficinaMecanica.security;

import com.oficinamecanica.OficinaMecanica.enums.AuthProvider;
import com.oficinamecanica.OficinaMecanica.models.UsuarioModel;
import com.oficinamecanica.OficinaMecanica.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (response.isCommitted()) {
            log.warn("Response already committed");
            return;
        }

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String providerId = oAuth2User.getAttribute("sub");

            log.info("OAuth2 authentication successful for: {}", email);

            UsuarioModel usuario = usuarioRepository.findByEmail(email).orElse(null);

            if (usuario == null) {
                log.error("User not found in database: {}", email);

                String errorUrl = UriComponentsBuilder
                        .fromUriString(frontendUrl + "/auth/login")
                        .queryParam("error", "user_not_registered")
                        .queryParam("tipo", "info")
                        .build()
                        .toUriString();

                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            if (usuario.getProvider() != AuthProvider.GOOGLE) {
                usuario.setProvider(AuthProvider.GOOGLE);
                usuario.setProviderId(providerId);
                usuarioRepository.save(usuario);
            }

            Set<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(Collectors.toSet());

            String token = tokenProvider.generateTokenFromEmailAndRoles(usuario.getEmail(), authorities);

            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("token", token)
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("Error in OAuth2 authentication: {}", e.getMessage());

            String errorUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/auth/login")
                    .queryParam("error", "google_auth_failed")
                    .queryParam("tipo", "error")
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}