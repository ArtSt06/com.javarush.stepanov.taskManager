package com.javarush.stepanov.taskmanager.model.repository;

import java.util.List;

import com.javarush.stepanov.taskmanager.model.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerId(Long owner);
}
