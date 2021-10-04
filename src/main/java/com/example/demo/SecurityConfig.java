package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.sessionManagement().sessionAuthenticationStrategy(sas())
                .and().addFilterAt(concurrentSessionFilter(), ConcurrentSessionFilter.class)
                .addFilterAt(myAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public UsernamePasswordAuthenticationFilter myAuthFilter() throws Exception {
        UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter();
        usernamePasswordAuthenticationFilter.setSessionAuthenticationStrategy(sas());
        usernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());
        return usernamePasswordAuthenticationFilter;
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionInformationExpiredStrategy redirectSessionInformationExpiredStrategy() {
        return new SimpleRedirectSessionInformationExpiredStrategy("/login");
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    //自定義的ConcurrentSessionFilter，檢查到重覆登入時，在此登出。
    @Bean
    public MyConcurrentSessionFilter concurrentSessionFilter() {
        MyConcurrentSessionFilter myConcurrentSessionFilter = new MyConcurrentSessionFilter(sessionRegistry(), redirectSessionInformationExpiredStrategy());
        myConcurrentSessionFilter.setLogoutHandlers(Arrays.asList(new SecurityContextLogoutHandler(), myLogoutHandler()));
        return myConcurrentSessionFilter;
    }

    //登入時，AbstractAuthenticationProcessingFilter會自動呼叫delegateStrategies中策略
    @Bean
    public SessionAuthenticationStrategy sas() {
        ConcurrentSessionControlAuthenticationStrategy concurrentSessionControlAuthenticationStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        concurrentSessionControlAuthenticationStrategy.setMaximumSessions(2);

        List<SessionAuthenticationStrategy> delegateStrategies = new ArrayList<>();
        delegateStrategies.add(concurrentSessionControlAuthenticationStrategy);
        delegateStrategies.add(new SessionFixationProtectionStrategy());
        delegateStrategies.add(new RegisterSessionAuthenticationStrategy(sessionRegistry()));
        delegateStrategies.add(mySessionAuthenticationStrategy());
        CompositeSessionAuthenticationStrategy sas = new CompositeSessionAuthenticationStrategy(delegateStrategies);
        return sas;
    }

    //自定義的登入策略
    @Bean
    public MySessionAuthenticationStrategy mySessionAuthenticationStrategy() {
        return new MySessionAuthenticationStrategy();
    }

    //自定義登出處理器
    @Bean
    public LogoutHandler myLogoutHandler() {
        return new MyLogoutHandler();
    }
}
