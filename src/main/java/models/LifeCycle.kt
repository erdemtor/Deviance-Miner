package models

/**
 * Created by Erdem on 11-Nov-17.
 */
enum class LifeCycle {
    START, COMPLETE, UNKNOWN;

    companion object {
        fun fromString(x: String): LifeCycle = when {
            x.isNullOrBlank() -> UNKNOWN
            x.trim().equals("start", ignoreCase = true) -> START
            x.trim().equals("complete", ignoreCase = true) -> COMPLETE
            else -> UNKNOWN

        }
    }
}
