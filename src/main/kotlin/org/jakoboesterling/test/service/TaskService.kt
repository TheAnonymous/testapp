package org.jakoboesterling.test.service

import java.util.Optional
import org.jakoboesterling.test.domain.Task

/**
 * Service Interface for managing [Task].
 */
interface TaskService {

    /**
     * Save a task.
     *
     * @param task the entity to save.
     * @return the persisted entity.
     */
    fun save(task: Task): Task

    /**
     * Get all the tasks.
     *
     * @return the list of entities.
     */
    fun findAll(): MutableList<Task>

    /**
     * Get the "id" task.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Task>

    /**
     * Delete the "id" task.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
