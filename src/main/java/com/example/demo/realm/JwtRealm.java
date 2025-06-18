package com.example.demo.realm;

import com.example.demo.entity.User;
import com.example.demo.filter.JwtToken;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Component
public class JwtRealm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        System.out.println(userService);
        User user = userService.findByUsername(username);

        // 添加角色和权限
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        if (user != null && user.getRole() != null) {
            roles.add(user.getRole());

            // 根据角色添加权限
            if ("admin".equals(user.getRole())) {
                permissions.add("user:manage");
                permissions.add("data:view");
            } else {
                permissions.add("data:view");
            }
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(roles);
        info.setStringPermissions(permissions);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {

        JwtToken jwtToken = (JwtToken) token;
        String jwt = (String) jwtToken.getCredentials();

        Claims claims = jwtUtil.parseToken(jwt);
        if (claims == null) {
            throw new AuthenticationException("Invalid token");
        }

        if (jwtUtil.isTokenExpired(claims.getExpiration())) {
            throw new ExpiredCredentialsException("Token已过期,请重新登录");
        }

        String username = claims.getSubject();
        String redisKey = "token_" + username;
        String storedToken = redisTemplate.opsForValue().get(redisKey);

        if (storedToken == null || !storedToken.equals(jwt)) {
            throw new AuthenticationException("Invalid token");
        }

        return new SimpleAuthenticationInfo(username, jwt, getName());
    }
}