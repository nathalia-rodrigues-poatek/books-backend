package com.books.users

import com.books.users.repositories.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository
) {

    @BeforeEach
    fun cleanDatabase() {
        userRepository.deleteAll()
    }

    /** Creates a user via the API and returns its generated id. */
    private fun createUser(
        name: String = "Nathalia",
        email: String = "nathalia@poatek.com",
        token: String = "tok-123",
        password: String = "senha123"
    ): Long {
        val body = mapOf("name" to name, "email" to email, "token" to token, "password" to password)
        val result = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isCreated).andReturn()
        return idFrom(result)
    }

    private fun idFrom(result: MvcResult): Long {
        val node = objectMapper.readTree(result.response.contentAsString)
        return node.get("id").asLong()
    }

    private fun login(email: String, password: String) =
        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mapOf("email" to email, "password" to password)))
        )

    // ---------- create ----------

    @Test
    fun `creates a user and never exposes the password or token in the response`() {
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Nathalia","email":"nathalia@poatek.com","token":"tok-1","password":"senha123"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("Nathalia"))
            .andExpect(jsonPath("$.email").value("nathalia@poatek.com"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.token").doesNotExist())
    }

    @Test
    fun `rejects invalid create payload with 400 and field errors`() {
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","email":"not-an-email","token":"t","password":"123"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists())
    }

    @Test
    fun `rejects duplicate email or token with 409`() {
        createUser(email = "dup@poatek.com", token = "tok-dup")
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Other","email":"dup@poatek.com","token":"tok-dup","password":"senha123"}""")
        ).andExpect(status().isConflict)
    }

    // ---------- read ----------

    @Test
    fun `lists only non-deleted users`() {
        val keptId = createUser(email = "keep@poatek.com", token = "tok-keep")
        val deletedId = createUser(email = "gone@poatek.com", token = "tok-gone")
        mockMvc.perform(delete("/api/users/$deletedId")).andExpect(status().isNoContent)

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(keptId))
    }

    @Test
    fun `returns 404 when getting an unknown user`() {
        mockMvc.perform(get("/api/users/999999")).andExpect(status().isNotFound)
    }

    // ---------- update ----------

    @Test
    fun `updates name and email`() {
        val id = createUser()
        mockMvc.perform(
            put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Nathalia Santos","email":"nathalia.santos@poatek.com"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Nathalia Santos"))
            .andExpect(jsonPath("$.email").value("nathalia.santos@poatek.com"))
    }

    @Test
    fun `updating the password lets the user login with the new one`() {
        val id = createUser(password = "senha123")
        mockMvc.perform(
            put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Nathalia","email":"nathalia@poatek.com","password":"novasenha123"}""")
        ).andExpect(status().isOk)

        login("nathalia@poatek.com", "novasenha123").andExpect(status().isOk)
        login("nathalia@poatek.com", "senha123").andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns 404 when updating an unknown user`() {
        mockMvc.perform(
            put("/api/users/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"X","email":"x@poatek.com"}""")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `rejects invalid update payload with 400 and field errors`() {
        val id = createUser()
        mockMvc.perform(
            put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","email":"not-an-email","password":"123"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists())
    }

    // ---------- delete / restore ----------

    @Test
    fun `soft deletes then restores a user`() {
        val id = createUser()

        mockMvc.perform(delete("/api/users/$id")).andExpect(status().isNoContent)
        mockMvc.perform(get("/api/users/$id")).andExpect(status().isNotFound)

        mockMvc.perform(post("/api/users/$id/restore"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.deleted").value(false))
            .andExpect(jsonPath("$.deletedDate").doesNotExist())

        mockMvc.perform(get("/api/users/$id")).andExpect(status().isOk)
    }

    @Test
    fun `deleted user cannot login or be updated`() {
        val id = createUser()
        mockMvc.perform(delete("/api/users/$id")).andExpect(status().isNoContent)

        login("nathalia@poatek.com", "senha123").andExpect(status().isUnauthorized)
        mockMvc.perform(
            put("/api/users/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"X","email":"x@poatek.com"}""")
        ).andExpect(status().isNotFound)
    }

    // ---------- block / unblock ----------

    @Test
    fun `blocks a user, forbids login, then unblocks and allows login again`() {
        val id = createUser()

        login("nathalia@poatek.com", "senha123").andExpect(status().isOk)

        mockMvc.perform(post("/api/users/$id/block"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.blocked").value(true))
            .andExpect(jsonPath("$.blockedDate").exists())

        login("nathalia@poatek.com", "senha123").andExpect(status().isForbidden)

        mockMvc.perform(post("/api/users/$id/unblock"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.blocked").value(false))
            .andExpect(jsonPath("$.blockedDate").doesNotExist())

        login("nathalia@poatek.com", "senha123").andExpect(status().isOk)
    }

    @Test
    fun `returns 404 when restoring, blocking or unblocking an unknown user`() {
        mockMvc.perform(post("/api/users/999999/restore")).andExpect(status().isNotFound)
        mockMvc.perform(post("/api/users/999999/block")).andExpect(status().isNotFound)
        mockMvc.perform(post("/api/users/999999/unblock")).andExpect(status().isNotFound)
    }

    // ---------- login ----------

    @Test
    fun `login succeeds with correct credentials and returns the token`() {
        createUser(token = "tok-login")
        login("nathalia@poatek.com", "senha123")
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value("tok-login"))
    }

    @Test
    fun `login fails with wrong password`() {
        createUser()
        login("nathalia@poatek.com", "errada").andExpect(status().isUnauthorized)
    }

    @Test
    fun `login fails for unknown email`() {
        login("naoexiste@poatek.com", "qualquer").andExpect(status().isUnauthorized)
    }
}
