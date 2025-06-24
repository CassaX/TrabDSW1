package DSW.TrabalhoDSW_Veiculos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import DSW.TrabalhoDSW_Veiculos.security.ClienteDetailsServiceimpl;

@Configuration
public class SecurityConfig {

    @Autowired
    private ClienteDetailsServiceimpl usuarioDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas
                        .requestMatchers("/", "/home", "/error", "/login", "/registro", "/registro/**").permitAll()
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/veiculos", "/veiculos/listar").permitAll()

                        // Admin (AGORA O ADMIN PODE ACESSAR TUDO RELACIONADO A CLIENTE E LOJA)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cliente/**").hasRole("ADMIN") // ADMIN também pode gerenciar
                                                                                       // clientes
                        .requestMatchers("/loja/**").hasRole("ADMIN") // ADMIN também pode gerenciar lojas

                        // Cliente (acesso restrito aos CLIENTES, exceto se for ADMIN)
                        // Se um CLIENTE comum tentar acessar /cliente/listar, ele precisa ter a role
                        // CLIENTE.
                        // Mas o ADMIN também poderá, pois já demos acesso acima.
                        .requestMatchers("/propostas/criar/**", "/propostas/minhas-propostas").hasRole("CLIENTE")

                        // Loja (acesso restrito às LOJAS, exceto se for ADMIN)
                        .requestMatchers("/veiculos/cadastrar", "/veiculos/meus-veiculos", "/veiculos/editar/**")
                        .hasRole("LOJA")
                        .requestMatchers("/propostas/loja/**", "/propostas/editar-status/**").hasRole("LOJA")

                        // Rotas compartilhadas (ADMIN, CLIENTE, LOJA - dependendo do contexto)
                        .requestMatchers("/propostas/salvar").hasAnyRole("CLIENTE", "LOJA", "ADMIN") // ADMIN também
                                                                                                     // pode salvar
                                                                                                     // proposta

                        // Qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .userDetailsService(usuarioDetailsService)
                .requestCache(cache -> cache
                        .requestCache(httpSessionRequestCache()));

        return http.build();
    }

    @Bean
    public HttpSessionRequestCache httpSessionRequestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(new NegatedRequestMatcher(
                new OrRequestMatcher(
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**"),
                        new AntPathRequestMatcher("/images/**"),
                        new AntPathRequestMatcher("/webjars/**"),
                        new AntPathRequestMatcher("/**.ico"),
                        new AntPathRequestMatcher("/**.png"),
                        new AntPathRequestMatcher("/**.gif"),
                        new AntPathRequestMatcher("/**.jpg"),
                        new AntPathRequestMatcher("/**.jpeg"),
                        new AntPathRequestMatcher("/**.svg"),
                        new AntPathRequestMatcher("/**.woff"),
                        new AntPathRequestMatcher("/**.woff2"),
                        new AntPathRequestMatcher("/**.ttf"))));
        requestCache.setCreateSessionAllowed(true);
        return requestCache;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}