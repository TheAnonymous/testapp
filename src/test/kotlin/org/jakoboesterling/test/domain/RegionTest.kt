package org.jakoboesterling.test.domain

import org.assertj.core.api.Assertions.assertThat
import org.jakoboesterling.test.web.rest.equalsVerifier
import org.junit.jupiter.api.Test

class RegionTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Region::class)
        val region1 = Region()
        region1.id = 1L
        val region2 = Region()
        region2.id = region1.id
        assertThat(region1).isEqualTo(region2)
        region2.id = 2L
        assertThat(region1).isNotEqualTo(region2)
        region1.id = null
        assertThat(region1).isNotEqualTo(region2)
    }
}
