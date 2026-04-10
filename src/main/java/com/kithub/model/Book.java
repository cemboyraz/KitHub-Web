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

    // Performans için puanlama verileri
    private Float averageRating = 0.0f;
    private Integer totalReviews = 0;

    @ElementCollection
    private List<String> tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingStatus> readingStatuses;
}