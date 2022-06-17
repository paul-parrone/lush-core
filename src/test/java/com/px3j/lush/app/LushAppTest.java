package com.px3j.lush.app;

import com.google.gson.Gson;
import com.px3j.lush.core.model.Advice;
import com.px3j.lush.core.passport.Passport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static com.px3j.lush.endpoint.http.Constants.WHO_HEADER_NAME;

@Slf4j
@SpringBootTest( classes={LushApp.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LushAppTest {
    private WebTestClient webTestClient;

    @Autowired
    public void setUp(ApplicationContext context) {
        webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .build();
    }

    @Test
    void contextLoads() {
        // empty test that would fail if our Spring configuration does not load correctly
    }

    @Test
    public void testPing() {
        Passport passport = new Passport("paul", "", List.of(new SimpleGrantedAuthority("user")));

        final String encodedPassport = passport.encode();
//        String asJson = new Gson().toJson(passport);
//        final String encodedPassport = new String(Base64.getEncoder().encode( asJson.getBytes(StandardCharsets.UTF_8) ));

        webTestClient
                .get()
                .uri("/lush/app/ping" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedPassport)
                ))
                .exchange()
                .expectBody(String.class)
                .value( s -> log.info( "Ping results: {}", s ));
    }

    @Test
    public void testSayHi() {
        Passport passport = new Passport("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedPassport = passport.encode();

        webTestClient
                .get()
                .uri("/lush/app/sayHi" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedPassport)
                ))
                .exchange()
                .expectBody(String.class)
                .value( s -> log.info( "sayHi results: {}", s ));

    }

    @Test
    public void testUnexpectedException() {
        Passport passport = new Passport("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedPassport = passport.encode();

        FluxExchangeResult<Map> resultFlux = webTestClient
                .get()
                .uri("/lush/app/uae")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedPassport)
                ))
                .exchange()
                .returnResult(Map.class);


        List<String> adviceHeader = resultFlux.getResponseHeaders().get("x-lush-advice");
        if( adviceHeader != null && adviceHeader.size() != 0) {
            Advice advice = new Gson().fromJson( adviceHeader.get(0), Advice.class );
            log.info( "Lush Advice: {}", advice.toString() );
        }
    }

    @Test
    public void testWithAdvice() {
        Passport passport = new Passport("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedPassport = passport.encode();

        FluxExchangeResult<String> resultFlux = webTestClient
                .get()
                .uri("/lush/app/withAdvice")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedPassport)
                ))
                .exchange()
                .returnResult(String.class);

        displayAdvice(resultFlux.getResponseHeaders());
        resultFlux.getResponseBody()
                .subscribe( s -> log.info( "Response body is: {}", s ));
    }

    private void displayAdvice(HttpHeaders headers ) {
        List<String> adviceHeader = headers.get("x-lush-advice");
        if( adviceHeader == null || adviceHeader.size() == 0) {
            log.info( "No Lush Advice available" );
            return;
        }

        log.info( "** START: Lush Advice **" );
        Advice advice = new Gson().fromJson( adviceHeader.get(0), Advice.class );

        log.info( "**        Status Code: {}", advice.getStatusCode() );
        log.info( "**        Trace Id: {}", advice.getTraceId() );
        log.info( "**" );
        log.info( "**        Extras:" );
        advice.getExtras().forEach( (k,v) -> log.info( "**          {}/{}:", k, v ));

        log.info( "**" );
        log.info( "**        Warnings:" );
        advice.getWarnings().forEach( w -> {
            log.info( "**          Code: {}", w.getCode() );
            log.info( "**          Details: " );
            w.getDetail().forEach( (k,v) -> log.info( "**            key: {} value: {}:", k, v ));
            log.info( "**          " );
        });

        log.info( "** END:   Lush Advice **" );
    }

}
