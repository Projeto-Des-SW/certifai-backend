package br.com.certifai.service.impl;

import br.com.certifai.enums.Roles;
import br.com.certifai.exception.ConflitoException;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.model.Usuario;
import br.com.certifai.repository.UsuarioRepository;
import br.com.certifai.requests.NovaSenhaRequest;
import br.com.certifai.requests.RecuperarSenhaRequest;
import br.com.certifai.service.interfaces.IAuthService;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthService implements IAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Cache<String, Boolean> tokenDenylist;

    @Value("${jwt.secret}")
    private String secretKey;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;

        this.tokenDenylist = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secretKey);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> user = usuarioRepository.findByEmail(username);
        if (user.isPresent()) {
            Usuario usuario = user.get();
            return User.builder()
                    .username(usuario.getEmail())
                    .password(usuario.getPassword())
                    .roles(getRoles(usuario))
                    .build();
        } else {
            throw new UsernameNotFoundException("Usuário não encontrado com o email: " + username);
        }
    }


    private String getRoles(Usuario user) {
        if (user == null) {
            throw new ConflitoException("É necessário escolher um perfil de usuário");
        }

        if (user.getRole() == null) {
            return Roles.ESTUDANTE.toString();
        }

        return user.getRole().toString();
    }

    @Override
    public Usuario createUser(Usuario user) {
        if (usuarioRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflitoException("Usuário já existe");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setTokenExpiresAt(LocalDateTime.now().plusHours(24));
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationToken);
        return usuarioRepository.save(user);
    }

    @Override
    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public String gerarToken(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido para gerar token");
        }

        String token = JWT.create()
                .withSubject(usuario.getEmail())
                .withClaim("roles", List.of(getRoles(usuario)))
                .withExpiresAt(LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.of("-03:00")))
                .sign(getAlgorithm());

        return token;
    }


    @Override
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }


    private DecodedJWT decodeToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        if (validateToken(token)) {
            DecodedJWT decodedJWT = decodeToken(token);
            return decodedJWT != null ? decodedJWT.getSubject() : null;
        } else {
            return null;
        }
    }

    @Override
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        if (validateToken(token)) {
            DecodedJWT decodedJWT = decodeToken(token);
            if (decodedJWT != null) {
                List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
                if (roles == null) {
                    return Collections.emptyList();
                }
                return roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Usuario> getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof Usuario usuario) {
            return Optional.of(usuario);
        }

        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            String email = oauth.getPrincipal().getAttribute("email");
            return getUsuarioByEmail(email);
        }

        if (authentication.getName() != null) {
            return getUsuarioByEmail(authentication.getName());
        }

        return Optional.empty();
    }

    public boolean verifyEmail(String token) {
        Optional<Usuario> userOptional = usuarioRepository.findByVerificationToken(token);
        if (userOptional.isEmpty()) {
            return false;
        }

        Usuario user = userOptional.get();

        if (user.getTokenExpiresAt() != null && user.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (user.isEmailVerified()) {
            return true;
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiresAt(null);
        usuarioRepository.save(user);

        return true;
    }

    @Override
    public void iniciarRecuperacaoSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String resetToken = Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(LocalDateTime.now()
                        .plusHours(1)
                        .toInstant(ZoneOffset.of("-03:00"))))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
        usuario.setResetToken(resetToken);
        usuario.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));

        usuarioRepository.save(usuario);
        emailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getName(), resetToken);
    }

    @Override
    public void invalidateToken(String token) {
        tokenDenylist.put(token, true);
    }

    @Override
    public boolean isTokenInvalid(String token) {
        return tokenDenylist.getIfPresent(token) != null;
    }

    @Override
    @Transactional
    public void resetarSenha(RecuperarSenhaRequest novaSenhaRequest) {
        String token = novaSenhaRequest.token();
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        if (novaSenhaRequest.novaSenha() == null || novaSenhaRequest.confirmarNovaSenha() == null ||
                !novaSenhaRequest.novaSenha().equals(novaSenhaRequest.confirmarNovaSenha())) {
            throw new IllegalArgumentException("Senhas inválidas.");
        }

        String email;
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            email = claims.getSubject();

            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Token inválido: email não encontrado.");
            }

        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Token inválido ou expirado.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado para o token informado."));

        usuario.setPassword(passwordEncoder.encode(novaSenhaRequest.novaSenha()));
        usuarioRepository.save(usuario);
        invalidateToken(token);
    }

    @Override
    public boolean isResetTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
