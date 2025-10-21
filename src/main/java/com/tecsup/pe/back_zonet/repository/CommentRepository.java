package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}