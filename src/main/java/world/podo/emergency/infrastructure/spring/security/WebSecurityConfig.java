package world.podo.emergency.infrastructure.spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import world.podo.emergency.application.LoginApplicationService;
import world.podo.emergency.application.LoginResponseAssembler;
import world.podo.emergency.application.MemberApplicationService;
import world.podo.emergency.application.TokenApplicationService;

import java.util.Arrays;
import java.util.Collections;

@ConditionalOnWebApplication
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final ObjectMapper objectMapper;
    private final TokenApplicationService<Long> tokenApplicationService;
    private final LoginApplicationService loginApplicationService;
    private final MemberApplicationService memberApplicationService;
    private final LoginResponseAssembler loginResponseAssembler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/api/**")
                .authorizeRequests()
                .mvcMatchers(HttpMethod.POST, "/api/members/login").permitAll()
                .anyRequest().authenticated();

        http.formLogin().disable();
        http.httpBasic().disable();

        http.logout()
                .logoutSuccessHandler(this.jsonLogoutSuccessHandler())
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/members/logout", HttpMethod.POST.name()));

        http.addFilterAt(this.todoPreAuthenticatedProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class);

        http.addFilterBefore(this.httpPostAuthenticationProcessingFilter(), BasicAuthenticationFilter.class);

        http.exceptionHandling()
                .authenticationEntryPoint(this.jsonAuthenticationEntryPoint())
                .accessDeniedHandler(this.jsonAccessDeniedHandler());

        http.sessionManagement().disable();

        http.csrf().disable();

        http.cors();
    }

    @Bean
    public AbstractPreAuthenticatedProcessingFilter todoPreAuthenticatedProcessingFilter() {
        AbstractPreAuthenticatedProcessingFilter filter = new TodoPreAuthenticatedProcessingFilter();
        filter.setAuthenticationManager(new ProviderManager(Collections.singletonList(preAuthTokenAuthenticationProvider())));
        return filter;
    }

    @Bean
    public AbstractAuthenticationProcessingFilter httpPostAuthenticationProcessingFilter() {
        AbstractAuthenticationProcessingFilter filter = new HttpPostAuthenticationProcessingFilter(
                "/api/members/login",
                HttpMethod.POST,
                objectMapper
        );
        filter.setAuthenticationManager(new ProviderManager(Collections.singletonList(httpBodyAuthenticationProvider())));
        filter.setAuthenticationSuccessHandler(this.jsonAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(this.jsonAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public PreAuthTokenAuthenticationProvider preAuthTokenAuthenticationProvider() {
        return new PreAuthTokenAuthenticationProvider(tokenApplicationService);
    }

    @Bean
    public AuthenticationProvider httpBodyAuthenticationProvider() {
        return new HttpBodyAuthenticationProvider(loginApplicationService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(Collections.singletonList(CorsConfiguration.ALL));
        configuration.setAllowedOrigins(Collections.singletonList(CorsConfiguration.ALL));
        configuration.setAllowedHeaders(Arrays.asList(
                "Accept",
                "Authorization",
                "Content-Type"
        ));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    private AuthenticationSuccessHandler jsonAuthenticationSuccessHandler() {
        return new JsonAuthenticationSuccessHandler(
                objectMapper,
                tokenApplicationService,
                memberApplicationService,
                loginResponseAssembler
        );
    }

    private AuthenticationFailureHandler jsonAuthenticationFailureHandler() {
        return new JsonAuthenticationFailureHandler(objectMapper);
    }

    private LogoutSuccessHandler jsonLogoutSuccessHandler() {
        return new JsonLogoutSuccessHandler();
    }

    private AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return new JsonAuthenticationEntryPoint(objectMapper);
    }

    private AccessDeniedHandler jsonAccessDeniedHandler() {
        return new JsonAccessDeniedHandler(objectMapper);
    }
}

