package org.jakoboesterling.test.domain

import org.assertj.core.api.Assertions.assertThat
import org.jakoboesterling.test.web.rest.equalsVerifier
import org.junit.jupiter.api.Test

class JobHistoryTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(JobHistory::class)
        val jobHistory1 = JobHistory()
        jobHistory1.id = 1L
        val jobHistory2 = JobHistory()
        jobHistory2.id = jobHistory1.id
        assertThat(jobHistory1).isEqualTo(jobHistory2)
        jobHistory2.id = 2L
        assertThat(jobHistory1).isNotEqualTo(jobHistory2)
        jobHistory1.id = null
        assertThat(jobHistory1).isNotEqualTo(jobHistory2)
    }
}
