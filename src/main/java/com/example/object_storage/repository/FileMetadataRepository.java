package com.example.object_storage.repository;

import com.example.object_storage.entity.FileMetadata;
import com.example.object_storage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByOwner(User owner);
    Optional<FileMetadata> findByDownloadLink(String downloadLink);
    Optional<FileMetadata> findByIdAndOwner(Long id, User owner);
}