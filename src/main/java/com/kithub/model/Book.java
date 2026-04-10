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

    @Column(length = 2000) // Uzun özetler için
    private String summary;

    private String publisher;
    private Integer publicationYear;
    private Integer pageCount;
    private String language;
    private String imageUrl; // Kitap kapağı için URL

    // AI için etiketler (Örn: "distopya", "yapay zeka", "macera")
    @ElementCollection
    private List<String> tags;

    @ManyToOne
    private Category category; // Kategori tablosuyla ilişki
}
