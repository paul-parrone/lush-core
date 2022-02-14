package com.px3j.lush.illustrator;

import com.px3j.lush.illustrator.model.Cat;
import com.px3j.lush.illustrator.repository.CatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan( {
        "com.px3j.lush.service.endpoint.http.reactive",
        "com.px3j.lush.illustrator"
})
public class LushTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LushTestApplication.class, args);
    }

    @Autowired
    CatRepository repository;

    @PostConstruct
    private void postConstruct() {
        repository.save( new Cat(null, "Tonkinese", "Brown", "Gumball", 1) )
                .subscribe();
        repository.save( new Cat(null, "Tonkinese", "Blue", "Sneeb", 1) )
                .subscribe();

    }
}
