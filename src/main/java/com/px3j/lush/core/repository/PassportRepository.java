package com.px3j.lush.core.repository;

import com.px3j.lush.core.security.Passport;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PassportRepository extends ReactiveMongoRepository<Passport,String> {
}
