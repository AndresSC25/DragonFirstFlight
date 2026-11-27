# Manual de Instalación - Dragon's First Flight

Guía técnica para clonar, configurar, compilar, probar y desplegar **Dragon's First Flight** en Android y Desktop JVM.

**Equipo:** Red Crow  
**Curso:** BISOFT-38 · Proyecto de Ingeniería del Software 4 · SCV0  
**Validación final:** 2026-11-27  
**Rama y commit validados:** `main` · `42bc659`

## 1. Propósito y alcance

Este manual describe cómo obtener el código, preparar el entorno, compilar Android y Desktop JVM, ejecutar pruebas y generar artefactos distribuibles. El proyecto utiliza Kotlin Multiplatform y KorGE.

| Plataforma | Estado | Distribución |
|---|---|---|
| Android | Compilado, instalado y validado | APK firmado en GitHub Releases |
| Desktop JVM | Compilado y ejecutado | Distribución nativa según el sistema operativo |
| iOS | Fuera del alcance | El módulo `iosApp` proviene de la plantilla y no ejecuta el juego |

## 2. Requisitos

- Git.
- Android Studio con Android SDK.
- Gradle Wrapper incluido en el repositorio.
- JDK compatible con el wrapper; el daemon utiliza Java 21.
- Conexión a Internet para la primera descarga de Gradle, dependencias y toolchains.
- Memoria disponible para Gradle y el daemon de Kotlin.

### Versiones del proyecto

| Componente | Versión |
|---|---:|
| Gradle | 9.8.2 |
| Kotlin | 2.4.20 |
| Android Gradle Plugin | 9.5.2 |
| KorGE | 6.0.0 |
| Compose Multiplatform | 1.12.1 |
| JavaFX | 21.0.2 |
| ktlint Gradle plugin | 14.2.0 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 24 |
| JVM objetivo | 21 |

## 3. Clonar el repositorio

```powershell
git clone https://github.com/AndresSC25/DragonFirstFlight.git; Set-Location ".\DragonFirstFlight"
```

## 4. Configurar el entorno

### 4.1 Android SDK

Gradle requiere la ruta del SDK. Android Studio normalmente crea `local.properties` con una entrada similar a:

```properties
sdk.dir=C\:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
```

`local.properties` es local y no debe agregarse al repositorio.

### 4.2 Verificar Gradle y Java

```powershell
.\gradlew.bat --version
```

La primera ejecución puede descargar Gradle, dependencias y Java 21.

### 4.3 Abrir en Android Studio

Abra la carpeta raíz `DragonFirstFlight` y espere a que finalice la sincronización de Gradle.

## 5. Compilar

Todos los comandos se ejecutan desde la raíz del repositorio.

### 5.1 Validación combinada

```powershell
.\gradlew.bat :shared:compileKotlinJvm :desktopApp:compileKotlin :androidApp:assembleDebug --console=plain --stacktrace --build-cache
```

Resultado validado: `BUILD SUCCESSFUL`.

### 5.2 Android debug

```powershell
.\gradlew.bat :androidApp:assembleDebug --console=plain --stacktrace --build-cache
```

Salida:

```text
androidApp\build\outputs\apk\debug\androidApp-debug.apk
```

El APK debug se firma automáticamente con la clave de depuración y se utiliza únicamente para pruebas.

### 5.3 Android release firmado

El artefacto final se generó mediante **Build > Generate Signed App Bundle or APK** de Android Studio, usando un keystore privado almacenado fuera del repositorio.

Datos del artefacto final:

| Campo | Valor |
|---|---|
| Archivo | `DragonsFirstFlight-v1.0.0.apk` |
| Tamaño | `115,364,227` bytes (aprox. 110.02 MiB) |
| Package ID | `com.redcrow.dragonsfirstflight` |
| Version name / code | `1.0` / `1` |
| Firma | APK Signature Scheme v2 |
| Firmante | Red Crow / Universidad CENFOTEC |
| SHA-256 | `CBD1F19E4D64CC5EE55F03D11BBF0266D3A20AEAF2603352356890B0D60991D4` |

Verificación de firma:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --verbose --print-certs ".\androidApp\release\DragonsFirstFlight-v1.0.0.apk"
```

Resultado validado: `Verifies`, un firmante y esquema v2 válido.

> El keystore, sus contraseñas y `local.properties` son privados. Nunca deben agregarse al repositorio ni adjuntarse a una GitHub Release.

### 5.4 Desktop JVM

Compilar:

```powershell
.\gradlew.bat :desktopApp:compileKotlin --console=plain --stacktrace --build-cache
```

Ejecutar:

```powershell
.\gradlew.bat :desktopApp:run --console=plain --stacktrace
```

### 5.5 Estilo

```powershell
.\gradlew.bat ktlintCheck --console=plain --stacktrace
```

## 6. Ejecutar Android

### 6.1 Verificar dispositivo

Inicie un único emulador desde Device Manager o conecte un dispositivo con depuración USB:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

### 6.2 Instalar APK debug

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r ".\androidApp\build\outputs\apk\debug\androidApp-debug.apk"
```

### 6.3 Instalar APK release

Si existe una versión debug o una versión firmada con otra clave, desinstale primero el paquete. Esto elimina los datos locales de la aplicación.

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" uninstall com.redcrow.dragonsfirstflight; & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install ".\androidApp\release\DragonsFirstFlight-v1.0.0.apk"
```

Resultado validado en `emulator-5554`: instalación `Success` y paquete confirmado mediante `pm path`.

## 7. Ejecutar pruebas

```powershell
.\gradlew.bat :shared:jvmTest :shared:testAndroidHostTest :desktopApp:test --console=plain --stacktrace --build-cache
```

Resultados registrados:

| Tarea | Pruebas | Estado |
|---|---:|---|
| `:shared:jvmTest` | 238 | PASS |
| `:shared:testAndroidHostTest` | 215 | PASS |
| `:desktopApp:test` | 9 | PASS |

El módulo `androidApp` no contiene una suite de pruebas propia.

## 8. Distribuir

### 8.1 Android

El APK final se adjunta a **GitHub Releases**. No debe versionarse dentro del repositorio.

Publicación validada:

```text
Tag: v1.0.0
Archivo: DragonsFirstFlight-v1.0.0.apk
Target: main
```

Para futuras versiones:

1. Reutilice el mismo keystore privado.
2. Incremente `versionCode`.
3. Actualice `versionName`.
4. Genere nuevamente el APK firmado.
5. Verifique la firma con `apksigner`.
6. Instale y pruebe exactamente el APK que se publicará.
7. Adjunte el APK a una nueva GitHub Release.

### 8.2 Desktop JVM

```powershell
.\gradlew.bat :desktopApp:createDistributable --console=plain --stacktrace
```

Los instaladores nativos deben construirse en el sistema operativo correspondiente:

```text
:desktopApp:packageDmg  -> macOS
:desktopApp:packageMsi  -> Windows
:desktopApp:packageDeb  -> Linux
```

`packageDmg` fue validado. `packageMsi` y `packageDeb` no fueron ejecutados durante la verificación documentada.

## 9. Documentación de API

```powershell
.\gradlew.bat :shared:dokkaGenerate --console=plain --stacktrace
```

Salida:

```text
shared\build\dokka\html\index.html
```

## 10. Solución de problemas

| Síntoma | Causa y solución |
|---|---|
| Gradle no encuentra el SDK | Crear `local.properties` con `sdk.dir=...` o definir `ANDROID_HOME`. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Existe una versión con una firma diferente. Desinstalar `com.redcrow.dragonsfirstflight` e instalar nuevamente el APK release. |
| Hay más de un emulador activo | Seleccionar el destino con `adb -s emulator-XXXX` o cerrar los emuladores adicionales. |
| No existe el APK debug | Ejecutar `:androidApp:assembleDebug`. |
| La primera compilación falla sin red | Permitir que Gradle descargue dependencias y Java 21 con conexión a Internet. |
| Desktop no abre | Confirmar una sesión gráfica y ejecutar `:desktopApp:run`. |
| Advertencia de Gradle 10 | No bloquea la versión actual. Debe atenderse en una actualización futura de plugins y scripts. |

## 11. Validación final

| Verificación | Resultado |
|---|---|
| Rama y commit | `main`, commit `42bc659` |
| Compilación Shared, Desktop y Android debug | PASS |
| APK final | `DragonsFirstFlight-v1.0.0.apk` |
| Firma | PASS, APK Signature Scheme v2 |
| SHA-256 | `CBD1F19E4D64CC5EE55F03D11BBF0266D3A20AEAF2603352356890B0D60991D4` |
| Instalación release | PASS en `emulator-5554` |
| Package ID | `com.redcrow.dragonsfirstflight` |
| Nombre e ícono | Verificados en el launcher de Android |
| Funciones principales | Introducción, menú, gameplay, pausa, Endless, Shop, Inventory, música y efectos verificados |
| Distribución | GitHub Releases |

La versión entregada fue implementada, compilada y validada para Android y Desktop JVM. El repositorio conserva `iosApp` de la plantilla inicial, pero el videojuego no fue integrado ni validado para iOS; por tanto, no se declara soporte de despliegue para esa plataforma.
