package org.jakoboesterling.test.web.rest

import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.Location
import org.jakoboesterling.test.repository.LocationRepository
import org.jakoboesterling.test.service.LocationService
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
 * Integration tests for the [LocationResource] REST controller.
 *
 * @see LocationResource
 */
@SpringBootTest(classes = [MyApp::class])
class LocationResourceIT {

    @Autowired
    private lateinit var locationRepository: LocationRepository

    @Autowired
    private lateinit var locationService: LocationService

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

    private lateinit var restLocationMockMvc: MockMvc

    private lateinit var location: Location

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val locationResource = LocationResource(locationService)
        this.restLocationMockMvc = MockMvcBuilders.standaloneSetup(locationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        location = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createLocation() {
        val databaseSizeBeforeCreate = locationRepository.findAll().size

        // Create the Location
        restLocationMockMvc.perform(
            post("/api/locations")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(location))
        ).andExpect(status().isCreated)

        // Validate the Location in the database
        val locationList = locationRepository.findAll()
        assertThat(locationList).hasSize(databaseSizeBeforeCreate + 1)
        val testLocation = locationList[locationList.size - 1]
        assertThat(testLocation.streetAddress).isEqualTo(DEFAULT_STREET_ADDRESS)
        assertThat(testLocation.postalCode).isEqualTo(DEFAULT_POSTAL_CODE)
        assertThat(testLocation.city).isEqualTo(DEFAULT_CITY)
        assertThat(testLocation.stateProvince).isEqualTo(DEFAULT_STATE_PROVINCE)
    }

    @Test
    @Transactional
    fun createLocationWithExistingId() {
        val databaseSizeBeforeCreate = locationRepository.findAll().size

        // Create the Location with an existing ID
        location.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restLocationMockMvc.perform(
            post("/api/locations")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(location))
        ).andExpect(status().isBadRequest)

        // Validate the Location in the database
        val locationList = locationRepository.findAll()
        assertThat(locationList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllLocations() {
        // Initialize the database
        locationRepository.saveAndFlush(location)

        // Get all the locationList
        restLocationMockMvc.perform(get("/api/locations?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(location.id?.toInt())))
            .andExpect(jsonPath("$.[*].streetAddress").value(hasItem(DEFAULT_STREET_ADDRESS)))
            .andExpect(jsonPath("$.[*].postalCode").value(hasItem(DEFAULT_POSTAL_CODE)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].stateProvince").value(hasItem(DEFAULT_STATE_PROVINCE)))
    }

    @Test
    @Transactional
    fun getLocation() {
        // Initialize the database
        locationRepository.saveAndFlush(location)

        val id = location.id
        assertNotNull(id)

        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.streetAddress").value(DEFAULT_STREET_ADDRESS))
            .andExpect(jsonPath("$.postalCode").value(DEFAULT_POSTAL_CODE))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY))
            .andExpect(jsonPath("$.stateProvince").value(DEFAULT_STATE_PROVINCE))
    }

    @Test
    @Transactional
    fun getNonExistingLocation() {
        // Get the location
        restLocationMockMvc.perform(get("/api/locations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateLocation() {
        // Initialize the database
        locationService.save(location)

        val databaseSizeBeforeUpdate = locationRepository.findAll().size

        // Update the location
        val id = location.id
        assertNotNull(id)
        val updatedLocation = locationRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedLocation are not directly saved in db
        em.detach(updatedLocation)
        updatedLocation.streetAddress = UPDATED_STREET_ADDRESS
        updatedLocation.postalCode = UPDATED_POSTAL_CODE
        updatedLocation.city = UPDATED_CITY
        updatedLocation.stateProvince = UPDATED_STATE_PROVINCE

        restLocationMockMvc.perform(
            put("/api/locations")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedLocation))
        ).andExpect(status().isOk)

        // Validate the Location in the database
        val locationList = locationRepository.findAll()
        assertThat(locationList).hasSize(databaseSizeBeforeUpdate)
        val testLocation = locationList[locationList.size - 1]
        assertThat(testLocation.streetAddress).isEqualTo(UPDATED_STREET_ADDRESS)
        assertThat(testLocation.postalCode).isEqualTo(UPDATED_POSTAL_CODE)
        assertThat(testLocation.city).isEqualTo(UPDATED_CITY)
        assertThat(testLocation.stateProvince).isEqualTo(UPDATED_STATE_PROVINCE)
    }

    @Test
    @Transactional
    fun updateNonExistingLocation() {
        val databaseSizeBeforeUpdate = locationRepository.findAll().size

        // Create the Location

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLocationMockMvc.perform(
            put("/api/locations")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(location))
        ).andExpect(status().isBadRequest)

        // Validate the Location in the database
        val locationList = locationRepository.findAll()
        assertThat(locationList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteLocation() {
        // Initialize the database
        locationService.save(location)

        val databaseSizeBeforeDelete = locationRepository.findAll().size

        val id = location.id
        assertNotNull(id)

        // Delete the location
        restLocationMockMvc.perform(
            delete("/api/locations/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val locationList = locationRepository.findAll()
        assertThat(locationList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_STREET_ADDRESS = "AAAAAAAAAA"
        private const val UPDATED_STREET_ADDRESS = "BBBBBBBBBB"

        private const val DEFAULT_POSTAL_CODE = "AAAAAAAAAA"
        private const val UPDATED_POSTAL_CODE = "BBBBBBBBBB"

        private const val DEFAULT_CITY = "AAAAAAAAAA"
        private const val UPDATED_CITY = "BBBBBBBBBB"

        private const val DEFAULT_STATE_PROVINCE = "AAAAAAAAAA"
        private const val UPDATED_STATE_PROVINCE = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Location {
            val location = Location(
                streetAddress = DEFAULT_STREET_ADDRESS,
                postalCode = DEFAULT_POSTAL_CODE,
                city = DEFAULT_CITY,
                stateProvince = DEFAULT_STATE_PROVINCE
            )

            return location
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Location {
            val location = Location(
                streetAddress = UPDATED_STREET_ADDRESS,
                postalCode = UPDATED_POSTAL_CODE,
                city = UPDATED_CITY,
                stateProvince = UPDATED_STATE_PROVINCE
            )

            return location
        }
    }
}
