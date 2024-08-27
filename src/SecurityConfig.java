package com.example.login.config;


import com.example.login.model.UserRepository;
import com.example.login.service.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


/**
 * SecurityConfig konfigurerar säkerhetsinställningarna för applikationen,
 * inklusive regler för inloggning, utloggning och åtkomst till olika sidor.
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Denna klass skapar en AuthenticationManager som hanterar användarinloggning med användardetaljer och lösenord.
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        return http.getSharedObject( AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    /**
     * Denna klass skapar en SecurityFilterChain som konfigurerar alla säkerhetsinställningar.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /**
         * 1.Ignorera CSRF-skydd för alla förfrågningar som matchar "/h2-console/**", vilket är viktigt för att H2-databasens webbkonsol ska fungera utan problem.
         *
         * 2. Tillåta obehindrad åtkomst till sidorna "/login", "/logout", "/register" samt H2-konsolen, vilket innebär att dessa sidor är öppna för alla användare utan inloggning.
         *
         * 3. Kräva att användaren har rollen "ADMIN" för att komma åt sidor som börjar med "/admin/**", samt sidorna "/users" och "/delete".
         *
         * 4. Kräva inloggning för alla andra sidor som inte specificeras ovan.
         * */
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/login", "/logout", "/register", "/h2-console/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/users", "/delete").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )

                /**
                 * 1. Sätt inloggningssidan: Den specifika sidan för inloggning är `/login`.
                 * När användare försöker logga in, dirigeras de till denna sida.
                 *
                 * 2. Vid framgångsrik inloggning: Om inloggningen lyckas, omdirigeras användaren till `/homepage`.
                 * Det andra argumentet `true` säkerställer att denna omdirigering alltid sker,
                 * oavsett vilken sida användaren försökte komma åt innan inloggning.
                 *
                 * 3. Vid misslyckad inloggning: Om inloggningen misslyckas,
                 * dirigeras användaren tillbaka till `/login` med en parameter `?error=true` som kan användas för att visa ett felmeddelande.
                 *
                 * 4. Tillåt all åtkomst till inloggningssidan: Sidan `/login` är tillgänglig för alla,
                 * även för användare som inte är inloggade.
                 */
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                                .defaultSuccessUrl("/homepage", true)
                                .failureUrl("/login?error=true")
                                .permitAll()
                )

                /**
                 * 1. Utloggning:
                 *    - `logoutUrl("/perform_logout")`: Anger den specifika URL:en, `/perform_logout`,
                 *    som användare ska använda för att logga ut från applikationen.
                 *    När en användare navigerar till denna URL, loggas de ut.
                 *
                 *    - `logoutSuccessUrl("/login")`: Efter att användaren har loggat ut, dirigeras de automatiskt till `/login`.
                 *    Detta gör att användaren kommer tillbaka till inloggningssidan efter utloggning.
                 *
                 *    - `permitAll()`: Tillåter alla användare att komma åt utloggnings-URL:en utan krav på inloggning.
                 *    Detta säkerställer att alla användare, oavsett om de är inloggade eller inte, kan logga ut.
                 *
                 * 2. **HTTP-headers**:
                 *    - `headers.frameOptions().disable()`: Inaktiverar `X-Frame-Options`-headern som annars hindrar att din applikation visas inuti en `<iframe>`.
                 *    Detta är användbart om du har funktioner som behöver bäddas in i en `<iframe>`, till exempel H2-konsolen,
                 *    som kräver att denna header är avstängd för att fungera korrekt.
                 */
                .logout(logout -> logout
                        .logoutUrl("/perform_logout")
                        .logoutSuccessUrl("/login")
                        .permitAll())
                .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }

    /**
     * Denna klass skapar en PasswordEncoder som använder BCrypt-algoritmen för lösenordskryptering.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Denna klass skapar en UserDetailsService som hanterar användarnas inloggningsinformation.
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new MyUserDetailsService(userRepository);
    }
}
