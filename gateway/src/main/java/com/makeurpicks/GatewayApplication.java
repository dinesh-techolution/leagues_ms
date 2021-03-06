package com.makeurpicks;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.util.WebUtils;

@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
@EnableCircuitBreaker
public class GatewayApplication {


	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
	
	@Configuration
	@EnableOAuth2Sso 
    protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
            	.logout()
            	.invalidateHttpSession(true)
            	 .deleteCookies("JSESSIONID")
            	 .and()
            	 
            	.authorizeRequests()
            	
                    .antMatchers("/login", "/beans", "/user", "/js/**", "/css/**", "/register.html", "/img/**", "/fonts/**").permitAll()
//                    .antMatchers(HttpMethod.POST, "/register/**")..anonymous()
                    .anyRequest().authenticated().and()
             
                .csrf()
                    .csrfTokenRepository(csrfTokenRepository()).and()
                    .addFilterBefore(new RequestContextFilter(), HeaderWriterFilter.class)
                    .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
        }
        
        

        @Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring().antMatchers("/register/**");
		}



		private Filter csrfHeaderFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class
                            .getName());
                    if (csrf != null) {
                        Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                        String token = csrf.getToken();
                        if (cookie == null || token != null
                                && !token.equals(cookie.getValue())) {
                            cookie = new Cookie("XSRF-TOKEN", token);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                    }
                    filterChain.doFilter(request, response);
                }
            };
        }

        private CsrfTokenRepository csrfTokenRepository() {
            HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
            repository.setHeaderName("X-XSRF-TOKEN");
            return repository;
        }

    }

}