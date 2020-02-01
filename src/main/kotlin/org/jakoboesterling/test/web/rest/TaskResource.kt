package org.jakoboesterling.test.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.jakoboesterling.test.domain.Task
import org.jakoboesterling.test.service.TaskService
import org.jakoboesterling.test.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val ENTITY_NAME = "task"
/**
 * REST controller for managing [org.jakoboesterling.test.domain.Task].
 */
@RestController
@RequestMapping("/api")
class TaskResource(
    private val taskService: TaskService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /tasks` : Create a new task.
     *
     * @param task the task to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new task, or with status `400 (Bad Request)` if the task has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/tasks")
    fun createTask(@RequestBody task: Task): ResponseEntity<Task> {
        log.debug("REST request to save Task : {}", task)
        if (task.id != null) {
            throw BadRequestAlertException(
                "A new task cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = taskService.save(task)
        return ResponseEntity.created(URI("/api/tasks/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /tasks` : Updates an existing task.
     *
     * @param task the task to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated task,
     * or with status `400 (Bad Request)` if the task is not valid,
     * or with status `500 (Internal Server Error)` if the task couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/tasks")
    fun updateTask(@RequestBody task: Task): ResponseEntity<Task> {
        log.debug("REST request to update Task : {}", task)
        if (task.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = taskService.save(task)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     task.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /tasks` : get all the tasks.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of tasks in body.
     */
    @GetMapping("/tasks")
    fun getAllTasks(): MutableList<Task> {
        log.debug("REST request to get all Tasks")
        return taskService.findAll()
    }

    /**
     * `GET  /tasks/:id` : get the "id" task.
     *
     * @param id the id of the task to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the task, or with status `404 (Not Found)`.
     */
    @GetMapping("/tasks/{id}")
    fun getTask(@PathVariable id: Long): ResponseEntity<Task> {
        log.debug("REST request to get Task : {}", id)
        val task = taskService.findOne(id)
        return ResponseUtil.wrapOrNotFound(task)
    }
    /**
     *  `DELETE  /tasks/:id` : delete the "id" task.
     *
     * @param id the id of the task to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/tasks/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Task : {}", id)
        taskService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
