package com.example.bootshelf.boardsvc.boardtag.repository;

import com.example.bootshelf.boardsvc.boardtag.model.entity.BoardTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardTagRepository extends JpaRepository<BoardTag, Integer> {
    List<BoardTag> findAllByIdx(Integer idx);
}