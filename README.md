# A17 Screen Recorder Enabler

Módulo LSPosed que habilita la grabadora de pantalla de Samsung en el Galaxy A17, donde el tile **Grabador de pantalla** viene oculto.

## Requisitos

- Galaxy A17 5G (SM-A176B) con One UI 9 / Android 17 (probado en `A176BXXU8DZJ5`)
- Root + LSPosed (probado con Vector / JingMatrix LSPosed v2.2)
- Scope: `com.android.systemui` y `com.samsung.android.app.smartcapture`
- La app **Grabador de pantalla** de Samsung debe estar instalada (viene con One UI)

## Cómo funciona

Samsung elimina `SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_SCREEN_RECORDER` en este modelo, así que `TileFeatureChecker` oculta `RecordScreenTile`. El módulo fuerza ese flag a `true` en `SemFloatingFeature`, con lo que el tile vuelve a aparecer.

## Instalación

1. Instala el APK desde **Releases**.
2. Actívalo en LSPosed/Vector y marca los dos paquetes del scope.
3. Reinicia SystemUI o el teléfono.
4. Añade el tile **Grabador de pantalla** desde el panel de edición de Quick Settings.

```sh
adb shell su -c 'kill $(pidof com.android.systemui)'
```

## Estado

Probado en Galaxy A17 5G (SM-A176B). En principio funciona en otros Samsung con el mismo flag desactivado.
