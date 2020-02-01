package org.jakoboesterling.test.service

import java.util.Optional
import org.jakoboesterling.test.domain.Country

/**
 * Service Interface for managing [Country].
 */
interface CountryService {

    /**
     * Save a country.
     *
     * @param country the entity to save.
     * @return the persisted entity.
     */
    fun save(country: Country): Country

    /**
     * Get all the countries.
     *
     * @return the list of entities.
     */
    fun findAll(): MutableList<Country>

    /**
     * Get the "id" country.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Country>

    /**
     * Delete the "id" country.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
