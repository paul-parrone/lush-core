package com.px3j.showcase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.px3j.showcase.model.Cat;
import com.px3j.showcase.repository.CatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableReactiveMongoRepositories({
        "com.px3j.showcase",
        "com.px3j.lush"
})
@ComponentScan({
        "com.px3j.lush.service.endpoint.http.reactive",
        "com.px3j.lush.service.endpoint.http.security.reactive",
        "com.px3j.showcase"
})
public class ShowcaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShowcaseApplication.class, args);
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

    @Bean
    public Gson gson() {
        return new GsonBuilder()
//                    .registerTypeAdapter(GrantedAuthority.class, new InstanceCreator<SimpleGrantedAuthority>() {
//                        @Override
//                        public SimpleGrantedAuthority createInstance(Type type) {
//                            return new SimpleGrantedAuthority("");
//                        }
//                    })
                .create();

    }

}
