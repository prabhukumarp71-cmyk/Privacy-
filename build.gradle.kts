// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

// Ensure debug.keystore is restored from debug.keystore.base64 on clean checkouts/CI
val debugKeystore = file("${rootDir}/debug.keystore")
val debugKeystoreBase64 = file("${rootDir}/debug.keystore.base64")
if (!debugKeystore.exists() && debugKeystoreBase64.exists()) {
    try {
        val decoded = java.util.Base64.getMimeDecoder().decode(debugKeystoreBase64.readText().trim())
        debugKeystore.writeBytes(decoded)
    } catch (_: Exception) {}
}

// Ensure .env exists for secrets plugin on clean checkouts/CI
val envFile = file("${rootDir}/.env")
val envExampleFile = file("${rootDir}/.env.example")
if (!envFile.exists() && envExampleFile.exists()) {
    try {
        envExampleFile.copyTo(envFile, overwrite = false)
    } catch (_: Exception) {}
}

