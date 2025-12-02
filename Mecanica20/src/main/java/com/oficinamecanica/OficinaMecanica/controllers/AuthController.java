package com.oficinamecanica.OficinaMecanica.controllers;

import com.oficinamecanica.OficinaMecanica.dto.LoginRequestDTO;
import com.oficinamecanica.OficinaMecanica.dto.UsuarioDTO;
import com.oficinamecanica.OficinaMecanica.dto.AuthResponseDTO;
import com.oficinamecanica.OficinaMecanica.dto.UsuarioResponseDTO;
import com.oficinamecanica.OficinaMecanica.security.JwtTokenProvider;
import com.oficinamecanica.OficinaMecanica.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para login, registro e autenticação OAuth2")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Login com email e senha", description = "Retorna token JWT para autenticação")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Tentativa de login: {}", loginRequest.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.senha()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(loginRequest.email());

            AuthResponseDTO response = new AuthResponseDTO(
                    token,
                    "Bearer",
                    usuario
            );

            log.info("Login bem-sucedido: {}", loginRequest.email());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro no login: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cadastra um novo usuário LOCAL (com senha)")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody UsuarioDTO dto) {
        log.info("Registrando novo usuário: {}", dto.email());
        UsuarioResponseDTO response = usuarioService.criar(dto);
        log.info("Usuário registrado com sucesso: {}", dto.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Obter usuário autenticado", description = "Retorna dados do usuário logado (usado após OAuth2)")
    public ResponseEntity<UsuarioResponseDTO> getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Tentativa de acesso /me sem autenticação");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        log.info("Buscando dados do usuário: {}", email);

        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorEmail(email);
            log.info("Dados do usuário retornados: {}", email);
            return ResponseEntity.ok(usuario);

        } catch (Exception e) {
            log.error("Erro ao buscar usuário {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/oauth2/callback")
    @Operation(summary = "Callback OAuth2 alternativo")
    public void oauth2Callback(HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("Callback OAuth2 acionado");

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Callback OAuth2 sem autenticação");
            response.sendRedirect("http://localhost:4200/auth/login?error=no_auth");
            return;
        }

        String email = authentication.getName();
        String token = tokenProvider.generateToken(authentication);

        String redirectUrl = "http://localhost:4200/auth/callback?token=" + token;
        log.info("Redirecionando OAuth2 para: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "Callback de sucesso OAuth2")
    public ResponseEntity<String> oauth2Success() {
        log.info("OAuth2 Success endpoint acionado");
        return ResponseEntity.ok("Autenticação OAuth2 realizada com sucesso! Você pode fechar esta janela.");
    }

    @GetMapping("/oauth2/failure")
    @Operation(summary = "Callback de falha OAuth2")
    public ResponseEntity<String> oauth2Failure() {
        log.error("OAuth2 Failure endpoint acionado");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Falha na autenticação OAuth2. Tente novamente.");
    }
}