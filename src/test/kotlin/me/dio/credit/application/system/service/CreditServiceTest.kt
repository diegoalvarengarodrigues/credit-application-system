package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.util.*
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.CreditServiceHelper.customerModel
import me.dio.credit.application.system.service.CreditServiceHelper.invalidDateCredit
import me.dio.credit.application.system.service.CreditServiceHelper.validCredit
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CreditServiceTest {

    @InjectMockKs
    lateinit var creditService: CreditService

    @RelaxedMockK
    lateinit var creditRepository: CreditRepository

    @RelaxedMockK
    lateinit var customerService: CustomerService

    @BeforeEach
    fun setUp() {
        creditService = CreditService(creditRepository, customerService)
    }

    @Test
    fun `when saving a credit, should bind it to a specific customer`() {
        val id = Random().nextLong()
        every { customerService.findById(id) } returns customerModel
        every { creditRepository.save(any()) } returns validCredit

        val actual: Credit = creditService.save(validCredit)

        assertThat(actual).isSameAs(validCredit)
        verify(exactly = 1) { creditRepository.save(any()) }
    }

    @Test
    fun `when saving a credit with an invalid date, should throw a business exception`() {
        val id = Random().nextLong()
        every { customerService.findById(id) } returns customerModel
        every { creditRepository.save(any()) } returns invalidDateCredit

        assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.save(invalidDateCredit) }
            .withMessage("Invalid Date")
    }

    @Test
    fun `when searching for credits by customer, should return a list of credits`() {
        val id = Random().nextLong()
        every { creditRepository.findAllByCustomerId(id) } returns listOf(validCredit)

        val actual = creditService.findAllByCustomer(id)

        assertThat(actual[0]).isSameAs(validCredit)
    }

    @Test
    fun `when searching for credit, should return credit if customer is credit owner`() {
        val customerId = Random().nextLong()
        val customerCreditCode = UUID.randomUUID()
        val credit = validCredit.apply {
            every { creditCode } returns customerCreditCode
            every { customer!!.id } returns customerId
        }
        every { creditRepository.findByCreditCode(customerCreditCode) } returns credit

        val actual = creditService.findByCreditCode(customerId, customerCreditCode)

        assertThat(actual).isExactlyInstanceOf(Credit::class.java)
        assertThat(actual).isSameAs(validCredit)
    }

    @Test
    fun `when searching for credit, should throw business exception if creditCode doesn't exist`() {
        val id = Random().nextLong()
        val creditCode = UUID.randomUUID()
        every { creditRepository.findByCreditCode(creditCode) } returns null

        assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy { creditService.findByCreditCode(id, creditCode) }
            .withMessage("Creditcode $creditCode not found")
    }

    @Test
    fun `when searching for credit, throw IllegalArgumentException if customer is not the credit owner`() {
        val id = Random().nextLong()
        val creditCode = UUID.randomUUID()
        every { creditRepository.findByCreditCode(creditCode) } returns validCredit

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { creditService.findByCreditCode(id, creditCode) }
            .withMessage("Contact admin")
    }
}