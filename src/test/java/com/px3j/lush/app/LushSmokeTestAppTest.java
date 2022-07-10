package com.px3j.lush.app;

import com.google.gson.Gson;
import com.px3j.lush.app.inbound.jms.ExampleSender;
import com.px3j.lush.core.model.Advice;
import com.px3j.lush.core.ticket.Ticket;
import com.px3j.lush.core.ticket.TicketUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.px3j.lush.endpoint.http.Constants.WHO_HEADER_NAME;

@Slf4j
//@WebFluxTest()
//@Import(com.px3j.lush.core.config.LushCoreConfig.class)
@ActiveProfiles( profiles = {"developer"})
@SpringBootTest( classes={LushSmokeTestApp.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LushSmokeTestAppTest {
    private WebTestClient webTestClient;
    private final TicketUtil ticketUtil;

    private final ExampleSender exampleSender;

    private final Tracer tracer;

    @Autowired
    public LushSmokeTestAppTest(TicketUtil ticketUtil, ExampleSender exampleSender, Tracer tracer) {
        this.ticketUtil = ticketUtil;
        this.exampleSender = exampleSender;
        this.tracer = tracer;
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
    void testJmsSend() {
        final Span span = tracer.nextSpan();
        final Ticket ticket = new Ticket("paul", "", List.of(new SimpleGrantedAuthority("user")));

        try(Tracer.SpanInScope spanInScope = tracer.withSpan(span)) {
            log.info( "About to send a JMS message" );
            exampleSender.send("jms.message.endpoint", ticket, "first message" );
            exampleSender.send("jms.message.endpoint", ticket, "second message" );
        }

    }


    @Test
    public void testPing() {
        Ticket ticket = new Ticket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        webTestClient
                .get()
                .uri("/lush/app/ping" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .expectBody(String.class)
                .value( s -> log.info( "Ping results: {}", s ));
    }

    @Test
    public void testSayHi() {
        Ticket ticket = new Ticket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        webTestClient
                .get()
                .uri("/lush/app/sayHi" )
                .accept(MediaType.APPLICATION_JSON)
                .headers( httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .expectBody(String.class)
                .value( s -> log.info( "sayHi results: {}", s ));

    }

    @Test
    public void testUnexpectedException() {
        Ticket ticket = new Ticket("paul", "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        FluxExchangeResult<Map> resultFlux = webTestClient
                .get()
                .uri("/lush/app/uae")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedTicket)
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
    public void testXray() {
        String username = "paul";

        Ticket ticket = new Ticket(username, "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        FluxExchangeResult<String> resultFlux = webTestClient
                .get()
                .uri("/lush/app/xray")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedTicket)
                ))
                .exchange()
                .returnResult(String.class);

        displayAdvice(resultFlux.getResponseHeaders());
        resultFlux.getResponseBody()
                .subscribe( s -> log.info( "Response body is: {}", s ));
    }

    @Test
    public void testWithAdviceThreaded() throws Exception {
        int numThreads = 15;
        ExecutorService executor = Executors.newFixedThreadPool( numThreads );

        for( int i=0; i<numThreads; i++ ) {
            executor.submit( () -> {
                testWithAdvice(UUID.randomUUID().toString());
            });
        }

        executor.shutdown();
        executor.awaitTermination( 10, TimeUnit.SECONDS );
    }

//    @Test
    public void testWithAdvice( String username ) {
        username = username == null ? "paul" : username;

        Ticket ticket = new Ticket(username, "", List.of(new SimpleGrantedAuthority("user")));
        final String encodedTicket = ticketUtil.encrypt(ticket);

        FluxExchangeResult<String> resultFlux = webTestClient
                .get()
                .uri("/lush/app/withAdvice")
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.put(
                        WHO_HEADER_NAME,
                        List.of(encodedTicket)
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
