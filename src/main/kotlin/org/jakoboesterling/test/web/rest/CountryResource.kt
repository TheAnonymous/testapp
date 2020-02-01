package org.jakoboesterling.test.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.jakoboesterling.test.domain.Country
import org.jakoboesterling.test.service.CountryService
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

private const val ENTITY_NAME = "country"
/**
 * REST controller for managing [org.jakoboesterling.test.domain.Country].
 */
@RestController
@RequestMapping("/api")
class CountryResource(
    private val countryService: CountryService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /countries` : Create a new country.
     *
     * @param country the country to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new country, or with status `400 (Bad Request)` if the country has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/countries")
    fun createCountry(@RequestBody country: Country): ResponseEntity<Country> {
        log.debug("REST request to save Country : {}", country)
        if (country.id != null) {
            throw BadRequestAlertException(
                "A new country cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = countryService.save(country)
        return ResponseEntity.created(URI("/api/countries/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /countries` : Updates an existing country.
     *
     * @param country the country to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated country,
     * or with status `400 (Bad Request)` if the country is not valid,
     * or with status `500 (Internal Server Error)` if the country couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/countries")
    fun updateCountry(@RequestBody country: Country): ResponseEntity<Country> {
        log.debug("REST request to update Country : {}", country)
        if (country.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = countryService.save(country)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     country.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /countries` : get all the countries.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of countries in body.
     */
    @GetMapping("/countries")
    fun getAllCountries(): MutableList<Country> {
        log.debug("REST request to get all Countries")
        return countryService.findAll()
    }

    /**
     * `GET  /countries/:id` : get the "id" country.
     *
     * @param id the id of the country to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the country, or with status `404 (Not Found)`.
     */
    @GetMapping("/countries/{id}")
    fun getCountry(@PathVariable id: Long): ResponseEntity<Country> {
        log.debug("REST request to get Country : {}", id)
        val country = countryService.findOne(id)
        return ResponseUtil.wrapOrNotFound(country)
    }
    /**
     *  `DELETE  /countries/:id` : delete the "id" country.
     *
     * @param id the id of the country to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/countries/{id}")
    fun deleteCountry(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Country : {}", id)
        countryService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
