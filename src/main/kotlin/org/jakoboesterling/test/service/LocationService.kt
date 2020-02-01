package org.jakoboesterling.test.service

import java.util.Optional
import org.jakoboesterling.test.domain.Location

/**
 * Service Interface for managing [Location].
 */
interface LocationService {

    /**
     * Save a location.
     *
     * @param location the entity to save.
     * @return the persisted entity.
     */
    fun save(location: Location): Location

    /**
     * Get all the locations.
     *
     * @return the list of entities.
     */
    fun findAll(): MutableList<Location>

    /**
     * Get the "id" location.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Location>

    /**
     * Delete the "id" location.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
