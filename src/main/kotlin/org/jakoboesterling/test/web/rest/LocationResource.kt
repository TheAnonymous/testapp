package org.jakoboesterling.test.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.jakoboesterling.test.domain.Location
import org.jakoboesterling.test.service.LocationService
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

private const val ENTITY_NAME = "location"
/**
 * REST controller for managing [org.jakoboesterling.test.domain.Location].
 */
@RestController
@RequestMapping("/api")
class LocationResource(
    private val locationService: LocationService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /locations` : Create a new location.
     *
     * @param location the location to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new location, or with status `400 (Bad Request)` if the location has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/locations")
    fun createLocation(@RequestBody location: Location): ResponseEntity<Location> {
        log.debug("REST request to save Location : {}", location)
        if (location.id != null) {
            throw BadRequestAlertException(
                "A new location cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = locationService.save(location)
        return ResponseEntity.created(URI("/api/locations/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /locations` : Updates an existing location.
     *
     * @param location the location to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated location,
     * or with status `400 (Bad Request)` if the location is not valid,
     * or with status `500 (Internal Server Error)` if the location couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/locations")
    fun updateLocation(@RequestBody location: Location): ResponseEntity<Location> {
        log.debug("REST request to update Location : {}", location)
        if (location.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = locationService.save(location)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     location.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /locations` : get all the locations.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of locations in body.
     */
    @GetMapping("/locations")
    fun getAllLocations(): MutableList<Location> {
        log.debug("REST request to get all Locations")
        return locationService.findAll()
    }

    /**
     * `GET  /locations/:id` : get the "id" location.
     *
     * @param id the id of the location to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the location, or with status `404 (Not Found)`.
     */
    @GetMapping("/locations/{id}")
    fun getLocation(@PathVariable id: Long): ResponseEntity<Location> {
        log.debug("REST request to get Location : {}", id)
        val location = locationService.findOne(id)
        return ResponseUtil.wrapOrNotFound(location)
    }
    /**
     *  `DELETE  /locations/:id` : delete the "id" location.
     *
     * @param id the id of the location to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/locations/{id}")
    fun deleteLocation(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Location : {}", id)
        locationService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
