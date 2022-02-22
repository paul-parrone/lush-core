package com.px3j.showcase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cats")
public class Cat {
    @Id
    private String id;
    private String breed;
    private String color;
    private String name;
    private int age;
}
