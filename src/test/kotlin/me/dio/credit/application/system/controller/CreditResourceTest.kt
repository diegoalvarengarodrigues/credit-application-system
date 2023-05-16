package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import me.dio.credit.application.system.controller.CreditResourceHelper.creditDto
import me.dio.credit.application.system.controller.CreditResourceHelper.customer
import me.dio.credit.application.system.controller.CreditResourceHelper.invalidCreditDto
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CreditResourceTest {

    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @BeforeEach
    fun setup() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @Test
    fun `when saving a credit to a customer, then return 201 http status`() {
        customerRepository.save(customer)
        val creditAsString = objectMapper.writeValueAsString(creditDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditAsString)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.creditValue").value("1000"))
            .andExpect(jsonPath("$.numberOfInstallment").value("3"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.emailCustomer").value("email@gmail.com"))
            .andExpect(jsonPath("$.incomeCustomer").value("1000.0"))
    }

    @Test
    fun `when date of first installment is over 3 months, then return 400 status`() {
        val creditAsString = objectMapper.writeValueAsString(invalidCreditDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditAsString)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.exception")
                .value("class me.dio.credit.application.system.exception.BusinessException"))
            .andExpect(jsonPath("$.details[*]").isNotEmpty)
    }

    @Test
    fun `when searching for list of credits by customer id, should return 200 status`() {
        // Must save a credit to a customer
        val customer = customerRepository.save(customer)
        val creditAsString = objectMapper.writeValueAsString(creditDto)
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditAsString)
        ).andExpect(status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.get(URL)
                .param("customerId", customer.id.toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].creditCode").exists())
            .andExpect(jsonPath("$[0].creditValue").value(1000.0))
            .andExpect(jsonPath("$[0].numberOfInstallments").value(3))
    }

    @Test
    fun `when searching for a specific credit, then return 200 status`() {
        val customer = customerRepository.save(customer)
        val creditAsString = objectMapper.writeValueAsString(creditDto)
        val creditResult = mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditAsString)
        ).andExpect(status().isCreated).andReturn()
        val responseBody = creditResult.response.contentAsString
        val creditCode = JsonPath.parse(responseBody).read<String>("$.creditCode")

        mockMvc.perform(
            MockMvcRequestBuilders.get("${URL}/${creditCode}")
                .param("customerId", customer.id.toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.creditCode").value(creditCode))
            .andExpect(jsonPath("$.creditValue").value("1000.0"))
            .andExpect(jsonPath("$.numberOfInstallment").value(3))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.emailCustomer").value("email@gmail.com"))
            .andExpect(jsonPath("$.incomeCustomer").value("1000.0"))
    }

    @Test
    fun `when searching for a credit with an invalid code, then return 400 status`() {
        val invalidUuid = "42de0e2d-e158-457b-ad3d-97875b36141b"
        val customer = customerRepository.save(customer)
        val creditAsString = objectMapper.writeValueAsString(creditDto)
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditAsString)
        ).andExpect(status().isCreated)

        mockMvc.perform(
            MockMvcRequestBuilders.get("${URL}/${invalidUuid}")
                .param("customerId", customer.id.toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.exception")
                .value("class me.dio.credit.application.system.exception.BusinessException"))
            .andExpect(jsonPath("$.details.null").value("Creditcode 42de0e2d-e158-457b-ad3d-97875b36141b not found"))
    }

    @Test
    fun `when customer is not the owner of searched credit code, then return 400 status`() {
        val invalidCustomerId = "100"
        customerRepository.save(customer)
        val creditAsString = objectMapper.writeValueAsString(creditDto)
        val creditResult = mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditAsString)
        ).andExpect(status().isCreated).andReturn()
        val responseBody = creditResult.response.contentAsString
        val creditCode = JsonPath.parse(responseBody).read<String>("$.creditCode")

        mockMvc.perform(
            MockMvcRequestBuilders.get("${URL}/${creditCode}")
                .param("customerId", invalidCustomerId)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.exception")
                .value("class java.lang.IllegalArgumentException"))
            .andExpect(jsonPath("$.details.null").value("Contact admin"))
    }

    companion object {
        private const val URL = "/api/credits"
    }
}