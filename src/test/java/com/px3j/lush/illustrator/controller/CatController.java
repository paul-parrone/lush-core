package com.px3j.lush.illustrator.controller;

import com.px3j.lush.core.api.ApiContext;
import com.px3j.lush.core.api.ApiResponse;
import com.px3j.lush.illustrator.model.Cat;
import com.px3j.lush.illustrator.repository.CatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("cats")
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
    public Mono<Cat> troublePost(@RequestBody Map<String,Object> someData, ApiContext apiContext ) {
        log.info( "Some data is: " + someData.toString() );

        ApiResponse apiResponse = apiContext.getResponse();
        apiResponse.setStatusCode(555);
        apiResponse.setDisplayableMessage( "This is a trouble cat" );

        return Mono.just(
                new Cat("one", "Tonkinese", "Brown", "Trouble", 1 )
        );
    }


}
