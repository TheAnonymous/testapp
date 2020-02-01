package org.jakoboesterling.test.web.rest

import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.Task
import org.jakoboesterling.test.repository.TaskRepository
import org.jakoboesterling.test.service.TaskService
import org.jakoboesterling.test.web.rest.errors.ExceptionTranslator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator

/**
 * Integration tests for the [TaskResource] REST controller.
 *
 * @see TaskResource
 */
@SpringBootTest(classes = [MyApp::class])
class TaskResourceIT {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restTaskMockMvc: MockMvc

    private lateinit var task: Task

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val taskResource = TaskResource(taskService)
        this.restTaskMockMvc = MockMvcBuilders.standaloneSetup(taskResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        task = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createTask() {
        val databaseSizeBeforeCreate = taskRepository.findAll().size

        // Create the Task
        restTaskMockMvc.perform(
            post("/api/tasks")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(task))
        ).andExpect(status().isCreated)

        // Validate the Task in the database
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeCreate + 1)
        val testTask = taskList[taskList.size - 1]
        assertThat(testTask.title).isEqualTo(DEFAULT_TITLE)
        assertThat(testTask.description).isEqualTo(DEFAULT_DESCRIPTION)
    }

    @Test
    @Transactional
    fun createTaskWithExistingId() {
        val databaseSizeBeforeCreate = taskRepository.findAll().size

        // Create the Task with an existing ID
        task.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restTaskMockMvc.perform(
            post("/api/tasks")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(task))
        ).andExpect(status().isBadRequest)

        // Validate the Task in the database
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllTasks() {
        // Initialize the database
        taskRepository.saveAndFlush(task)

        // Get all the taskList
        restTaskMockMvc.perform(get("/api/tasks?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.id?.toInt())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
    }

    @Test
    @Transactional
    fun getTask() {
        // Initialize the database
        taskRepository.saveAndFlush(task)

        val id = task.id
        assertNotNull(id)

        // Get the task
        restTaskMockMvc.perform(get("/api/tasks/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
    }

    @Test
    @Transactional
    fun getNonExistingTask() {
        // Get the task
        restTaskMockMvc.perform(get("/api/tasks/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateTask() {
        // Initialize the database
        taskService.save(task)

        val databaseSizeBeforeUpdate = taskRepository.findAll().size

        // Update the task
        val id = task.id
        assertNotNull(id)
        val updatedTask = taskRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedTask are not directly saved in db
        em.detach(updatedTask)
        updatedTask.title = UPDATED_TITLE
        updatedTask.description = UPDATED_DESCRIPTION

        restTaskMockMvc.perform(
            put("/api/tasks")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedTask))
        ).andExpect(status().isOk)

        // Validate the Task in the database
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate)
        val testTask = taskList[taskList.size - 1]
        assertThat(testTask.title).isEqualTo(UPDATED_TITLE)
        assertThat(testTask.description).isEqualTo(UPDATED_DESCRIPTION)
    }

    @Test
    @Transactional
    fun updateNonExistingTask() {
        val databaseSizeBeforeUpdate = taskRepository.findAll().size

        // Create the Task

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskMockMvc.perform(
            put("/api/tasks")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(task))
        ).andExpect(status().isBadRequest)

        // Validate the Task in the database
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteTask() {
        // Initialize the database
        taskService.save(task)

        val databaseSizeBeforeDelete = taskRepository.findAll().size

        val id = task.id
        assertNotNull(id)

        // Delete the task
        restTaskMockMvc.perform(
            delete("/api/tasks/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_TITLE = "AAAAAAAAAA"
        private const val UPDATED_TITLE = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Task {
            val task = Task(
                title = DEFAULT_TITLE,
                description = DEFAULT_DESCRIPTION
            )

            return task
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Task {
            val task = Task(
                title = UPDATED_TITLE,
                description = UPDATED_DESCRIPTION
            )

            return task
        }
    }
}
