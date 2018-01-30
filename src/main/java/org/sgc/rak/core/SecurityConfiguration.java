package org.sgc.rak.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * App application configuration related to security.
 */
@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Environment environment;

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Autowired
    public SecurityConfiguration(Environment environment) {
        this.environment = environment;
    }

    /**
     * For now we only have a single admin account.  In the future we may need to migrate to JDBC-based
     * authentication.
     *
     * @param auth The authentication manager builder.
     * @throws Exception If an error occurs.
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        String user = this.environment.getProperty("rak.login.user");
        String password = this.environment.getProperty("rak.login.password");

        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
            auth.inMemoryAuthentication()
                .withUser(user).password(password)
                    .authorities("ROLE_USER", "ROLE_ADMIN");
            LOGGER.info("User was configured");
        }
        else {
            LOGGER.warn("No users were configured.  Logging in will not be possible");
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .httpBasic()
            .and()
                .authorizeRequests()
                    .anyRequest().permitAll()
            .and()
                .csrf()
                    // CookieCsrfTokenRepository uses the default cookie & header names as axios
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        // @formatter:on
    }
}