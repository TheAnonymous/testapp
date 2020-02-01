package org.jakoboesterling.test.web.rest

import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.Job
import org.jakoboesterling.test.repository.JobRepository
import org.jakoboesterling.test.web.rest.errors.ExceptionTranslator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
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
 * Integration tests for the [JobResource] REST controller.
 *
 * @see JobResource
 */
@SpringBootTest(classes = [MyApp::class])
class JobResourceIT {

    @Autowired
    private lateinit var jobRepository: JobRepository

    @Mock
    private lateinit var jobRepositoryMock: JobRepository

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

    private lateinit var restJobMockMvc: MockMvc

    private lateinit var job: Job

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val jobResource = JobResource(jobRepository)
        this.restJobMockMvc = MockMvcBuilders.standaloneSetup(jobResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        job = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createJob() {
        val databaseSizeBeforeCreate = jobRepository.findAll().size

        // Create the Job
        restJobMockMvc.perform(
            post("/api/jobs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(job))
        ).andExpect(status().isCreated)

        // Validate the Job in the database
        val jobList = jobRepository.findAll()
        assertThat(jobList).hasSize(databaseSizeBeforeCreate + 1)
        val testJob = jobList[jobList.size - 1]
        assertThat(testJob.jobTitle).isEqualTo(DEFAULT_JOB_TITLE)
        assertThat(testJob.minSalary).isEqualTo(DEFAULT_MIN_SALARY)
        assertThat(testJob.maxSalary).isEqualTo(DEFAULT_MAX_SALARY)
    }

    @Test
    @Transactional
    fun createJobWithExistingId() {
        val databaseSizeBeforeCreate = jobRepository.findAll().size

        // Create the Job with an existing ID
        job.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restJobMockMvc.perform(
            post("/api/jobs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(job))
        ).andExpect(status().isBadRequest)

        // Validate the Job in the database
        val jobList = jobRepository.findAll()
        assertThat(jobList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllJobs() {
        // Initialize the database
        jobRepository.saveAndFlush(job)

        // Get all the jobList
        restJobMockMvc.perform(get("/api/jobs?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(job.id?.toInt())))
            .andExpect(jsonPath("$.[*].jobTitle").value(hasItem(DEFAULT_JOB_TITLE)))
            .andExpect(jsonPath("$.[*].minSalary").value(hasItem(DEFAULT_MIN_SALARY.toInt())))
            .andExpect(jsonPath("$.[*].maxSalary").value(hasItem(DEFAULT_MAX_SALARY.toInt())))
    }

    @Suppress("unchecked")
    fun getAllJobsWithEagerRelationshipsIsEnabled() {
        val jobResource = JobResource(jobRepositoryMock)
        `when`(jobRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        val restJobMockMvc = MockMvcBuilders.standaloneSetup(jobResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build()

        restJobMockMvc.perform(get("/api/jobs?eagerload=true"))
            .andExpect(status().isOk)

        verify(jobRepositoryMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Suppress("unchecked")
    fun getAllJobsWithEagerRelationshipsIsNotEnabled() {
        val jobResource = JobResource(jobRepositoryMock)
        `when`(jobRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))
        val restJobMockMvc = MockMvcBuilders.standaloneSetup(jobResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build()

        restJobMockMvc.perform(get("/api/jobs?eagerload=true"))
            .andExpect(status().isOk)

        verify(jobRepositoryMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Test
    @Transactional
    fun getJob() {
        // Initialize the database
        jobRepository.saveAndFlush(job)

        val id = job.id
        assertNotNull(id)

        // Get the job
        restJobMockMvc.perform(get("/api/jobs/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.jobTitle").value(DEFAULT_JOB_TITLE))
            .andExpect(jsonPath("$.minSalary").value(DEFAULT_MIN_SALARY.toInt()))
            .andExpect(jsonPath("$.maxSalary").value(DEFAULT_MAX_SALARY.toInt()))
    }

    @Test
    @Transactional
    fun getNonExistingJob() {
        // Get the job
        restJobMockMvc.perform(get("/api/jobs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateJob() {
        // Initialize the database
        jobRepository.saveAndFlush(job)

        val databaseSizeBeforeUpdate = jobRepository.findAll().size

        // Update the job
        val id = job.id
        assertNotNull(id)
        val updatedJob = jobRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedJob are not directly saved in db
        em.detach(updatedJob)
        updatedJob.jobTitle = UPDATED_JOB_TITLE
        updatedJob.minSalary = UPDATED_MIN_SALARY
        updatedJob.maxSalary = UPDATED_MAX_SALARY

        restJobMockMvc.perform(
            put("/api/jobs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedJob))
        ).andExpect(status().isOk)

        // Validate the Job in the database
        val jobList = jobRepository.findAll()
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate)
        val testJob = jobList[jobList.size - 1]
        assertThat(testJob.jobTitle).isEqualTo(UPDATED_JOB_TITLE)
        assertThat(testJob.minSalary).isEqualTo(UPDATED_MIN_SALARY)
        assertThat(testJob.maxSalary).isEqualTo(UPDATED_MAX_SALARY)
    }

    @Test
    @Transactional
    fun updateNonExistingJob() {
        val databaseSizeBeforeUpdate = jobRepository.findAll().size

        // Create the Job

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJobMockMvc.perform(
            put("/api/jobs")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(job))
        ).andExpect(status().isBadRequest)

        // Validate the Job in the database
        val jobList = jobRepository.findAll()
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteJob() {
        // Initialize the database
        jobRepository.saveAndFlush(job)

        val databaseSizeBeforeDelete = jobRepository.findAll().size

        val id = job.id
        assertNotNull(id)

        // Delete the job
        restJobMockMvc.perform(
            delete("/api/jobs/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val jobList = jobRepository.findAll()
        assertThat(jobList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_JOB_TITLE = "AAAAAAAAAA"
        private const val UPDATED_JOB_TITLE = "BBBBBBBBBB"

        private const val DEFAULT_MIN_SALARY: Long = 1L
        private const val UPDATED_MIN_SALARY: Long = 2L

        private const val DEFAULT_MAX_SALARY: Long = 1L
        private const val UPDATED_MAX_SALARY: Long = 2L

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Job {
            val job = Job(
                jobTitle = DEFAULT_JOB_TITLE,
                minSalary = DEFAULT_MIN_SALARY,
                maxSalary = DEFAULT_MAX_SALARY
            )

            return job
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Job {
            val job = Job(
                jobTitle = UPDATED_JOB_TITLE,
                minSalary = UPDATED_MIN_SALARY,
                maxSalary = UPDATED_MAX_SALARY
            )

            return job
        }
    }
}
