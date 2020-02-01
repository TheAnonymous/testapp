package org.jakoboesterling.test.web.rest

import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.Department
import org.jakoboesterling.test.repository.DepartmentRepository
import org.jakoboesterling.test.service.DepartmentService
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
 * Integration tests for the [DepartmentResource] REST controller.
 *
 * @see DepartmentResource
 */
@SpringBootTest(classes = [MyApp::class])
class DepartmentResourceIT {

    @Autowired
    private lateinit var departmentRepository: DepartmentRepository

    @Autowired
    private lateinit var departmentService: DepartmentService

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

    private lateinit var restDepartmentMockMvc: MockMvc

    private lateinit var department: Department

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val departmentResource = DepartmentResource(departmentService)
        this.restDepartmentMockMvc = MockMvcBuilders.standaloneSetup(departmentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        department = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createDepartment() {
        val databaseSizeBeforeCreate = departmentRepository.findAll().size

        // Create the Department
        restDepartmentMockMvc.perform(
            post("/api/departments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(department))
        ).andExpect(status().isCreated)

        // Validate the Department in the database
        val departmentList = departmentRepository.findAll()
        assertThat(departmentList).hasSize(databaseSizeBeforeCreate + 1)
        val testDepartment = departmentList[departmentList.size - 1]
        assertThat(testDepartment.departmentName).isEqualTo(DEFAULT_DEPARTMENT_NAME)
    }

    @Test
    @Transactional
    fun createDepartmentWithExistingId() {
        val databaseSizeBeforeCreate = departmentRepository.findAll().size

        // Create the Department with an existing ID
        department.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restDepartmentMockMvc.perform(
            post("/api/departments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(department))
        ).andExpect(status().isBadRequest)

        // Validate the Department in the database
        val departmentList = departmentRepository.findAll()
        assertThat(departmentList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun checkDepartmentNameIsRequired() {
        val databaseSizeBeforeTest = departmentRepository.findAll().size
        // set the field null
        department.departmentName = null

        // Create the Department, which fails.

        restDepartmentMockMvc.perform(
            post("/api/departments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(department))
        ).andExpect(status().isBadRequest)

        val departmentList = departmentRepository.findAll()
        assertThat(departmentList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    fun getAllDepartments() {
        // Initialize the database
        departmentRepository.saveAndFlush(department)

        // Get all the departmentList
        restDepartmentMockMvc.perform(get("/api/departments?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(department.id?.toInt())))
            .andExpect(jsonPath("$.[*].departmentName").value(hasItem(DEFAULT_DEPARTMENT_NAME)))
    }

    @Test
    @Transactional
    fun getDepartment() {
        // Initialize the database
        departmentRepository.saveAndFlush(department)

        val id = department.id
        assertNotNull(id)

        // Get the department
        restDepartmentMockMvc.perform(get("/api/departments/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.departmentName").value(DEFAULT_DEPARTMENT_NAME))
    }

    @Test
    @Transactional
    fun getNonExistingDepartment() {
        // Get the department
        restDepartmentMockMvc.perform(get("/api/departments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateDepartment() {
        // Initialize the database
        departmentService.save(department)

        val databaseSizeBeforeUpdate = departmentRepository.findAll().size

        // Update the department
        val id = department.id
        assertNotNull(id)
        val updatedDepartment = departmentRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedDepartment are not directly saved in db
        em.detach(updatedDepartment)
        updatedDepartment.departmentName = UPDATED_DEPARTMENT_NAME

        restDepartmentMockMvc.perform(
            put("/api/departments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedDepartment))
        ).andExpect(status().isOk)

        // Validate the Department in the database
        val departmentList = departmentRepository.findAll()
        assertThat(departmentList).hasSize(databaseSizeBeforeUpdate)
        val testDepartment = departmentList[departmentList.size - 1]
        assertThat(testDepartment.departmentName).isEqualTo(UPDATED_DEPARTMENT_NAME)
    }

    @Test
    @Transactional
    fun updateNonExistingDepartment() {
        val databaseSizeBeforeUpdate = departmentRepository.findAll().size

        // Create the Department

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDepartmentMockMvc.perform(
            put("/api/departments")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(department))
        ).andExpect(status().isBadRequest)

        // Validate the Department in the database
        val departmentList = departmentRepository.findAll()
        assertThat(departmentList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteDepartment() {
        // Initialize the database
        departmentService.save(department)

        val databaseSizeBeforeDelete = departmentRepository.findAll().size

        val id = department.id
        assertNotNull(id)

        // Delete the department
        restDepartmentMockMvc.perform(
            delete("/api/departments/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val departmentList = departmentRepository.findAll()
        assertThat(departmentList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_DEPARTMENT_NAME = "AAAAAAAAAA"
        private const val UPDATED_DEPARTMENT_NAME = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Department {
            val department = Department(
                departmentName = DEFAULT_DEPARTMENT_NAME
            )

            return department
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Department {
            val department = Department(
                departmentName = UPDATED_DEPARTMENT_NAME
            )

            return department
        }
    }
}
