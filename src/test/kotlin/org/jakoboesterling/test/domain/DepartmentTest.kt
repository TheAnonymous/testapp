package org.jakoboesterling.test.domain

import org.assertj.core.api.Assertions.assertThat
import org.jakoboesterling.test.web.rest.equalsVerifier
import org.junit.jupiter.api.Test

class DepartmentTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Department::class)
        val department1 = Department()
        department1.id = 1L
        val department2 = Department()
        department2.id = department1.id
        assertThat(department1).isEqualTo(department2)
        department2.id = 2L
        assertThat(department1).isNotEqualTo(department2)
        department1.id = null
        assertThat(department1).isNotEqualTo(department2)
    }
}
