# Política de seguridad

## Reporte de vulnerabilidades

No publiques credenciales, tokens, datos médicos ni información personal en un issue.
Reporta el problema de forma privada al responsable del repositorio e incluye los pasos
para reproducirlo, la versión afectada y el impacto esperado.

## Controles automatizados

Cada cambio a `main` ejecuta compilación, Android Lint, pruebas unitarias y escaneo de
secretos. Dependabot revisa semanalmente las dependencias de Gradle y GitHub Actions.

## Manejo de configuración

La URL pública de Firebase se configura con la propiedad Gradle o variable de entorno
`FIREBASE_DATABASE_URL`. Nunca deben almacenarse claves privadas, archivos de cuentas
de servicio ni tokens de autenticación dentro del APK o del repositorio.

Las reglas incluidas en `firebase.database.rules.json` permiten lectura y escritura
públicas únicamente para el prototipo. Antes de una publicación se debe incorporar
Firebase Authentication y restringir cada dispositivo a su propia ruta.
