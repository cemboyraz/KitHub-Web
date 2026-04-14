package com.kithub.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "books")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // google apiden gelen gibi bişey bu
    @Column(unique = true)
    private String googleBooksId;

    private String title;
    private String author;
    private String isbn;

    @Column(length = 2000)
    private String summary;

    private String publisher;
    private Integer publicationYear;
    private Integer pageCount;
    private String language;
    private String imageUrl;

    private Float averageRating = 0.0f;
    private Integer totalReviews = 0;

    @ElementCollection
    private List<String> tags;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBook> userBooks;
}