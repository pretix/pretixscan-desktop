package tickets.data

import eu.pretix.scan.tickets.data.EmailValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmailValidatorTest {
    companion object {
        val sampleData = listOf(
            // These are from https://github.com/django/django/blob/1b5338d03ecc962af8ab4678426bc60b0672b8dd/tests/validators/tests.py#L279
            arrayOf("email@here.com", true),
            arrayOf("weirder-email@here.and.there.com", true),
            arrayOf("example@valid-----hyphens.com", true),
            arrayOf("example@valid-with-hyphens.com", true),
            arrayOf("test@domain.with.idn.tld.उदाहरण.परीक्षा", true),
            arrayOf(
                "email@localhost",
                false
            ),  // difference to django since we did not implement the whitelist
            arrayOf("\"test@test\"@example.com", true),
            arrayOf("example@atm.${"a".repeat(63)}", true),
            arrayOf("example@${"a".repeat(63)}.atm", true),
            arrayOf("example@${"a".repeat(63)}.${"b".repeat(10)}.atm", true),
            arrayOf("example@atm.${"a".repeat(64)}", false),
            arrayOf("example@${"b".repeat(64)}.atm.${"a".repeat(63)}", false),
            arrayOf("example@${("a".repeat(63) + ".").repeat(100)}com", false),
            arrayOf("", false),
            arrayOf("abc", false),
            arrayOf("abc@", false),
            arrayOf("abc@bar", false),
            arrayOf("a @x.cz", false),
            arrayOf("abc@.com", false),
            arrayOf("something@@somewhere.com", false),
            arrayOf("example@invalid-.com", false),
            arrayOf("example@-invalid.com", false),
            arrayOf("example@invalid.com-", false),
            arrayOf("example@inv-.alid-.com", false),
            arrayOf("example@inv-.-alid.com", false),
            arrayOf("test@example.com\n\n<script src=\"x.js\">", false),
            // Quoted-string format (CR not allowed)
            arrayOf("\"\\\t\"@here.com", true),
            arrayOf("\"\\\r\"@here.com", false),
            arrayOf("trailingdot@shouldfail.com.", false),
            // Max length of domain name labels is 63 characters per RFC 1034.
            arrayOf("a@${"a".repeat(63)}.us", true),
            arrayOf("a@${"a".repeat(64)}.us", false),
            // Trailing newlines in username or domain not allowed
            arrayOf("a@b.com\n", false),
            arrayOf("a\n@b.com", false),
            arrayOf("\"test@test\"\n@example.com", false),

            // We are even stricter than Django and do not allow any IP addresses
            arrayOf("email@[127.0.0.1]", false),
            arrayOf("email@[2001:dB8::1]", false),
            arrayOf("email@[2001:dB8:0:0:0:0:0:1]", false),
            arrayOf("email@[::fffF:127.0.0.1]", false),
            arrayOf("email@127.0.0.1", false),
            arrayOf("email@[127.0.0.256]", false),
            arrayOf("email@[2001:db8::12345]", false),
            arrayOf("email@[2001:db8:0:0:0:0:1]", false),
            arrayOf("email@[::ffff:127.0.0.256]", false),
            arrayOf("email@[2001:dg8::1]", false),
            arrayOf("email@[2001:dG8:0:0:0:0:0:1]", false),
            arrayOf("email@[::fTzF:127.0.0.1]", false),
            arrayOf("a@[127.0.0.1]\n", false),

            // Real-world find
            arrayOf("foobar@example.com.k", false),
        )
    }

    @Test
    fun validatesEmails() {
        val sut = EmailValidator()
        for (inputCondition in sampleData) {
            val value = inputCondition[0] as String
            val valid = inputCondition[1] as Boolean
            assertEquals(valid, sut.isValidEmail(value), "$value is valid email:")
        }
    }
}