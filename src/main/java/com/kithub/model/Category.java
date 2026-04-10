package com.kithub.model;



import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    // Bir kategoride birçok kitap olabilir (One-to-Many)
    @OneToMany(mappedBy = "category")
    private List<Book> books;
}