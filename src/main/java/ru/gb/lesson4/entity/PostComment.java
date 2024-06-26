package ru.gb.lesson4.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post_comment")
public class PostComment {

    @Setter
    @Getter
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "text")
    private String text;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
