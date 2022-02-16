package com.px3j.lush.illustrator.tests;

import com.google.gson.Gson;
import com.px3j.app.IncubatorApplication;
import com.px3j.lush.core.security.Actor;
import com.px3j.lush.service.endpoint.http.Constants;
import com.px3j.app.model.Cat;
import com.px3j.app.repository.CatRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.px3j.lush.service.endpoint.http.Constants.WHO_HEADER_NAME;

@SpringBootTest( classes={IncubatorApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class LushTests {
    private WebTestClient webTestClient;
    @Autowired private CatRepository repository;

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

        String token = login("paul");

        webTestClient
                .post()
                .uri("/cats/findOne" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(token));
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

        String token = login("paul");

        webTestClient
                .get()
                .uri("/cats/findAll" )
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(token));
                })
                .exchange()
                .expectBodyList(Cat.class)
                .hasSize(2)
                .value( v -> log.info(v.toString()) );
    }


    @Test
    public void test_trouble() {
        String token = login("paul");

        webTestClient
                .get()
                .uri("/cats/findFail" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(token));
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
        String token = login("paul");

        webTestClient
                .post()
                .uri("/cats/troublePost" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> {
                    httpHeaders.put( WHO_HEADER_NAME, List.of(token));
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

    private String login( final String user ) {
        return webTestClient
                .post()
                .uri("/oauth/sim/login")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(Map.of("user", "lush")), Map.class)

                .exchange()
                .returnResult(String.class)
                .getResponseBody().blockFirst();
    }
}
