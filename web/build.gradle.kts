import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    org.jetbrains.kotlin.multiplatform
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.rootDir.path)
                        add(project.projectDir.path)
                    }
                }
            }
            binaries.executable()
        }

        compilerOptions {
            freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.common)
            }
        }
    }
}
