package com.px3j.lush.illustrator.controller;

import com.px3j.lush.service.Context;
import com.px3j.lush.service.ResponseAdvice;
import com.px3j.lush.illustrator.model.Cat;
import com.px3j.lush.illustrator.repository.CatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

// service.endpoint.http
// service.endpoint.xxx

@Slf4j
@RestController
@RequestMapping("cats")
@CrossOrigin(origins="*", exposedHeaders = {"x-lush-response"} )
public class CatController  {
    private final CatRepository repository;

    @Autowired
    public CatController(CatRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "findOne", method = RequestMethod.POST)
    public Mono<Cat> findOne(@RequestBody Cat finder ) {
        log.info( "There are " + repository.count() + " cats" );

        return repository.findByName(finder.getName());
    }

    @RequestMapping( value = "findAll", method = RequestMethod.GET)
    public Flux<Cat> findAll() {
        log.info( "findAll() has been called" );
        return repository.findAll();
    }

    @RequestMapping(value = "findFail", method = RequestMethod.GET)
    public Mono<Cat> findFail() {
        if( true ) {
            throw new RuntimeException( "Some simulated unexpected exception" );
        }

        return Mono.empty();
    }

    @RequestMapping(value = "troublePost", method = RequestMethod.POST)
    public Mono<Cat> troublePost(@RequestBody Map<String,Object> someData, Context context) {
        log.info( "Some data is: " + someData.toString() );

        ResponseAdvice responseAdvice = context.getResponse();
        responseAdvice.setStatusCode(555);

        responseAdvice.addDetailCode( new ResponseAdvice.Detail(600, "This is a trouble cat") );

        return Mono.just(
                new Cat("one", "Tonkinese", "Brown", "Trouble", 1 )
        );
    }


}
