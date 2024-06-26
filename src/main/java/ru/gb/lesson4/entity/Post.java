package ru.gb.lesson4.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post")
public class Post {

    @Setter
    @Getter
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "title")
    private String title;

    public Post() {

    }
}
