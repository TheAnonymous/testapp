package org.jakoboesterling.test.repository

import java.util.Optional
import org.jakoboesterling.test.domain.Job
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Job] entity.
 */
@Repository
interface JobRepository : JpaRepository<Job, Long> {

    @Query(
        value = "select distinct job from Job job left join fetch job.tasks",
        countQuery = "select count(distinct job) from Job job"
    )
    fun findAllWithEagerRelationships(pageable: Pageable): Page<Job>

    @Query(value = "select distinct job from Job job left join fetch job.tasks")
    fun findAllWithEagerRelationships(): MutableList<Job>

    @Query("select job from Job job left join fetch job.tasks where job.id =:id")
    fun findOneWithEagerRelationships(@Param("id") id: Long): Optional<Job>
}
