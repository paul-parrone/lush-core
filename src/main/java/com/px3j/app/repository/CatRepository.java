package com.px3j.app.repository;

import com.px3j.app.model.Cat;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CatRepository extends ReactiveMongoRepository<Cat,String> {
    Mono<Cat> findByName(final String name );
}
