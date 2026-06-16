package com.kithub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "books")
@Data
public class Book {
    @Id
    private String id;

    @Column(length = 1000) // Başlıklar bazen 255 karakteri aşabilir, güvenceye aldık.
    private String title;

    private String author;
    private String category;
    private String isbn;

    //   Özet kısmının 2000 sınırını kaldırıp sınırsız  yaptık!
    @Column(columnDefinition = "TEXT")
    private String summary;

    private String publisher;
    private Integer publicationYear;
    private Integer pageCount;
    private String language;

    //  Resim linkleri çok uzun olabilir, burayı da sınırsız (TEXT) yaptık!
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private Float averageRating = 0.0f;
    private Integer totalReviews = 0;

    @ElementCollection
    private List<String> tags;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // Sonsuz döngü kalkanı
    @JsonIgnore
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBook> userBooks;
}