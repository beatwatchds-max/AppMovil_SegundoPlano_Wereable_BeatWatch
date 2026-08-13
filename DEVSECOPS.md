# Verificación DevSecOps

El flujo `.github/workflows/android-ci.yml` se ejecuta en cada `push` y `pull_request`
contra `main`. Comprueba:

- compilación del APK de depuración;
- pruebas unitarias JVM;
- Android Lint;
- formato básico de Git y sintaxis JSON de las reglas de Firebase;
- secretos expuestos mediante Gitleaks;
- publicación de reportes y del APK como artefactos de GitHub Actions.

## Verificación local

En Windows, desde la raíz del proyecto:

```bat
gradlew.bat clean testDebugUnitTest lintDebug assembleDebug
```

Los reportes principales quedan en:

- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/lint-results-debug.html`
- `app/build/outputs/apk/debug/app-debug.apk`

## Configuración en GitHub

1. Subir los cambios a una rama y abrir un pull request contra `main`.
2. En **Settings > Branches > Branch protection rule**, proteger `main`.
3. Activar **Require a pull request before merging**.
4. Activar **Require status checks to pass before merging** y seleccionar:
   - `Build, Lint y pruebas`
   - `Escaneo de secretos`
5. Activar Dependabot alerts y Dependabot security updates en
   **Settings > Code security and analysis**.

El repositorio no necesita guardar contraseñas para escribir en la configuración
actual de Firebase. La URL puede cambiarse en GitHub con una variable de repositorio
llamada `FIREBASE_DATABASE_URL`.

## Firebase

`firebase.database.rules.json` documenta y valida el esquema esperado. Las reglas
deben copiarse/publicarse en Firebase Realtime Database; incluir el archivo en GitHub
no las despliega automáticamente.

Las reglas actuales permiten lectura y escritura sin autenticación para facilitar el
prototipo. Antes de producción se debe incorporar Firebase Authentication y exigir
`auth != null`, tal como se advierte en `SECURITY.md`.
