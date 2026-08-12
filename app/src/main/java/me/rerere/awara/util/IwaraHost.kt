package me.rerere.awara.util

/**
 * Returns true only for the iwara.tv apex domain and its real subdomains.
 * A plain substring check would also trust attacker-controlled hosts such as
 * `iwara.tv.example.com`.
 */
internal fun isTrustedIwaraHost(host: String): Boolean {
    val normalizedHost = host.trimEnd('.').lowercase()
    return normalizedHost == "iwara.tv" || normalizedHost.endsWith(".iwara.tv")
}
