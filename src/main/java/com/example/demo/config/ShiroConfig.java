package com.example.demo.config;

import com.example.demo.filter.JwtFilter;
import com.example.demo.realm.JwtRealm;
import com.example.demo.util.JwtUtil;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    private final JwtUtil jwtUtil;

    // 关键修复1：添加@Autowired注解
    @Autowired
    public ShiroConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 关键修复2：添加无参构造函数
    public ShiroConfig() {
        this.jwtUtil = null; // 临时解决方案，确保CGLIB代理能创建实例
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);

        // 添加JWT过滤器
        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", new JwtFilter(jwtUtil)); // 使用注入的jwtUtil
        factoryBean.setFilters(filters);

        // 设置访问规则
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/api/auth/login", "anon");
        filterMap.put("/api/auth/register", "anon");
        filterMap.put("/api/**", "jwt");
        filterMap.put("/appInfo/about", "anon");
        factoryBean.setFilterChainDefinitionMap(filterMap);

        return factoryBean;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(JwtRealm jwtRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jwtRealm);

        // 关闭Shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        return securityManager;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}