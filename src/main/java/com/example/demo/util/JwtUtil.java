package com.example.demo.util;

import io.jsonwebtoken.*;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    @Value("${jwt.refresh}")
    private long refresh;

    public String generateToken(String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire * 1000);

        // 将 secret 密钥转换为 byte 数组
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS512.getJcaName());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(secretKey)  // 使用从配置文件中读取的密钥
                .compact();
    }


    public Claims parseToken(String token) {
        // 将 secret 密钥转换为 byte 数组
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS512.getJcaName());
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)  // 确保这里使用的是正确的密钥
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Token 已过期
            throw new ExpiredCredentialsException("Expired or invalid JWT token");
        } catch (JwtException e) {
            // 其他解析错误
            return null;
        }
    }


    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    public long getExpire() {
        return expire;
    }

    public long getRefresh() {
        return refresh;
    }
}