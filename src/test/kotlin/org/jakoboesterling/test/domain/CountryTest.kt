package org.jakoboesterling.test.domain

import org.assertj.core.api.Assertions.assertThat
import org.jakoboesterling.test.web.rest.equalsVerifier
import org.junit.jupiter.api.Test

class CountryTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Country::class)
        val country1 = Country()
        country1.id = 1L
        val country2 = Country()
        country2.id = country1.id
        assertThat(country1).isEqualTo(country2)
        country2.id = 2L
        assertThat(country1).isNotEqualTo(country2)
        country1.id = null
        assertThat(country1).isNotEqualTo(country2)
    }
}
