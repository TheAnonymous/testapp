package org.jakoboesterling.test.service.impl

import java.util.Optional
import org.jakoboesterling.test.domain.Task
import org.jakoboesterling.test.repository.TaskRepository
import org.jakoboesterling.test.service.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Task].
 */
@Service
@Transactional
class TaskServiceImpl(
    private val taskRepository: TaskRepository
) : TaskService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a task.
     *
     * @param task the entity to save.
     * @return the persisted entity.
     */
    override fun save(task: Task): Task {
        log.debug("Request to save Task : {}", task)
        return taskRepository.save(task)
    }

    /**
     * Get all the tasks.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): MutableList<Task> {
        log.debug("Request to get all Tasks")
        return taskRepository.findAll()
    }

    /**
     * Get one task by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Task> {
        log.debug("Request to get Task : {}", id)
        return taskRepository.findById(id)
    }

    /**
     * Delete the task by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete Task : {}", id)

        taskRepository.deleteById(id)
    }
}
