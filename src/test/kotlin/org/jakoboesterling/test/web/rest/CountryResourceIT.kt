package org.jakoboesterling.test.web.rest

import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.Country
import org.jakoboesterling.test.repository.CountryRepository
import org.jakoboesterling.test.service.CountryService
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
 * Integration tests for the [CountryResource] REST controller.
 *
 * @see CountryResource
 */
@SpringBootTest(classes = [MyApp::class])
class CountryResourceIT {

    @Autowired
    private lateinit var countryRepository: CountryRepository

    @Autowired
    private lateinit var countryService: CountryService

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

    private lateinit var restCountryMockMvc: MockMvc

    private lateinit var country: Country

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val countryResource = CountryResource(countryService)
        this.restCountryMockMvc = MockMvcBuilders.standaloneSetup(countryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        country = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCountry() {
        val databaseSizeBeforeCreate = countryRepository.findAll().size

        // Create the Country
        restCountryMockMvc.perform(
            post("/api/countries")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(country))
        ).andExpect(status().isCreated)

        // Validate the Country in the database
        val countryList = countryRepository.findAll()
        assertThat(countryList).hasSize(databaseSizeBeforeCreate + 1)
        val testCountry = countryList[countryList.size - 1]
        assertThat(testCountry.countryName).isEqualTo(DEFAULT_COUNTRY_NAME)
    }

    @Test
    @Transactional
    fun createCountryWithExistingId() {
        val databaseSizeBeforeCreate = countryRepository.findAll().size

        // Create the Country with an existing ID
        country.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restCountryMockMvc.perform(
            post("/api/countries")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(country))
        ).andExpect(status().isBadRequest)

        // Validate the Country in the database
        val countryList = countryRepository.findAll()
        assertThat(countryList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllCountries() {
        // Initialize the database
        countryRepository.saveAndFlush(country)

        // Get all the countryList
        restCountryMockMvc.perform(get("/api/countries?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(country.id?.toInt())))
            .andExpect(jsonPath("$.[*].countryName").value(hasItem(DEFAULT_COUNTRY_NAME)))
    }

    @Test
    @Transactional
    fun getCountry() {
        // Initialize the database
        countryRepository.saveAndFlush(country)

        val id = country.id
        assertNotNull(id)

        // Get the country
        restCountryMockMvc.perform(get("/api/countries/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.countryName").value(DEFAULT_COUNTRY_NAME))
    }

    @Test
    @Transactional
    fun getNonExistingCountry() {
        // Get the country
        restCountryMockMvc.perform(get("/api/countries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateCountry() {
        // Initialize the database
        countryService.save(country)

        val databaseSizeBeforeUpdate = countryRepository.findAll().size

        // Update the country
        val id = country.id
        assertNotNull(id)
        val updatedCountry = countryRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedCountry are not directly saved in db
        em.detach(updatedCountry)
        updatedCountry.countryName = UPDATED_COUNTRY_NAME

        restCountryMockMvc.perform(
            put("/api/countries")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedCountry))
        ).andExpect(status().isOk)

        // Validate the Country in the database
        val countryList = countryRepository.findAll()
        assertThat(countryList).hasSize(databaseSizeBeforeUpdate)
        val testCountry = countryList[countryList.size - 1]
        assertThat(testCountry.countryName).isEqualTo(UPDATED_COUNTRY_NAME)
    }

    @Test
    @Transactional
    fun updateNonExistingCountry() {
        val databaseSizeBeforeUpdate = countryRepository.findAll().size

        // Create the Country

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCountryMockMvc.perform(
            put("/api/countries")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(country))
        ).andExpect(status().isBadRequest)

        // Validate the Country in the database
        val countryList = countryRepository.findAll()
        assertThat(countryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteCountry() {
        // Initialize the database
        countryService.save(country)

        val databaseSizeBeforeDelete = countryRepository.findAll().size

        val id = country.id
        assertNotNull(id)

        // Delete the country
        restCountryMockMvc.perform(
            delete("/api/countries/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val countryList = countryRepository.findAll()
        assertThat(countryList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_COUNTRY_NAME = "AAAAAAAAAA"
        private const val UPDATED_COUNTRY_NAME = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Country {
            val country = Country(
                countryName = DEFAULT_COUNTRY_NAME
            )

            return country
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Country {
            val country = Country(
                countryName = UPDATED_COUNTRY_NAME
            )

            return country
        }
    }
}
