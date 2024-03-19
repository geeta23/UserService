package com.example.UseService.Service;

import com.example.UseService.Repositories.SessionRepository;
import com.example.UseService.Repositories.UserRepository;
import com.example.UseService.model.Session;
import com.example.UseService.model.SessionStatus;
import com.example.UseService.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    // to encrypt the password before storing in db
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SecretKey secret;

    public User signUp(String email, String password){
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty())
        {
            User user = new User();
            user.setEmail(email);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            User savedUser = userRepository.save(user);
            return savedUser;
        }
        return userOptional.get();
    }

    public Pair<User, MultiValueMap<String, String>> login(String email, String password){
        Optional<User> userOptional = userRepository.findByEmail(email);

            if(userOptional.isEmpty()) {
                return null;
            }
            User user = userOptional.get();
            //if(!user.getPassword().equals(password))
            // as we encrypted the password we can use direct check for password instead use the below
            // encrypt both password and user.getPassword()
            if(!bCryptPasswordEncoder.matches(password, user.getPassword()))
                return null;

            //Token generation
//            String message = "{\n" +
//                    " \"email\": \"geeta@gmail.com\", \n" +
//                    " \"roles\" : [\n" +
//                    "   \"instructor\", \n" +
//                    "   \"buddy\"\n" +
//                    "   ],\n" +
//                    "   \"expirationDate\": \"2ndApril2024\"\n" +
//                    "}";
//            byte[] content = message.getBytes(StandardCharsets.UTF_8);

            Map<String, Object>jwtData = new HashMap<>();
            jwtData.put("email", user.getEmail());
            jwtData.put("roles", user.getRoles());
            long nowInMilliSecond = System.currentTimeMillis();
            jwtData.put("expiryTime", new Date(nowInMilliSecond + 1000000000));
            jwtData.put("createdAt", new Date(nowInMilliSecond));

            // refer to notes of token generation
            //jwts: json web token
            //String token = Jwts.builder().content(content).signWith(secret).compact();

            String token = Jwts.builder().claims(jwtData).signWith(secret).compact();

            Session session = new Session();
            session.setSessionStatus(SessionStatus.ACTIVE);
            session.setUser(user);
            session.setExpiringAt(new Date(nowInMilliSecond + 10000));
            session.setToken(token);
            sessionRepository.save(session);


            // add the token in cookies, cookies underlying ds is hashmap
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add(HttpHeaders.SET_COOKIE, token);
            return new Pair<User, MultiValueMap<String, String>>(user, headers);
        }

        // this validation is done in resource server when client comes with request but
        // few companies do it in auth server also, it is up to companies way to put it
        public Boolean validateToken(String token, Long user_id){
            Optional<Session> optionalSession = sessionRepository.findByTokenAndUser_Id(token, user_id);

            if(optionalSession.isEmpty()){
                System.out.println("No token found!");
                return false;
            }

            Session session = optionalSession.get();
            String storedToken = session.getToken();

            // deserializing the token with common secret in SpringSecurity
            // unsign with secret
            JwtParser jwtParser = Jwts.parser().verifyWith(secret).build();
            // and get the payload
            Claims claims = jwtParser.parseSignedClaims(storedToken).getPayload();

            long nowInMills = System.currentTimeMillis();
            long tokenExpiry = (Long)claims.get("expiryTime");

            if(nowInMills > tokenExpiry)
            {
                System.out.println("current time " + nowInMills);
                System.out.println("Expiry time " + tokenExpiry);
                System.out.println("Token expired!");
                return false;
            }

            Optional<User> optionalUser = userRepository.findById(user_id);
            if(optionalUser.isEmpty()){
                return false;
            }
            String email = optionalUser.get().getEmail();

            if(!email.equals(claims.get("email"))){
                System.out.println("user doesn't match");
                return false;
            }

            return true;
        }
    }
