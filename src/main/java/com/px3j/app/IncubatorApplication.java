package com.px3j.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.px3j.app.model.Cat;
import com.px3j.app.repository.CatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan({
        "com.px3j.lush.service.endpoint.http.reactive",
        "com.px3j.lush.service.endpoint.http.security.reactive",
        "com.px3j.app"
})
public class IncubatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncubatorApplication.class, args);
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
