package com.kithub.dto;

import java.util.List;

public record GoogleBooksResponse(List<Item> items) {

    public record Item(
            String id,
            VolumeInfo volumeInfo
    ) {}

    public record VolumeInfo(
            String title,
            List<String> authors,
            String description,
            ImageLinks imageLinks
    ) {}

    public record ImageLinks(
            String thumbnail
    ) {}
}