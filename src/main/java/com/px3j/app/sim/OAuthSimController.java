package com.px3j.app.sim;

import com.google.gson.Gson;
import com.px3j.lush.core.repository.PassportRepository;
import com.px3j.lush.core.security.Actor;
import com.px3j.lush.core.security.Passport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

//TODO: AuthService later...

@Slf4j
@RestController
@RequestMapping( "/oauth/sim" )
public class OAuthSimController {
    private final PassportRepository passportRepository;

    @Autowired
    public OAuthSimController(PassportRepository passportRepository) {
        this.passportRepository = passportRepository;
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Mono<String> login(@RequestBody Map<String,String> params) {
        String user = params.get("user");
        log.info( "Simulated login of user: " + user);

        Actor actor = new Actor(user == null ? "unknown" : user, "", List.of(new SimpleGrantedAuthority("user")));
        Base64.getEncoder().encode( new Gson().toJson(new Passport(actor)).getBytes(StandardCharsets.UTF_8) );

        Mono<Passport> ticketMono = passportRepository.save(new Passport(actor));
        return ticketMono.map( t -> {
            String asJson = new Gson().toJson(t);
            return new String(Base64.getEncoder().encode( asJson.getBytes(StandardCharsets.UTF_8) ));
        });
    }

}
