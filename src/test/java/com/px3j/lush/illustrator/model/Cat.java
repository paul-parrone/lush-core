package com.px3j.lush.illustrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cat {
    private String breed;
    private String color;
    private String name;
    private int age;
}
