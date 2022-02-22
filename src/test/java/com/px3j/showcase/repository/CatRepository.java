package com.px3j.showcase.repository;

import com.px3j.showcase.model.Cat;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CatRepository extends ReactiveMongoRepository<Cat,String> {
    Mono<Cat> findByName(final String name );
}
