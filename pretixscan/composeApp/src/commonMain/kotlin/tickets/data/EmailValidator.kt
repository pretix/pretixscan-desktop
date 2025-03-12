package tickets.data

import java.net.IDN
import java.util.regex.Pattern

class EmailValidator {
    // Same validation as django/core/validators.py
    val DOT_ATOM = "^[-!#\\$%&'*+/=?^_`{}|~0-9A-Z]+(\\.[-!#\\$%&'*+/=?^_`{}|~0-9A-Z]+)*\\Z"
    val QUOTED_STRING = "^\"([\\001-\\010\\013\\014\\016-\\037!#-\\[\\]-\\0177]|\\\\[\\001-\\011\\013\\014\\016-\\0177])*\"\\Z"
    val USER_REGEX = Pattern.compile("($DOT_ATOM|$QUOTED_STRING)", Pattern.CASE_INSENSITIVE)
    val DOMAIN_REGEX = Pattern.compile(
        // max length for domain name labels is 63 characters per RFC 1034
        "((?:[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?\\.)+)(?:[A-Z0-9-]{2,63}(?<!-))\\Z",
        Pattern.CASE_INSENSITIVE
    )

    fun isValidEmail(value: String): Boolean {
        // The maximum length of an email is 320 characters per RFC 3696
        // section 3.
        if (value.isBlank() || !value.contains("@") || value.length > 320) {
            return false
        }

        val userPart = value.substringBeforeLast("@")
        val domainPart = value.substringAfterLast("@")

        if (!USER_REGEX.matcher(userPart).matches()) {
            return false
        }

        if (!validateDomainPart(domainPart)) {
            try {
                val asciiDomainPart = IDN.toASCII(domainPart)
                if (validateDomainPart(asciiDomainPart)) {
                    return true
                }
            } catch (e: IllegalArgumentException) {
                // ignore
            }
            return false
        }
        return true
    }

    fun validateDomainPart(domainPart: String): Boolean {
        return DOMAIN_REGEX.matcher(domainPart).matches()
        // Django also checks for literal form here, such as @[127.0.0.1] here, but we are stricter
        // here and do not accept that
    }
}