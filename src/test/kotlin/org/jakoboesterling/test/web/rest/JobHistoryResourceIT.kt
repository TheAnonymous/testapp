package org.jakoboesterling.test.web.rest

import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.JobHistory
import org.jakoboesterling.test.domain.enumeration.Language
import org.jakoboesterling.test.repository.JobHistoryRepository
import org.jakoboesterling.test.service.JobHistoryService
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
 * Integration tests for the [JobHistoryResource] REST controller.
 *
 * @see JobHistoryResource
 */
@SpringBootTest(classes = [MyApp::class])
class JobHistoryResourceIT {

    @Autowired
    private lateinit var jobHistoryRepository: JobHistoryRepository

    @Autowired
    private lateinit var jobHistoryService: JobHistoryService

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

    private lateinit var restJobHistoryMockMvc: MockMvc

    private lateinit var jobHistory: JobHistory

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val jobHistoryResource = JobHistoryResource(jobHistoryService)
        this.restJobHistoryMockMvc = MockMvcBuilders.standaloneSetup(jobHistoryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        jobHistory = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createJobHistory() {
        val databaseSizeBeforeCreate = jobHistoryRepository.findAll().size

        // Create the JobHistory
        restJobHistoryMockMvc.perform(
            post("/api/job-histories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(jobHistory))
        ).andExpect(status().isCreated)

        // Validate the JobHistory in the database
        val jobHistoryList = jobHistoryRepository.findAll()
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeCreate + 1)
        val testJobHistory = jobHistoryList[jobHistoryList.size - 1]
        assertThat(testJobHistory.startDate).isEqualTo(DEFAULT_START_DATE)
        assertThat(testJobHistory.endDate).isEqualTo(DEFAULT_END_DATE)
        assertThat(testJobHistory.language).isEqualTo(DEFAULT_LANGUAGE)
    }

    @Test
    @Transactional
    fun createJobHistoryWithExistingId() {
        val databaseSizeBeforeCreate = jobHistoryRepository.findAll().size

        // Create the JobHistory with an existing ID
        jobHistory.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restJobHistoryMockMvc.perform(
            post("/api/job-histories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(jobHistory))
        ).andExpect(status().isBadRequest)

        // Validate the JobHistory in the database
        val jobHistoryList = jobHistoryRepository.findAll()
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllJobHistories() {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory)

        // Get all the jobHistoryList
        restJobHistoryMockMvc.perform(get("/api/job-histories?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(jobHistory.id?.toInt())))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString())))
    }

    @Test
    @Transactional
    fun getJobHistory() {
        // Initialize the database
        jobHistoryRepository.saveAndFlush(jobHistory)

        val id = jobHistory.id
        assertNotNull(id)

        // Get the jobHistory
        restJobHistoryMockMvc.perform(get("/api/job-histories/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.language").value(DEFAULT_LANGUAGE.toString()))
    }

    @Test
    @Transactional
    fun getNonExistingJobHistory() {
        // Get the jobHistory
        restJobHistoryMockMvc.perform(get("/api/job-histories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateJobHistory() {
        // Initialize the database
        jobHistoryService.save(jobHistory)

        val databaseSizeBeforeUpdate = jobHistoryRepository.findAll().size

        // Update the jobHistory
        val id = jobHistory.id
        assertNotNull(id)
        val updatedJobHistory = jobHistoryRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedJobHistory are not directly saved in db
        em.detach(updatedJobHistory)
        updatedJobHistory.startDate = UPDATED_START_DATE
        updatedJobHistory.endDate = UPDATED_END_DATE
        updatedJobHistory.language = UPDATED_LANGUAGE

        restJobHistoryMockMvc.perform(
            put("/api/job-histories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedJobHistory))
        ).andExpect(status().isOk)

        // Validate the JobHistory in the database
        val jobHistoryList = jobHistoryRepository.findAll()
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeUpdate)
        val testJobHistory = jobHistoryList[jobHistoryList.size - 1]
        assertThat(testJobHistory.startDate).isEqualTo(UPDATED_START_DATE)
        assertThat(testJobHistory.endDate).isEqualTo(UPDATED_END_DATE)
        assertThat(testJobHistory.language).isEqualTo(UPDATED_LANGUAGE)
    }

    @Test
    @Transactional
    fun updateNonExistingJobHistory() {
        val databaseSizeBeforeUpdate = jobHistoryRepository.findAll().size

        // Create the JobHistory

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJobHistoryMockMvc.perform(
            put("/api/job-histories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(jobHistory))
        ).andExpect(status().isBadRequest)

        // Validate the JobHistory in the database
        val jobHistoryList = jobHistoryRepository.findAll()
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteJobHistory() {
        // Initialize the database
        jobHistoryService.save(jobHistory)

        val databaseSizeBeforeDelete = jobHistoryRepository.findAll().size

        val id = jobHistory.id
        assertNotNull(id)

        // Delete the jobHistory
        restJobHistoryMockMvc.perform(
            delete("/api/job-histories/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val jobHistoryList = jobHistoryRepository.findAll()
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private val DEFAULT_START_DATE: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_START_DATE: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        private val DEFAULT_END_DATE: Instant = Instant.ofEpochMilli(0L)
        private val UPDATED_END_DATE: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

        private val DEFAULT_LANGUAGE: Language = Language.FRENCH
        private val UPDATED_LANGUAGE: Language = Language.ENGLISH

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): JobHistory {
            val jobHistory = JobHistory(
                startDate = DEFAULT_START_DATE,
                endDate = DEFAULT_END_DATE,
                language = DEFAULT_LANGUAGE
            )

            return jobHistory
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): JobHistory {
            val jobHistory = JobHistory(
                startDate = UPDATED_START_DATE,
                endDate = UPDATED_END_DATE,
                language = UPDATED_LANGUAGE
            )

            return jobHistory
        }
    }
}
