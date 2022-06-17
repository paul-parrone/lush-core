package com.px3j.lush.app.controller;

import com.px3j.lush.core.exception.LushException;
import com.px3j.lush.core.model.Advice;
import com.px3j.lush.core.model.LushContext;
import com.px3j.lush.core.passport.Passport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * An example controller that will respond to a 'ping' request.
 *
 *  @author Paul Parrone
 */
@Slf4j
@RestController
@RequestMapping("/lush/app")
public class GuidelineController {

    public GuidelineController() {
        log.debug( "PingController created" );
    }

    /**
     * Example controller method that returns a String (wrapped by a Mono) as we are using Spring WebFlux.
     *
     * A few things to note:
     * <ul>
     *     <li>@PreAuthorize is automatically wired to recognize a Passport as authenticated.</li>
     *     <li>This controller doesn't use the Passport so it isn't a parameter, see below for how to have passport injected.</li>
     * </ul>
     *
     * @return A Mono wrapping a hard-coded String
     */
    @RequestMapping("ping")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> ping() {
        log.info( "ping() has been called" );
        return Mono.just( "Powered By Lush" );
    }

    /**
     * This method illustrates how to use the passport in a controller method.  Lush will automatically inject it if it
     * is a parameter to the method.
     *
     * @param passport The passport representing the user triggering this request.
     * @return A Mono wrapping a String that says hi.
     */
    @RequestMapping("sayHi")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> sayHi( Passport passport ) {
        return Mono.just( String.format( "%s says hi!", passport.getUsername()) );
    }

    /**
     * This method shows how Lush will intercept an exception and inject its response protocol to signal the error
     * to the caller.
     */
    @RequestMapping("uae")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> uae( Passport passport ) {
        if( true ) {
            throw new LushException( "Illustrate Exception Handling" );
        }

        return Mono.just( String.format( "%s says hi!", passport.getUsername()) );
    }

    /**
     * This method shows how to use the Lush Advice concept to return meaningful information back to your caller in
     * addition to some data response.
     */
    @RequestMapping("withAdvice")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> withAdvice( Passport passport, LushContext context ) {
        Advice advice = context.getAdvice();

        /* You can set a status code in advice - this is not the same as the HTTP Status Code */
        advice.setStatusCode( 555 );

        /* You can add extra information to return via Advice */
        advice.putExtra( "recommendation", "use Lush" );

        /* You can add warnings to advice */
        advice.addWarning( new Advice.Warning(1, Map.of( "collision", "field1,field2")));

        return Mono.just( "Advice Attached" );

    }




}
