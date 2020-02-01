package org.jakoboesterling.test.web.rest

import org.hamcrest.Matchers.isEmptyString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.jakoboesterling.test.MyApp
import org.jakoboesterling.test.domain.User
import org.jakoboesterling.test.repository.UserRepository
import org.jakoboesterling.test.security.jwt.TokenProvider
import org.jakoboesterling.test.web.rest.errors.ExceptionTranslator
import org.jakoboesterling.test.web.rest.vm.LoginVM
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

/**
 * Integration tests for the [UserJWTController] REST controller.
 */
@SpringBootTest(classes = [MyApp::class])
class UserJWTControllerIT {

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    @Autowired
    private lateinit var authenticationManager: AuthenticationManagerBuilder

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        val userJWTController = UserJWTController(tokenProvider, authenticationManager)
        this.mockMvc = MockMvcBuilders.standaloneSetup(userJWTController)
            .setControllerAdvice(exceptionTranslator)
            .build()
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorize() {
        val user = User(
            login = "user-jwt-controller",
            email = "user-jwt-controller@example.com",
            activated = true,
            password = passwordEncoder.encode("test")
        )

        userRepository.saveAndFlush(user)

        val login = LoginVM(username = "user-jwt-controller", password = "test")
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(login))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.id_token").isString)
            .andExpect(jsonPath("\$.id_token").isNotEmpty)
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(isEmptyString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun testAuthorizeWithRememberMe() {
        val user = User(
            login = "user-jwt-controller-remember-me",
            email = "user-jwt-controller-remember-me@example.com",
            activated = true,
            password = passwordEncoder.encode("test")
        )

        userRepository.saveAndFlush(user)

        val login = LoginVM(
            username = "user-jwt-controller-remember-me",
            password = "test",
            isRememberMe = true
        )
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(login))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.id_token").isString)
            .andExpect(jsonPath("\$.id_token").isNotEmpty)
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(isEmptyString())))
    }

    @Test
    @Throws(Exception::class)
    fun testAuthorizeFails() {
        val login = LoginVM(username = "wrong-user", password = "wrong password")
        mockMvc.perform(
            post("/api/authenticate")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(login))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("\$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"))
    }
}
