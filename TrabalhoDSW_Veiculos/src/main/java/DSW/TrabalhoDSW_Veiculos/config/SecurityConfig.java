package DSW.TrabalhoDSW_Veiculos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers("/", "/home", "/error", "/login", "/registro", "/registro/**").permitAll()
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/veiculos", "/veiculos/listar").permitAll()
                        .requestMatchers("/veiculos/imagem/**", "/veiculos/veiculo/**").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cliente/**").hasRole("ADMIN")
                        .requestMatchers("/loja/**").hasRole("ADMIN")

                        .requestMatchers("/propostas/criar/**", "/propostas/minhas-propostas").hasRole("CLIENTE")


                        .requestMatchers(HttpMethod.GET, "/veiculos/cadastrar", "/veiculos/meus-veiculos", "/veiculos/editar/**").hasRole("LOJA")
                        .requestMatchers(HttpMethod.POST, "/veiculos/salvar", "/veiculos/editar").hasRole("LOJA")
                        .requestMatchers(HttpMethod.GET, "/veiculos/excluir/**").hasRole("LOJA") 
                        .requestMatchers(HttpMethod.POST, "/veiculos/excluir/**").hasRole("LOJA") 

                        .requestMatchers("/propostas/loja/**", "/propostas/editar-status/**").hasRole("LOJA")

                        .requestMatchers("/propostas/salvar").hasAnyRole("CLIENTE", "LOJA", "ADMIN")


                        .anyRequest().authenticated()
                )
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
                        .requestCache(httpSessionRequestCache()))
                .csrf(csrf -> csrf.disable()); 

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