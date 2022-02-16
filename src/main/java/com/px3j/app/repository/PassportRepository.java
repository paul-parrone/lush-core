package com.px3j.app.repository;

import com.px3j.lush.service.endpoint.http.security.reactive.Passport;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PassportRepository extends ReactiveMongoRepository<Passport,String> {
}
