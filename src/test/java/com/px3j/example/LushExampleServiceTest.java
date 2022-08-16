package com.px3j.example;

import com.google.gson.Gson;
import com.px3j.example.service.model.Cat;
import com.px3j.lush.core.model.LushAdvice;
import com.px3j.lush.core.ticket.LushTicket;
import com.px3j.example.service.LushExampleServiceApp;
import com.px3j.lush.core.ticket.TicketUtil;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.px3j.lush.endpoint.http.Constants.TICKET_HEADER_NAME;

@Slf4j
@ActiveProfiles( profiles = {"developer", "clear-ticket"})
@SpringBootTest( classes={LushExampleServiceApp.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LushExampleServiceTest {
    private WebTestClient webTestClient;
    private final TicketUtil ticketUtil;

    @Autowired
    public LushExampleServiceTest(TicketUtil ticketUtil) {
        this.ticketUtil = ticketUtil;
    }

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
        log.info( "START: testPing" );

        LushTicket ticket = new LushTicket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        webTestClient
                .get()
                .uri("/lush/example/ping" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> httpHeaders.put(
                        TICKET_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .expectBody(String.class)
                .value( s -> log.info( "Ping results: {}", s ))
                .returnResult();

        log.info( "END: testPing" );
    }

    @Test
    public void testPingUser() {
        log.info( "START: testPingUser" );

        LushTicket ticket = new LushTicket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        webTestClient
                .get()
                .uri("/lush/example/pingUser" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> httpHeaders.put(
                        TICKET_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .expectBody(String.class)
                .value( s -> log.info( "pingUser results: {}", s ))
                .returnResult();

        log.info( "END: testPingUser" );
    }

    @Test
    public void testFluxOfCats() {
        log.info( "START: testFluxOfCats" );

        LushTicket ticket = new LushTicket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        webTestClient
                .get()
                .uri("/lush/example/fluxOfCats" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> httpHeaders.put(
                        TICKET_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .expectBodyList(Cat.class)
                .value( l -> {
                    l.forEach( i -> log.info( i.toString() ) );
                })
                .returnResult();

        log.info( "END: testFluxOfCat" );
    }

    @Test
    public void testFluxOfCatsWithAdvice() {
        testFluxOfCatsWithAdviceImpl("tester");
    }

    @Test
    public void testUnexpectedException() {
        log.info( "START: testUnexpectedException" );

        LushTicket ticket = new LushTicket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        FluxExchangeResult<Map> resultFlux = webTestClient
                .get()
                .uri("/lush/example/uae")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        TICKET_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .returnResult(Map.class);


        List<String> adviceHeader = resultFlux.getResponseHeaders().get("x-lush-advice");
        if( adviceHeader != null && adviceHeader.size() != 0) {
            LushAdvice advice = new Gson().fromJson( adviceHeader.get(0), LushAdvice.class );
            log.info( "Lush LushAdvice: {}", advice.toString() );
        }

        log.info( "END: testUnexpectedException" );
    }

    @Test
    public void testUnexpectedExceptionNoLush() {
        log.info( "START: testUnexpectedExceptionNoLush" );

        LushTicket ticket = new LushTicket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        FluxExchangeResult<Map> resultFlux = webTestClient
                .get()
                .uri("/lush/example/uaeNoLush")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        TICKET_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .returnResult(Map.class);


        List<String> adviceHeader = resultFlux.getResponseHeaders().get("x-lush-advice");
        if( adviceHeader != null && adviceHeader.size() != 0) {
            LushAdvice advice = new Gson().fromJson( adviceHeader.get(0), LushAdvice.class );
            log.info( "Lush LushAdvice: {}", advice.toString() );
        }

        log.info( "END: testUnexpectedExceptionNoLush" );
    }

    @Test
    public void testXray() {
        log.info( "START: testXray" );
        String username = "paul";

        LushTicket ticket = new LushTicket(username, "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        FluxExchangeResult<String> resultFlux = webTestClient
                .get()
                .uri("/lush/example/xray")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        TICKET_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .returnResult(String.class);

        displayAdvice(resultFlux.getResponseHeaders());
        resultFlux.getResponseBody()
                .subscribe( s -> log.info( "Response body is: {}", s ));

        log.info( "END: testXray" );
    }

    @Test
    public void testWithAdviceThreaded() throws Exception {
        int numThreads = 15;
        ExecutorService executor = Executors.newFixedThreadPool( numThreads );

        for( int i=0; i<numThreads; i++ ) {
            executor.submit( () -> {
                testFluxOfCatsWithAdviceImpl(UUID.randomUUID().toString());
            });
        }

        executor.shutdown();
        executor.awaitTermination( 10, TimeUnit.SECONDS );
    }

    private void testFluxOfCatsWithAdviceImpl( String username ) {
        username = username == null ? "paul" : username;

        LushTicket ticket = new LushTicket(username, "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        Flux<Cat> data =webTestClient
                .get()
                .uri("/lush/example/fluxOfCatsWithAdvice")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        TICKET_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .expectHeader().value("x-lush-advice", this::displayAdvice)
                .returnResult(Cat.class)
                .getResponseBody();

        data.subscribe( s -> log.info( "{}", s.toString() ));
    }

    private void displayAdvice(HttpHeaders headers) {
        List<String> adviceHeader = headers.get("x-lush-advice");
        if (adviceHeader == null || adviceHeader.size() == 0) {
            log.info("No Lush LushAdvice available");
            return;
        }
        displayAdvice( adviceHeader.get(0) );
    }

    private void displayAdvice(String adviceJson) {
        log.info( "** START: Lush LushAdvice **" );
        LushAdvice advice = new Gson().fromJson( adviceJson, LushAdvice.class );

        log.info( "**        Status Code: {}", advice.getStatusCode() );
        log.info( "**        Trace Id:    {}", advice.getTraceId() );
        log.info( "**        Extras:" );
        advice.getExtras().forEach( (k,v) -> log.info( "**           extra - key: {} value: {}:", k, v ));
        log.info( "**        Warnings:" );
        advice.getWarnings().forEach( w -> {
            log.info( "**          Code: {}", w.getCode() );
            log.info( "**          Details: " );
            w.getDetail().forEach( (k,v) -> log.info( "**             warning - key: {} details: {}:", k, v ));
            log.info( "**          " );
        });

        log.info( "** END:   Lush LushAdvice **" );
    }
}
