package org.jakoboesterling.test.service.impl

import java.util.Optional
import org.jakoboesterling.test.domain.Location
import org.jakoboesterling.test.repository.LocationRepository
import org.jakoboesterling.test.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Location].
 */
@Service
@Transactional
class LocationServiceImpl(
    private val locationRepository: LocationRepository
) : LocationService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a location.
     *
     * @param location the entity to save.
     * @return the persisted entity.
     */
    override fun save(location: Location): Location {
        log.debug("Request to save Location : {}", location)
        return locationRepository.save(location)
    }

    /**
     * Get all the locations.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): MutableList<Location> {
        log.debug("Request to get all Locations")
        return locationRepository.findAll()
    }

    /**
     * Get one location by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Location> {
        log.debug("Request to get Location : {}", id)
        return locationRepository.findById(id)
    }

    /**
     * Delete the location by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete Location : {}", id)

        locationRepository.deleteById(id)
    }
}
