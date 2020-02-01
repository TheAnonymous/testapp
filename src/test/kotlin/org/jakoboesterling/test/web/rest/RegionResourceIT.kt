package org.jakoboesterling.test.web.rest

import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.Region
import org.jakoboesterling.test.repository.RegionRepository
import org.jakoboesterling.test.service.RegionService
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
 * Integration tests for the [RegionResource] REST controller.
 *
 * @see RegionResource
 */
@SpringBootTest(classes = [MyApp::class])
class RegionResourceIT {

    @Autowired
    private lateinit var regionRepository: RegionRepository

    @Autowired
    private lateinit var regionService: RegionService

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

    private lateinit var restRegionMockMvc: MockMvc

    private lateinit var region: Region

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val regionResource = RegionResource(regionService)
        this.restRegionMockMvc = MockMvcBuilders.standaloneSetup(regionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        region = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createRegion() {
        val databaseSizeBeforeCreate = regionRepository.findAll().size

        // Create the Region
        restRegionMockMvc.perform(
            post("/api/regions")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(region))
        ).andExpect(status().isCreated)

        // Validate the Region in the database
        val regionList = regionRepository.findAll()
        assertThat(regionList).hasSize(databaseSizeBeforeCreate + 1)
        val testRegion = regionList[regionList.size - 1]
        assertThat(testRegion.regionName).isEqualTo(DEFAULT_REGION_NAME)
    }

    @Test
    @Transactional
    fun createRegionWithExistingId() {
        val databaseSizeBeforeCreate = regionRepository.findAll().size

        // Create the Region with an existing ID
        region.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restRegionMockMvc.perform(
            post("/api/regions")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(region))
        ).andExpect(status().isBadRequest)

        // Validate the Region in the database
        val regionList = regionRepository.findAll()
        assertThat(regionList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllRegions() {
        // Initialize the database
        regionRepository.saveAndFlush(region)

        // Get all the regionList
        restRegionMockMvc.perform(get("/api/regions?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(region.id?.toInt())))
            .andExpect(jsonPath("$.[*].regionName").value(hasItem(DEFAULT_REGION_NAME)))
    }

    @Test
    @Transactional
    fun getRegion() {
        // Initialize the database
        regionRepository.saveAndFlush(region)

        val id = region.id
        assertNotNull(id)

        // Get the region
        restRegionMockMvc.perform(get("/api/regions/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.regionName").value(DEFAULT_REGION_NAME))
    }

    @Test
    @Transactional
    fun getNonExistingRegion() {
        // Get the region
        restRegionMockMvc.perform(get("/api/regions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateRegion() {
        // Initialize the database
        regionService.save(region)

        val databaseSizeBeforeUpdate = regionRepository.findAll().size

        // Update the region
        val id = region.id
        assertNotNull(id)
        val updatedRegion = regionRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedRegion are not directly saved in db
        em.detach(updatedRegion)
        updatedRegion.regionName = UPDATED_REGION_NAME

        restRegionMockMvc.perform(
            put("/api/regions")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedRegion))
        ).andExpect(status().isOk)

        // Validate the Region in the database
        val regionList = regionRepository.findAll()
        assertThat(regionList).hasSize(databaseSizeBeforeUpdate)
        val testRegion = regionList[regionList.size - 1]
        assertThat(testRegion.regionName).isEqualTo(UPDATED_REGION_NAME)
    }

    @Test
    @Transactional
    fun updateNonExistingRegion() {
        val databaseSizeBeforeUpdate = regionRepository.findAll().size

        // Create the Region

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRegionMockMvc.perform(
            put("/api/regions")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(region))
        ).andExpect(status().isBadRequest)

        // Validate the Region in the database
        val regionList = regionRepository.findAll()
        assertThat(regionList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteRegion() {
        // Initialize the database
        regionService.save(region)

        val databaseSizeBeforeDelete = regionRepository.findAll().size

        val id = region.id
        assertNotNull(id)

        // Delete the region
        restRegionMockMvc.perform(
            delete("/api/regions/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val regionList = regionRepository.findAll()
        assertThat(regionList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_REGION_NAME = "AAAAAAAAAA"
        private const val UPDATED_REGION_NAME = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Region {
            val region = Region(
                regionName = DEFAULT_REGION_NAME
            )

            return region
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Region {
            val region = Region(
                regionName = UPDATED_REGION_NAME
            )

            return region
        }
    }
}
