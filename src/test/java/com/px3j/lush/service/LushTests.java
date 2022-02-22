package com.px3j.lush.service;

import com.px3j.showcase.ShowcaseApplication;
import com.px3j.lush.service.endpoint.http.security.sim.PassportUtil;
import com.px3j.lush.service.endpoint.http.Constants;
import com.px3j.showcase.model.Cat;
import com.px3j.showcase.repository.CatRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.px3j.lush.service.endpoint.http.Constants.WHO_HEADER_NAME;

@SpringBootTest( classes={ShowcaseApplication.class, PassportUtil.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class LushTests {
    private WebTestClient webTestClient;
    @Autowired private CatRepository repository;
    @Autowired private PassportUtil passportUtil;

    @Autowired
    public void setUp(ApplicationContext context) {
        webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .build();
    }

    @Test
    public void test_findOne() {
        repository.deleteAll().block();
        repository.save( new Cat(null, "Tonkinese", "Brown", "Gumball", 1) ).block();
        repository.save( new Cat(null, "Tonkinese", "Blue", "Sneeb", 1) ).block();

        Cat finder = new Cat();
        finder.setName( "Gumball" );

        String passport = passportUtil.generatePassport(Map.of("user", "paul")).block();

        webTestClient
                .post()
                .uri("/cats/findOne" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(passport));
                })
                .body( Mono.just(finder), Cat.class )

                .exchange()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Gumball");
    }

    @Test
    public void test_findAll() {
        repository.deleteAll().block();
        repository.save( new Cat(null, "Tonkinese", "Brown", "Gumball", 1) ).block();
        repository.save( new Cat(null, "Tonkinese", "Blue", "Sneeb", 1) ).block();

        String passport = passportUtil.generatePassport(Map.of("user", "paul")).block();
        log.info( passport );

        webTestClient
                .get()
                .uri("/cats/findAll" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(passport));
                })
                .exchange()
                .expectBodyList(Cat.class)
                .hasSize(2)
                .value( v -> log.info(v.toString()) );
    }


    @Test
    public void test_trouble() {
        String passport = passportUtil.generatePassport(Map.of("user", "paul")).block();

        webTestClient
                .get()
                .uri("/cats/findFail" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(passport));
                })
                .exchange()
                .expectHeader().value(
                        Constants.ADVICE_HEADER_NAME,
                        h -> log.info(h.toString())
                );
//                .expectBody(Map.class)
//                .value( m -> log.info(m.toString()) );
    }

    @Test
    public void test_troublePost() {
        String passport = passportUtil.generatePassport(Map.of("user", "paul")).block();

        webTestClient
                .post()
                .uri("/cats/troublePost" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(passport));
                })
                .body( Mono.just(Map.of("a","b")), Map.class )

                .exchange()
                .expectHeader().value(
                        Constants.ADVICE_HEADER_NAME,
                        h -> log.info(h.toString())
                );
//                .expectBody(Map.class)
//                .value( m -> log.info(m.toString()) );
    }
}
