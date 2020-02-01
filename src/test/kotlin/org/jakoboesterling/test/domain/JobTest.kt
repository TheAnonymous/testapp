package org.jakoboesterling.test.domain

import org.assertj.core.api.Assertions.assertThat
import org.jakoboesterling.test.web.rest.equalsVerifier
import org.junit.jupiter.api.Test

class JobTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Job::class)
        val job1 = Job()
        job1.id = 1L
        val job2 = Job()
        job2.id = job1.id
        assertThat(job1).isEqualTo(job2)
        job2.id = 2L
        assertThat(job1).isNotEqualTo(job2)
        job1.id = null
        assertThat(job1).isNotEqualTo(job2)
    }
}
