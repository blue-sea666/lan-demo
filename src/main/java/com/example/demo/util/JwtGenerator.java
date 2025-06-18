package com.example.demo.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtGenerator {

    public static void main(String[] args) {
        // 自动生成一个适合HS512算法的密钥
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        String token = generateToken("user123", secretKey);
        System.out.println("Generated JWT Token: " + token);
    }

    public static String generateToken(String username, SecretKey secretKey) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))  // 1小时后过期
                .signWith(secretKey)  // 使用自动生成的密钥
                .compact();
    }
}

