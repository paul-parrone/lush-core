package com.px3j.lush.illustrator.repository;

import com.px3j.lush.illustrator.model.Cat;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Mono;

public interface CatRepository extends ReactiveMongoRepository<Cat,String> {
    public Mono<Cat> findByName(final String name );
}
